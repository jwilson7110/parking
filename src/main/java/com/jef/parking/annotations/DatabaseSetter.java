package com.jef.parking.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME) 
public @interface DatabaseSetter //maps a function of a class to a database column for querying from database to application
{
	String name();//name of the database column
}
