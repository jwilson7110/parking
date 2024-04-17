package com.jef.parking;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import jakarta.annotation.PostConstruct;

@Configuration
public class Config
{
	
	@Autowired
	public Environment env;
	
	private static HashMap <String, String> values = new HashMap <> ();
	
	@PostConstruct
	public void postConstruct ()
	{
		var keys = new String [] {
			"spring.datasource.host",
			"spring.datasource.port",
			"spring.datasource.username",
			"spring.datasource.password",
			"onemap.token",
			"lots.file"
		};
		
		for (var key : keys)
		{
			values.put(key, env.getProperty(key));
		}
	}
	
	public static String getValue(String key)
	{
		return values.get(key);
	}
}
