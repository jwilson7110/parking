package com.jef.parking;

import java.sql.SQLException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;



@SpringBootApplication
public class ParkingApplication 
{
	
	
	public static void main(String[] args) throws SQLException 
	{
		SpringApplication.run(ParkingApplication.class, args);
			
		//new LotService().importFromFile("C:\\Users\\jwils\\Downloads\\parking\\HDBCarparkInformation.csv");
		//new LotAvailabilityService().importFromUrl("https://api.data.gov.sg/v1/transport/carpark-availability");		
		
	}

}
