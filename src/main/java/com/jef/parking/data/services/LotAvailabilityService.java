package com.jef.parking.data.services;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jef.parking.annotations.TextImportSetter;
import com.jef.parking.data.Lot;
import com.jef.parking.data.LotAvailability;

public class LotAvailabilityService extends DataService <LotAvailability> 
{

	public LotAvailabilityService ()
	{
		super(LotAvailability.class);
	}	
	
	
	
	public HashMap <String, Object> flattenData(JsonArray data, HashMap <String, Object> result) //regardless of nested data, we can go through all the values in the json element
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
	
	public HashMap <String, Object> flattenData(JsonObject data, HashMap <String, Object> result)//regardless of nested data, we can go through all the values in the json element
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
				flattenData(item.getAsJsonObject(), result); //if we come across a json element, loop through all its values and map them to the base set 
			}
			if (item.isJsonArray())
			{
				flattenData(item.getAsJsonArray(), result);
			}
			else
			{
				result.put(key, item.getAsString()); //put the key value pair into the final result 
			}
		}
		
		return result;
	}
	
	public HashMap <String, Object> flattenData(JsonObject data) //at level one, we dont have a final result
	{
		return flattenData(data, null);
	}
	
	//TODO: use multiple value insert functions in order to not close connection with each entry
	public ArrayList <LotAvailability> importData(String dataString) //take in a json array string and convert it to a list of Objects
	{
		var result = new ArrayList <LotAvailability> ();
		
		//we need to map out the existing lots so we can find the lot id the lotavailability is linked to
		var lotService = new LotService();
		var lots = lotService.queryObjectList("select * from Lot");
		var lotMap = new HashMap <String, Lot>();
		
		for (var lot : lots)
		{
			lotMap.put(lot.getNumber(), lot); //we map by the lot number since we dont have the id yet
		}
		
		
		var allData = new Gson().fromJson(dataString, JsonObject.class);//start by converting the string into a Json elemetn
	    var data = allData.getAsJsonObject().get("items").getAsJsonArray().get(0).getAsJsonObject().get("carpark_data").getAsJsonArray(); //trace through to out elements	    
	    
	    for (var item : data.asList())
	    {
	    	var lotAvailability = new LotAvailability();
	    	lotAvailability.setId(UUID.randomUUID()); //we need to know the id to update the lot row
	    	
	    	var jsonData = item.getAsJsonObject();
	    	var number = jsonData.get("carpark_number").getAsString();
	    	if (!lotMap.containsKey(number))//if we dont have a lot row with this number we ignore it for now
	    	{
	    		continue;
	    	}
	    	
	    	var lot = lotMap.get(number); //get the lot this availability set belongs to
	    	lotAvailability.setLotId(lot.getId());//we use ou own ids to join
	    	
	    	var itemMap = flattenData(jsonData);//we dont care where the value is nested
	    	
	    	for (var method : LotAvailability.class.getMethods()) //go through every method in Lot AVailability
	    	{
	    		var setter = method.getAnnotation(TextImportSetter.class);
	    		
	    		if (setter == null) //set every method is a setter
	    		{
	    			continue;
	    		}
	    		
	    		var value = itemMap.get(setter.name());
	    		
	    		var type = method.getParameterTypes()[0];//the setter should only have one parameter, we need to know the type in case we need to make conversions
	    		
	    		//TODO: support more types
	    		if (type.equals(LocalDateTime.class))
	    		{
	    			value = LocalDateTime.parse((String)value);
	    		}
	    		else if (type.getTypeName() == "int")
				{
					value = Integer.valueOf((String)value);
				}
	    		
	    		try
	    		{
	    			method.invoke(lotAvailability, value);
	    		} 
	    		catch (Exception e) 
	    		{
	    			System.err.println("Could not Execute Setter: " + setter.name() + " value: " + value + " valueType: " + value.getClass().getName());
					e.printStackTrace();
	    		}
	    	}
	    	
	    	this.insert(lotAvailability);
	    	
	    	lot.setCurrentAvailabilityId(lotAvailability.getId());//we are assuming this is the latest availability data
	    	lotService.update(lot);
		}
		
		return result;	
		
	}
		
	public ArrayList <LotAvailability> importFromUrl (String url) //import data from json endpoint at supplied url and insert into database 
	{	
		HttpClient client = HttpClient.newHttpClient();
	    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
	    HttpResponse<String> response = null;
	    
		try
		{
		    response = client.send(request, BodyHandlers.ofString());
		} 
		catch (Exception e) 
		{
			System.err.println("Could not retireve data from url: " + url);
			e.printStackTrace();
		}
		
		if (response == null)
		{
			return null;
		}
		
	    return importData(response.body());
		    
	}
	
	
	
}
