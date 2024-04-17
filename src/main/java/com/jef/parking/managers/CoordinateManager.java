package com.jef.parking.managers;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jef.parking.Config;

public class CoordinateManager 
{
	
	public static JsonObject convertSVY21toWGS84(double xCoordinate, double yCoordinate)
	{
		HttpClient client = HttpClient.newHttpClient();
		var url = "https://www.onemap.gov.sg/api/common/convert/3414to4326?X=" + xCoordinate + "&Y=" + yCoordinate;
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(url))
		        .headers("Authorization", "Bearer " + Config.getValue("onemap.token")) //this token tends to expire,TODO: update this token dynmically 
		        .build();
	
		HttpResponse<String> response = null;
		
		try
		{			
			response = client.send(request, BodyHandlers.ofString());
		}
		catch (Exception e)
		{
			System.err.println("Error in http request to " + url);
			e.printStackTrace();
		}
		
		JsonObject result = null;
		
		if (response != null)
		{
			var body = response.body();
			result = JsonParser.parseString(body).getAsJsonObject();
		}
		
		return result;
		
		
	}
	
	//in app method for calculating distance between 2 coordinates
	//not currently used
	public static double calculateDistanceBetween2Coordinates (double latitude1, double longitude1, double latitude2, double longitude2)
	{
		
		double lat1Rad = Math.toRadians(latitude1);
	    double lat2Rad = Math.toRadians(latitude2);
	    double lon1Rad = Math.toRadians(longitude1);
	    double lon2Rad = Math.toRadians(longitude2);

	    double x = (lon2Rad - lon1Rad) * Math.cos((lat1Rad + lat2Rad) / 2);
	    double y = (lat2Rad - lat1Rad);
	    double distance = Math.sqrt(x * x + y * y) * 6371;

	    return distance;
	}

}
