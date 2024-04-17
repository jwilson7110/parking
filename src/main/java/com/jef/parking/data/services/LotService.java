package com.jef.parking.data.services;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.UUID;

import com.jef.parking.annotations.TextImportSetter;
import com.jef.parking.data.Lot;

public class LotService extends DataService <Lot> 
{
	
	
	public LotService ()
	{
		super(Lot.class);
		dataTypes.put("currentAvailabilityId", UUID.class);
	}
	
	public HashMap <String, String> convertStringsToMap(String [] keys, String [] values)//maps out a set of strings to the top row of a csv
	{
		var result = new HashMap <String, String> ();
		
		for (var i = 0; i < Math.min(keys.length, values.length); ++i)//whichever has fewer items will be our limit
		{
			result.put(keys[i], values[i]);
		}
		
		return result;
		
	}
	
	public Lot importNewLot (HashMap <String, String> values)
	{
		Lot result = new Lot();
		
		for (var key : values.keySet())
		{
			Object value = values.get(key);
			
			for (Method method : Lot.class.getMethods())
			{							
				var annotation = method.getAnnotation(TextImportSetter.class);
				
				if (annotation == null)
				{
					continue;
				}
				
				var setter = (TextImportSetter)annotation;
						
				if (setter.name().equals(key))
				{
					var type = method.getParameterTypes()[0];
					if (type.getTypeName() == "double")
					{
						value = Double.parseDouble((String)value);
					}
					else if (type.getTypeName() == "int")
					{
						value = Integer.valueOf((String)value);
					}
					
					try 
					{
						method.invoke(result, value);
					} catch (Exception e) {}
				
				}
			}
		}
		
		
		return result;
	}
		
	public ArrayList <Lot> importFromFile (String filePath)
	{	
		var data = new ArrayList <Lot> ();
		
		var existingItems = queryMapList("select id, number, currentAvailabilityId, latitude, longitude from Lot");
		var existingItemMap = new HashMap <String, HashMap <String, Object>>();
		
		for (var lot : existingItems)
		{
			existingItemMap.put((String)lot.get("number"), lot);
		}
		  
		var file = new File (filePath);
		String [] fields = new String[0];
		
		boolean first = true;
		
		Scanner scanner = null;
		
		try
		{
			scanner = new Scanner(file);
		}
		catch (Exception e)
		{
			System.err.println("Could not open file: " + filePath);
			e.printStackTrace();
			
			if (scanner != null)
			{
				scanner.close();
			}
			
			return null;
		}
		
		while (scanner.hasNextLine()) {
			
			String line = scanner.nextLine();
			
			var items = new ArrayList <String> ();
			String item = "";
			
			var currentQuote = new ArrayList <Character> ();
			
			for (var i = 0; i < line.length(); ++i)
			{
				var c = line.charAt(i);
				Character quote = null;
				if (!currentQuote.isEmpty())
				{
					quote = currentQuote.get(currentQuote.size() - 1);
				}
				
				if (c == ',' && quote == null)
				{
					items.add(item);
					item = "";
				}
				
				else if (quote != null && c == quote)
				{
					currentQuote.remove(currentQuote.size() - 1);
				}
				else if (c == '"'/* || c == '\'' || c == '`'*/)
				{
					currentQuote.add(c);
				}
				else
				{
					item += c;
				}
			}
			
			items.add(item);
			
			if (first) {
				first = false;
				fields = items.toArray(new String [items.size()]);
				continue;
			}
			
			Lot lot = importNewLot(convertStringsToMap(fields, items.toArray(new String [items.size()])));					
			
			var existingLot = existingItemMap.getOrDefault(lot.getNumber(), null);
			if (existingLot != null)
			{
				lot.setId((UUID)existingLot.get("id"));
				lot.setCurrentAvailabilityId((UUID)existingLot.get("currentAvailabilityId"));
				
				Float latitude = (Float)existingLot.get("latitude");
				Float longitude = (Float)existingLot.get("longitude");
				
				if (latitude != null)
				{
					lot.setLatitude(latitude.doubleValue());
				}
				
				if (longitude != null)
				{
					lot.setLongitude(longitude.doubleValue());
				}
				
			}	
			
			data.add(lot);
		}
		
		scanner.close();
		
		saveObjects(data);
		return data;
	}
	
	public ArrayList <Lot> queryLotsClosestToPoint(double latitude, double longitude, int page, int per_page)
	{
		String command = "select *, "
				+ "ACOS(SIN(PI()*latitude/180.0)*SIN(PI()*?/180.0)+COS(PI()*latitude/180.0)*COS(PI()*?/180.0)*COS(PI()*?/180.0-PI()*longitude/180.0))*6371 as distance "
				+ "from Lot l "
				//+ "left join lotAvailability la on l.currentAvailabilityId = la.id "
				+ "order by distance "
				+ "limit ? offset ?";
		
		return this.queryObjectList(command, new ArrayList <Object> (Arrays.asList(latitude, latitude, longitude, per_page, (page - 1) * per_page)));
	}
	
	
	public void fillCurrentLotAvailability (ArrayList <Lot> lots)
	{
		
		var ids = new ArrayList <Object> ();
		var map = new HashMap <UUID, Lot> ();
		String parameters = "";
		
		var first = true;
		for (var lot : lots)
		{
			ids.add(lot.getCurrentAvailabilityId());
			map.put(lot.getCurrentAvailabilityId(), lot);
			
			if (!first)
			{
				parameters += ",";
			}
			first = false;
			parameters += "?";
		}
		
		String command = "select * from LotAvailability where id in (" + parameters + ")";
		var lotAvailabilities = new LotAvailabilityService().queryObjectList(command, ids);
		
		for (var lotAvailability : lotAvailabilities)
		{
			if (!map.containsKey(lotAvailability.getId()))
			{
				continue;				
			}
			
			map.get(lotAvailability.getId()).setCurrentAvailability(lotAvailability);
		}
	}
	
}
