package com.jef.parking.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jef.parking.data.services.LotAvailabilityService;

@RestController
public class LotAvailabilityController 
{
		
	//endpoint for importing lot availability data from url
	//TODO: allow alternate url 
	@GetMapping("/importLotAvailability")
	public String importLotAvailability()   
	{
	
		new LotAvailabilityService().importFromUrl("https://api.data.gov.sg/v1/transport/carpark-availability");		
		return "OK";
		
	}

}
