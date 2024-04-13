package com.jef.parking;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME) 
public @interface DatabaseGetter 
{
	String name();
}
