package com.jef.parking.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME) 
public @interface DatabaseGetter //maps a function of a class to a database column for inserting and updating from application to database
{
	String name();//name of the database column
}
