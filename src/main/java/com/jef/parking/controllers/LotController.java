package com.jef.parking.controllers;

import java.io.IOException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jef.parking.Config;
import com.jef.parking.data.services.LotService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

@RestController
public class LotController 
{
	//imports lots data from file path set in config
	//TODO: allow for uploading file
	//TODO: this action takes a long time due to the coordinate conversion of each record, this should show some progress in logs
	@GetMapping("/importLots")
	public String importLots ()  
	{
		new LotService().importFromFile(Config.getValue("lots.file"));//import from default filepath set in config
		return "OK";
		
	}
	
	//finds the nearest parking lots within given latitude and longitude
		//latitude: required double TODO: handle invalid format
		//longitude: required double TODO: handle invalid format
		//page: the offset of where to start returning results
		//per_page: the limit of results returned
	@GetMapping("/carparks/nearest")
	public void nearest (HttpServletResponse response, @RequestParam() double latitude, @RequestParam() double longitude, @RequestParam(required = false, defaultValue = "1") int page, @RequestParam(required = false, defaultValue = "20") int per_page) throws IOException 
	{
		JsonArray result = new JsonArray();
		
		HttpServletResponseWrapper wrapper = new HttpServletResponseWrapper(response);
        wrapper.setContentType("application/json;charset=UTF-8");
		
        var lotService = new LotService();
		var lots = lotService.queryLotsClosestToPoint(latitude, longitude, page, per_page);
		lotService.fillCurrentLotAvailability(lots); //queries all lot availability data and maps it to lot objects, this saves us from running seperate queries for each lot returned
		
		for (var lot : lots)
		{
			var lotJson = new JsonObject();
			lotJson.addProperty("address", lot.getAddress());
			lotJson.addProperty("latitde", lot.getLatitude());
			lotJson.addProperty("longitude", lot.getLongitude());
			
			var lotAvailability = lot.getCurrentAvailability();
			if (lotAvailability != null)//not all lots have availability data
			{
				lotJson.addProperty("total_lots", lotAvailability.getTotal());
				lotJson.addProperty("available_lots", lotAvailability.getAvailable());
			}			
			
			result.add(lotJson);		
		}
		
		response.getWriter().print(new Gson().toJson(result));
	}

}
