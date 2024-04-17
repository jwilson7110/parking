package com.jef.parking.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME) 
public @interface DatabaseTable //maps a class to a database table
{
	
	String name();//name of the database table
	
	//name of the primary key field, used in conjunction with DatabaseSetter and DatabaseGetter annotionation in order to build sql commands
	//TODO: allow multiple primary key fields
	String primaryKey() default "id"; 
		

}
