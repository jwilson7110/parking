package com.jef.parking;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.UUID;

public class LotService extends DataService <Lot> 
{

	public LotService ()
	{
		super(Lot.class);
	}
	
	public HashMap <String, String> convertStringsToMap(String [] keys, String [] values)
	{
		var result = new HashMap <String, String> ();
		
		for (var i = 0; i < Math.min(keys.length, values.length); ++i)
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
		try
		{
			var result = new ArrayList <Lot> ();
			
			var existingItems = queryObjectList("select * from Lot");
			var existingItemMap = new HashMap <String, Lot>();
			
			for (var lot : existingItems)
			{
				existingItemMap.put(lot.getNumber(), lot);
			}
			  
			var file = new File (filePath);
			String [] fields = new String[0];
			
			boolean first = true;			
			try (var scanner = new Scanner(file))
			{
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
						else if (c == '"' || c == '\'' || c == '`')
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
					result.add(lot);
					
					if (existingItemMap.containsKey(lot.getNumber()))
					{
						lot.setId(existingItemMap.get(lot.getNumber()).getId());
						update(lot);
					}
					else 
					{
						insert(lot);
					}
				}
			}		
			 
			
			return result;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;		
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
