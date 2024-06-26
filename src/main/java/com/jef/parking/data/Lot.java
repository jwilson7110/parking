package com.jef.parking.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import com.jef.parking.annotations.DatabaseGetter;
import com.jef.parking.annotations.DatabaseSetter;
import com.jef.parking.annotations.DatabaseTable;
import com.jef.parking.annotations.TextImportSetter;
import com.jef.parking.data.services.LotAvailabilityService;
import com.jef.parking.managers.CoordinateManager;

@DatabaseTable(name = "Lot")
public class Lot extends Data //class representing a row in the lot database table
{
	
	public Lot () {
		
		
	}
	
	
	private UUID id;
	
	@DatabaseGetter(name = "id")
	public UUID getId()
	{
		return id;
	}
	
	@DatabaseSetter(name = "id")
	public void setId(UUID value)
	{
		this.id = value;
	}
	
	
	private UUID currentAvailabilityId;
	
	@DatabaseGetter(name = "currentAvailabilityId")
	public UUID getCurrentAvailabilityId()
	{
		return currentAvailabilityId;
	}
	
	@DatabaseSetter(name = "currentAvailabilityId")
	public void setCurrentAvailabilityId(UUID value)
	{
		if (getCurrentAvailabilityId() != value)
		{
			setCurrentAvailability(null);//if the id value changes, the we need to let go of the availability object we are holding on to 
		}
		
		this.currentAvailabilityId = value;
		
	}
	
	private LotAvailability currentAvailability;
	public LotAvailability getCurrentAvailability()//lazy getter for current availability
	{
		if (currentAvailability == null)
		{
			if (getCurrentAvailabilityId() != null)
			{
				var items = new LotAvailabilityService().queryObjectList("select * from LotAvailability where id = ?", new ArrayList <Object> (Arrays.asList(getCurrentAvailabilityId())));//TODO: functions for return one item and passing in one parameter
				if (items.size() > 0)//there should be only one or zero
				{
					setCurrentAvailability(items.get(0));
				}
			}
		}
		
		return currentAvailability;
	}
	
	public void setCurrentAvailability (LotAvailability value)
	{
		this.currentAvailability = value;
	}
	
	
	private String number;
	
	@DatabaseGetter(name = "number") //car park number, official external id
	public String getNumber()
	{
		return number;
	}
	
	@TextImportSetter(name = "car_park_no")
	@DatabaseSetter(name = "number")
	public void setNumber(String value)
	{
		this.number = value;
	}
	
	
	private String address;
	
	@DatabaseGetter(name = "address")
	public String getAddress()
	{
		return address;
	}
	
	@TextImportSetter(name = "address")
	@DatabaseSetter(name = "address")
	public void setAddress(String value)
	{
		this.address = value;
	}
	
	
	
	private double xCoordinate;
	
	@DatabaseGetter(name = "xCoordinate")
	public double getXCoodinate()
	{
		return xCoordinate;
	}
	
	@TextImportSetter(name = "x_coord")
	@DatabaseSetter(name = "xCoordinate")
	public void setXCoordinate(double value)
	{
		this.xCoordinate = value;//TODO: reset latitude and longitude if this value changes, there were issues with doing this before, cant remember what though
	}

	private double yCoordinate;
	
	@DatabaseGetter(name = "yCoordinate")
	public double getYCoordinate()
	{
		return yCoordinate;
	}
	
	@TextImportSetter(name = "y_coord")
	@DatabaseSetter(name = "yCoordinate")
	public void setYCoordinate(double value)
	{
		this.yCoordinate = value;//TODO: reset latitude and longitude if this value changes, there were issues with doing this before, cant remember what though
	}
	
	
	private Double latitude;
	
	@DatabaseGetter(name = "latitude")
	public Double getLatitude()
	{
		if (latitude == null)
		{
			determineLatitudeAndLongitude();//if we dont have this value yet, we can request it before returning it
		}
		
		return latitude;
	}
	
	@DatabaseSetter(name = "latitude")
	public void setLatitude(Double value)
	{
		this.latitude = value;
	}

	private Double longitude;
	
	@DatabaseGetter(name = "longitude")
	public Double getLongitude()
	{		
		if (longitude == null)
		{
			determineLatitudeAndLongitude();//if we dont have this value yet, we can request it before returning it
		}
		return longitude;
	}
	
	@DatabaseSetter(name = "longitude")
	public void setLongitude(Double value)
	{
		this.longitude = value;
	}
	
	
	private String type;
	
	@DatabaseGetter(name = "type")
	public String getType()
	{
		return type;
	}
	
	@TextImportSetter(name = "car_park_type")
	@DatabaseSetter(name = "type")
	public void setType(String value)
	{
		this.type = value;
	}
	
	private String system;
	
	@DatabaseGetter(name = "system")
	public String getSystem()
	{
		return system;
	}
	
	@TextImportSetter(name = "type_of_parking_system")
	@DatabaseSetter(name = "system")
	public void setSystem(String value)
	{
		this.system = value;
	}
	
	
	private String shortTerm;
	
	@DatabaseGetter(name = "shortTerm")
	public String getShortTerm()
	{
		return shortTerm;
	}
	
	@TextImportSetter(name = "short_term_parking")
	@DatabaseSetter(name = "shortTerm")
	public void setShortTerm(String value)
	{
		this.shortTerm = value;
	}
	
	private String free;
	
	@DatabaseGetter(name = "free")
	public String getFree()
	{
		return free;
	}
	
	@TextImportSetter(name = "free_parking")
	@DatabaseSetter(name = "free")
	public void setFree(String value)
	{
		this.free = value;
	}
	
	private String night;
	
	@DatabaseGetter(name = "night")
	public String getNight()
	{
		return night;
	}
	
	@TextImportSetter(name = "night_parking")
	@DatabaseSetter(name = "night")
	public void setNight(String value)
	{
		this.night = value;
	}
	
	private int decks;
	
	@DatabaseGetter(name = "decks")
	public int getDecks()
	{
		return decks;
	}
	
	@TextImportSetter(name = "car_park_decks")
	@DatabaseSetter(name = "decks")
	public void setDecks(int value)
	{
		this.decks = value;
	}
	
	private double gantryHeight;
	
	@DatabaseGetter(name = "gantryHeight")
	public double getGantryHeight()
	{
		return gantryHeight;
	}
	
	@TextImportSetter(name = "gantry_height")
	@DatabaseSetter(name = "gantryHeight")
	public void setGantryHeight(double value)
	{
		this.gantryHeight = value;
	}
	
	
	private String basement;
	
	@DatabaseGetter(name = "basement")
	public String getBasement()
	{
		return basement;
	}
	
	@TextImportSetter(name = "car_park_basement")
	@DatabaseSetter(name = "basement")
	public void setBasement(String value)
	{
		this.basement = value;
	}
	
	
	
	public void determineLatitudeAndLongitude()
	{
		var data = CoordinateManager.convertSVY21toWGS84(getXCoodinate(), getYCoordinate());//gets a json from an external endpoint, this process is slow
		
		if (data != null)
		{
			setLatitude(data.get("latitude").getAsDouble());
			setLongitude(data.get("longitude").getAsDouble());
		}
	}
	
	//use this if the supplied coordinates change... somehow
	public void clearLatitudeAndLongitude()
	{
		setLatitude(null);
		setLongitude(null);
	}
	

}

