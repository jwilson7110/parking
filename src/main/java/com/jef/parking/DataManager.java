package com.jef.parking;

import java.sql.Connection;
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;

public class DataManager 
{
	public static BasicDataSource dataSource;
	
	static 
	{
		DataSourceBuilder<BasicDataSource> dataSourceBuilder = (DataSourceBuilder<BasicDataSource>) DataSourceBuilder.create();
		//dataSourceBuilder.driverClassName("cdata.jdbc.postgresql.PostgreSQLDriver");
		dataSourceBuilder.driverClassName("org.postgresql.Driver");
		//dataSourceBuilder.url("jdbc:postgresql:Database=postgres;Server=127.0.0.1;Port=5432;");
		//dataSourceBuilder.url("jdbc:postgresql://localhost:5432/parking?user=postgres&password=cpop0522");
		dataSourceBuilder.url("jdbc:postgresql://localhost:5432/parking");
		dataSourceBuilder.username("postgres");
		dataSourceBuilder.password("cpop0522");
		
		dataSource = dataSourceBuilder.build();
		dataSource.setMinIdle(5);
		dataSource.setMaxIdle(10);
		dataSource.setMaxTotal(25);
		
		
	}
	
	public static Connection newConnection () 
	{
		try
		{
			return dataSource.getConnection();
		} catch (Exception e) {}
		
		return null;
	}
	
	
	public static Statement newStatement (Connection connection)
	{
		if (connection == null) {
			connection = newConnection();
		}
		
		try
		{
			return connection.createStatement();
		} catch (Exception e) {}
		
		return null;
	}
	
	public static Statement newStatement ()
	{
		 return newStatement(null);
	}
	
}
