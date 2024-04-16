package com.jef.parking;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class LotAvailabilityService extends DataService <LotAvailability> 
{

	public LotAvailabilityService ()
	{
		super(LotAvailability.class);
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
	
	public LotAvailability importNewLotAvailability (HashMap <String, String> values)
	{
		LotAvailability result = new LotAvailability();
		
		
		
		for (var key : values.keySet())
		{
			Object value = values.get(key);
			
			for (Method method : LotAvailability.class.getMethods())
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
	
	public HashMap <String, Object> flattenData(JsonArray data, HashMap <String, Object> result)
	{
		for (var item : data.asList())
		{
			if (item.isJsonObject())
			{
				flattenData(item.getAsJsonObject(), result);
			}
			if (item.isJsonArray())
			{
				flattenData(item.getAsJsonArray(), result);
			}	
		}
		
		return result;
	}
	
	public HashMap <String, Object> flattenData(JsonObject data, HashMap <String, Object> result)
	{
		if (result == null) 
		{
			result = new HashMap <String, Object> ();
		}
		
		for (String key : data.keySet())
		{
			var item = data.get(key);
			
			if (item.isJsonObject())
			{
				flattenData(item.getAsJsonObject(), result);
			}
			if (item.isJsonArray())
			{
				flattenData(item.getAsJsonArray(), result);
			}
			else
			{
				result.put(key, item.getAsString());
			}
		}
		
		return result;
	}
	
	public HashMap <String, Object> flattenData(JsonObject data)
	{
		return flattenData(data, null);
	}
		
	public ArrayList <LotAvailability> importFromUrl (String url)
	{	
		try
		{
			var result = new ArrayList <LotAvailability> ();
			
			var lotService = new LotService();
			var lots = lotService.queryObjectList("select * from Lot");
			var lotMap = new HashMap <String, Lot>();
			
			for (var lot : lots)
			{
				lotMap.put(lot.getNumber(), lot);
			}
			
			HttpClient client = HttpClient.newHttpClient();
		    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
	
		    HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
		    
		    
		    var allData = new Gson().fromJson(response.body(), JsonObject.class);
		    var data = allData.getAsJsonObject().get("items").getAsJsonArray().get(0).getAsJsonObject().get("carpark_data").getAsJsonArray();
		    
		    for (var item : data.asList())
		    {
		    	var lotAvailability = new LotAvailability();
		    	lotAvailability.setId(UUID.randomUUID());
		    	
		    	var jsonData = item.getAsJsonObject();
		    	var number = jsonData.get("carpark_number").getAsString();
		    	if (!lotMap.containsKey(number))
		    	{
		    		continue;
		    	}
		    	
		    	var lot = lotMap.get(number);		    	
		    	lotAvailability.setLotId(lot.getId());
		    	
		    	var itemMap = flattenData(jsonData);
		    	
		    	for (var method : LotAvailability.class.getMethods())
		    	{
		    		var setter = method.getAnnotation(TextImportSetter.class);
		    		
		    		if (setter == null)
		    		{
		    			continue;
		    		}
		    		
		    		var value = itemMap.get(setter.name());
		    		
		    		var type = method.getParameterTypes()[0];
		    		if (type.equals(LocalDateTime.class))
		    		{
		    			value = LocalDateTime.parse((String)value);
		    		}
		    		else if (type.getTypeName() == "int")
					{
						value = Integer.valueOf((String)value);
					}
		    		
		    		method.invoke(lotAvailability, value);
		    	}
		    	
		    	this.insert(lotAvailability);
		    	
		    	lot.setCurrentAvailabilityId(lotAvailability.getId());
		    	lotService.update(lot);
			}
			 
			
			return result;
			
		} catch (Exception e) {e.printStackTrace();}
		
		return null;		
	}
	
	
	
}
