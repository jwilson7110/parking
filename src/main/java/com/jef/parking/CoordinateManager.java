package com.jef.parking;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class CoordinateManager 
{
	
	public static JsonObject convertSVY21toWGS84(double xCoordinate, double yCoordinate)
	{
		try {
			HttpClient client = HttpClient.newHttpClient();
		    HttpRequest request = HttpRequest.newBuilder()
		          .uri(URI.create("https://www.onemap.gov.sg/api/common/convert/3414to4326?X=" + xCoordinate + "&Y=" + yCoordinate))
		          .headers("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMTk5MjczMmNmOTU3M2Q1YjU0ODc2YjkzYWJmOGQzZSIsImlzcyI6Imh0dHA6Ly9pbnRlcm5hbC1hbGItb20tcHJkZXppdC1pdC0xMjIzNjk4OTkyLmFwLXNvdXRoZWFzdC0xLmVsYi5hbWF6b25hd3MuY29tL2FwaS92Mi91c2VyL3Bhc3N3b3JkIiwiaWF0IjoxNzEzMjE2MTAxLCJleHAiOjE3MTM0NzUzMDEsIm5iZiI6MTcxMzIxNjEwMSwianRpIjoicWJ5cVdGSWhRVkIxa0dTZyIsInVzZXJfaWQiOjMyMTcsImZvcmV2ZXIiOmZhbHNlfQ.xrNXsESZFE_gWikJ0jm6EGs1MyjNVzrIOMvFxmCWJ5o")
		          .build();
	
		    HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
		    
		    return JsonParser.parseString(response.body()).getAsJsonObject();
		    
		} catch (Exception e) {e.printStackTrace();} 
			
		return null;
	}
	
	public static double calculateDistanceBetween2Coordinates (double latitude1, double longitude1, double latitude2, double longitude2)
	{
		
		//return Math.acos( Math.sin(latitude1) * Math.sin(latitude2) + Math.cos(latitude1) * Math.cos(latitude2) * Math.cos(longitude2 - longitude1) ) * 6371;
		
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
