package com.jef.parking.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME) 
public @interface TextImportSetter //maps the key from an external data source (csv file, json rest endpoint, etc.) to a class setter method 
{
	//name of the key from the external source
	//TODO: allow multiple names
	String name();
}
