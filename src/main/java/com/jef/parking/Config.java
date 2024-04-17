package com.jef.parking;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.jef.parking.kafka.MessageProducer;

import jakarta.annotation.PostConstruct;

@Configuration
public class Config
{
	
	@Autowired
	public Environment env;
	
	private static HashMap <String, String> values = new HashMap <> ();
	
	@PostConstruct
	public void postConstruct () //runs after object is initialzed
	{
		var keys = new String [] {
			"spring.datasource.host", //database host
			"spring.datasource.port", //database port
			"spring.datasource.username", //database username
			"spring.datasource.password", //database password
			"onemap.token", //oauth token to access one map
			"lots.file"//path of default file to import lot data from 
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
