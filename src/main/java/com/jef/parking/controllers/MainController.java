package com.jef.parking.controllers;

import java.io.IOException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jef.parking.Config;
import com.jef.parking.data.services.LotAvailabilityService;
import com.jef.parking.data.services.LotService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

@RestController
public class MainController 
{
	
	@GetMapping("/lots")
	public void lots (HttpServletResponse response, @RequestParam() double latitude, @RequestParam() double longitude, @RequestParam(required = false, defaultValue = "1") int page, @RequestParam(required = false, defaultValue = "20") int per_page) throws IOException 
	{
		JsonArray result = new JsonArray();
		
		HttpServletResponseWrapper wrapper = new HttpServletResponseWrapper(response);
        wrapper.setContentType("application/json;charset=UTF-8");
		
        var lotService = new LotService();
		var lots = lotService.queryLotsClosestToPoint(latitude, longitude, page, per_page);
		lotService.fillCurrentLotAvailability(lots);
		
		for (var lot : lots)
		{
			var json = new JsonObject();
			json.addProperty("address", lot.getAddress());
			json.addProperty("latitde", lot.getLatitude());
			json.addProperty("longitude", lot.getLongitude());
			
			var lotAvailability = lot.getCurrentAvailability();
			if (lotAvailability != null)
			{
				json.addProperty("total_lots", lotAvailability.getTotal());
				json.addProperty("available_lots", lotAvailability.getAvailable());
			}
			
			
			result.add(json);
			
		}	

		
		response.getWriter().print(new Gson().toJson(result));
	}

	
	@GetMapping("/importLots")
	public String importLots ()  
	{
		
		//new LotService().importFromFile("/lots.csv");
		new LotService().importFromFile(Config.getValue("lots.file"));
		return "OK";
		
	}
	
	
	@GetMapping("/importLotAvailability")
	public String importLotAvailability()  
	{
	
		new LotAvailabilityService().importFromUrl("https://api.data.gov.sg/v1/transport/carpark-availability");		
		return "OK";
		
	}
}