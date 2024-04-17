package com.jef.parking.data;

import java.time.LocalDateTime;
import java.util.UUID;

import com.jef.parking.annotations.DatabaseGetter;
import com.jef.parking.annotations.DatabaseSetter;
import com.jef.parking.annotations.DatabaseTable;
import com.jef.parking.annotations.TextImportSetter;

@DatabaseTable(name = "LotAvailability")
public class LotAvailability extends Data
{
	
	public LotAvailability () 
	{
		
		
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
	
	
	private UUID lotId;
	
	@DatabaseGetter(name = "lotId")
	public UUID getLotId()
	{
		return lotId;
	}
	
	@DatabaseSetter(name = "lotId")
	public void setLotId(UUID value)
	{
		this.lotId = value;
	}
	
	
	private String type;
	
	@DatabaseGetter(name = "type")
	public String getType()
	{
		return type;
	}
	
	@TextImportSetter(name = "lot_type")
	@DatabaseSetter(name = "type")
	public void setType(String value)
	{
		this.type = value;
	}
	
	
	private int total;
	
	@DatabaseGetter(name = "total")
	public int getTotal()
	{
		return total;
	}
	
	@TextImportSetter(name = "total_lots")
	@DatabaseSetter(name = "total")
	public void setTotal(int value)
	{
		this.total = value;
	}
	

	private int available;
	
	@DatabaseGetter(name = "available")
	public int getAvailable()
	{
		return available;
	}
	
	@TextImportSetter(name = "lots_available")
	@DatabaseSetter(name = "available")
	public void setAvailable(int value)
	{
		this.available = value;
	}
	
	
	private LocalDateTime lastUpdated;
	
	@DatabaseGetter(name = "lastUpdated")
	public LocalDateTime getLastUpdated()
	{
		return lastUpdated;
	}
	
	@TextImportSetter(name = "update_datetime")
	@DatabaseSetter(name = "lastUpdated")
	public void setLastUpdated(LocalDateTime value)
	{
		this.lastUpdated = value;
	}
}
