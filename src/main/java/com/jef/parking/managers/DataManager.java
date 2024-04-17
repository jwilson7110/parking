package com.jef.parking.managers;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;

import com.jef.parking.Config;

public class DataManager //establishes database connection pool and provides connections to database
{
	
	public static BasicDataSource dataSource;
	
	
	static 
	{
		DataSourceBuilder<BasicDataSource> dataSourceBuilder = (DataSourceBuilder<BasicDataSource>) DataSourceBuilder.create();
		dataSourceBuilder.driverClassName("org.postgresql.Driver");
		dataSourceBuilder.url("jdbc:postgresql://" + Config.getValue("spring.datasource.host") + ":" + Config.getValue("spring.datasource.port") + "/parking");
		dataSourceBuilder.username(Config.getValue("spring.datasource.username"));
		dataSourceBuilder.password(Config.getValue("spring.datasource.password"));
		
		dataSource = dataSourceBuilder.build();
		dataSource.setMinIdle(5);
		dataSource.setMaxIdle(10);
		dataSource.setMaxTotal(25);
		
		try 
		{
			System.out.print(dataSource.getConnection());
		} 
		catch (SQLException e) 
		{
			System.err.println("Could Not establish database connection");
			e.printStackTrace();
		}
	}
	
	public static Connection newConnection () 
	{
		try
		{ 
			return dataSource.getConnection();
		} 
		catch (Exception e) 
		{
			System.err.println("Could Not create new database connection");
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	public static Statement newStatement (Connection connection)
	{
		if (connection == null) //allow an existing connection to be used 
		{
			connection = newConnection();
		}
		
		try
		{
			return connection.createStatement();
		} 
		catch (Exception e) 
		{
			System.err.println("Could Not create new database statement");
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static Statement newStatement ()
	{
		 return newStatement(null);
	}
	
}
