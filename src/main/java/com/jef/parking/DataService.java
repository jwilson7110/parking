package com.jef.parking;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class DataService <T> 
{	
	
	private Connection connection;
	private Class<T> type;
	
	private boolean autoCloseConnection = true;
	
	public DataService (Class<T> type)
	{
		this.type = type;
	}
	
	public Connection newConnection() 
	{
		return DataManager.newConnection();
		
	}
	
	public Connection getConnection () 
	{
		
		if (connection == null) 
		{
			connection = newConnection();
			
		}
		
		return connection;
	}
	
	public void closeConnection ()
	{
		try
		{		
			getConnection().close();
			connection = null;
		}catch (Exception e) {e.printStackTrace();}
	}
	
	public PreparedStatement prepareStatement(String command, ArrayList <Object> parameters)
	{
		try
		{
			var result = getConnection().prepareStatement(command);
			
			if (parameters != null)
			{
				
				for (var i = 0; i < parameters.size(); ++i)
				{
					//System.out.println(parameters.get(i).toString());
					result.setObject(i + 1, parameters.get(i));
				}
			}
			
			System.out.println(command);
			
			return result;
		
		} catch (Exception e) {e.printStackTrace();}
		
		return null;
	}
	
	public PreparedStatement prepareStatement(String command)
	{
		return prepareStatement(command, null);
	}
	
	public ResultSet query(String command, ArrayList <Object> parameters)
	{
		
		try 
		{
			var statement =  prepareStatement(command,parameters);
			var result = statement.executeQuery();
			
			return result;
			
		} catch (SQLException e) {e.printStackTrace();}
			
		return null;
	}
	
	public ResultSet query(String command)
	{
		return query (command, null);
	}
	
	
	
	/*
	public Type getGenericType ()
	{
		if (type == null)
		{
			this.type = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		}
		
		return type;
	}*/
	
	public T newItem () 
	{
		try
		{	
			return this.type.getConstructor(null).newInstance(null);
		} catch (Exception e) {e.printStackTrace();}
		
		return null;
	}
	
	public HashMap <String, Object> convertResultSetToMap (ResultSet resultSet)
	{
		try
		{
			var result = new HashMap <String, Object> ();
			
			for (var method : type.getMethods())
			{
				var setter = method.getAnnotation(DatabaseSetter.class);
				
				if (setter == null) 
				{
					continue;
				}
				
				var value = resultSet.getObject(setter.name());
				if (resultSet.wasNull())
				{
					value = null;
				}
				
				result.put(setter.name(), value);
			}
			
			return result;
		} catch (Exception e) {e.printStackTrace();}
		
		return null;
	}
	
	public ArrayList <HashMap <String, Object>> convertResultSetToMapList (ResultSet resultSet)
	{
		var result = new ArrayList <HashMap <String, Object>> ();
		
		try {
			while (resultSet.next())
			{
				result.add(convertResultSetToMap(resultSet));
			}
		} catch (SQLException e) {e.printStackTrace();}
		
		return result;
	}
	
	public ArrayList <HashMap<String, Object>> queryMapList (String command, ArrayList <Object> parameters)
	{
		var resultSet = query (command, parameters);
		
		var result = convertResultSetToMapList(resultSet);
		
		try {
			resultSet.close();
		} catch (SQLException e) {e.printStackTrace();}
		
		if (this.autoCloseConnection)
		{
			this.closeConnection();
		}
		
		return result;		
	}
	
	public ArrayList <HashMap<String, Object>> queryMapList (String command)
	{
		return queryMapList (command, null);
	}
	
	public T convertResultSetToObject (ResultSet resultSet)
	{	
		try
		{
			T result = newItem();
			
			for (var method : result.getClass().getMethods())
			{
				var setter = method.getAnnotation(DatabaseSetter.class);
				
				
				if (setter == null) 
				{
					continue;
				}
				
				var value = resultSet.getObject(setter.name());
				if (resultSet.wasNull())
				{
					value = null;
				}
				
				
				if (value instanceof Float)
				{
					value = ((Float)value).doubleValue();
				}
				
				else if (value instanceof BigDecimal)
				{
					value = ((BigDecimal)value).doubleValue();
				}
				else if (value instanceof Timestamp)
				{
					value = ((Timestamp)value).toLocalDateTime();
				}
				
				try 
				{
					method.invoke(result, value);
				}
				catch (Exception e)
				{
					String valueString = null;
					String valueType = "";
					
					if (value != null)
					{
						valueString = value.toString();
						valueType = value.getClass().getCanonicalName();
					}
					
					
					System.err.println("method: " + setter.name() + ", value: " + valueString + ", type: " + valueType);
					e.printStackTrace();
				}
			}
			
			return result;
			
		} catch (Exception e) {e.printStackTrace();}
		
		return null;
	}
	
	public ArrayList <T> convertResultSetToObjectList (ResultSet resultSet)
	{
		var result = new ArrayList <T> ();
		
		try 
		{
			while (resultSet.next())
			{
				result.add(convertResultSetToObject(resultSet));
			}
		} catch (SQLException e) {e.printStackTrace();}
		
		return result;
	}
	
	public ArrayList <T> queryObjectList (String command, ArrayList <Object> parameters)
	{
		var resultSet = query (command, parameters);		
		
		var result = convertResultSetToObjectList(resultSet);
		
		try {
			resultSet.close();
		} catch (SQLException e) {e.printStackTrace();}
		
		if (this.autoCloseConnection)
		{
			this.closeConnection();
		}
		
		return result;
	}
	
	public ArrayList <T> queryObjectList (String command)
	{
		return queryObjectList (command, null);
	}
	
	
	public HashMap <String, Object> convertObjectToMap (T obj)
	{
		var result = new HashMap <String, Object> ();
		
		for (var method : obj.getClass().getMethods())
		{
			var getter = method.getAnnotation(DatabaseGetter.class);
			
			if (getter == null) 
			{
				continue;
			}
			
			try
			{
				result.put(getter.name(), method.invoke(obj, new Object [] {}));				
			}
		
			catch (Exception e) 
			{
				System.err.println("method: " + getter.name());
				e.printStackTrace();
			}
		}
		
		return result;
	}
	
	
	
	public void update(HashMap <String, Object> map)
	{
		var table = type.getAnnotation(DatabaseTable.class); 
		var command = "update " + table.name() + " set ";
		var parameters = new ArrayList<Object>();
		
		Object primaryKey = null;
		var first = true;
		
		for (var key : map.keySet()) 
		{
			var value = map.get(key);
			
			if (key.equals(table.primaryKey()))
			{
				primaryKey = (UUID)value;
				continue;
			}
			
			if (!first) 
			{
				command += ", ";				
			}
			
			first = false;
			
			command += key + " = ?";
			parameters.add(value);
		}
		
		if (primaryKey == null)
		{
			return;
		}
		
		command += " where " + table.primaryKey() + " = ?";
		parameters.add(primaryKey);
		
		var statement = prepareStatement(command, parameters);
		
		try
		{
			statement.execute();
			statement.close();
		}
		catch (Exception e) {e.printStackTrace();}
		
		if (this.autoCloseConnection)
		{
			this.closeConnection();
		}
	}
	
	public void update(T item)
	{
		update(convertObjectToMap(item));
	}
	
	public void insert(HashMap <String, Object> map)
	{
		var table = type.getAnnotation(DatabaseTable.class); 
		var command = "insert into " + table.name();
		var parameters = new ArrayList<Object>();
		
		var columns = " (";
		var values = " values (";		
		
		var first = true;
		
		if (!map.containsKey(table.primaryKey()))
		{
			map.put(table.primaryKey(), UUID.randomUUID());
		}
		
		if (map.get(table.primaryKey()) == null)
		{
			map.put(table.primaryKey(), UUID.randomUUID());
		}
		
		
		for (var key : map.keySet())
		{
			if (!first)
			{
				columns += ", ";
				values += ", ";
			}
			
			first = false;
			
			var value = map.get(key);			
			
			columns += key;
			values += "?";
			parameters.add(value);
		}
		
		columns += ")";
		values += ")";
		
		var statement = prepareStatement(command + columns + values, parameters);
		
		try
		{
			statement.execute();
			statement.close();
		}
		catch (Exception e) {e.printStackTrace();}
		
		if (this.autoCloseConnection)
		{
			this.closeConnection();
		}
	}
	
	public void insert(T item)
	{
		insert(convertObjectToMap(item));
	}
	
	public void save(HashMap <String, Object> map)
	{
		var table = type.getAnnotation(DatabaseTable.class); 
		if (map.get(table.primaryKey()) == null)
		{
			insert(map);			
		}
		else 
		{
			update (map);
		}
	}
	
	public void save (T item)
	{
		save(convertObjectToMap(item));
	}

}
