package com.jef.parking.data.services;

import java.math.BigDecimal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import com.jef.parking.annotations.DatabaseGetter;
import com.jef.parking.annotations.DatabaseSetter;
import com.jef.parking.annotations.DatabaseTable;
import com.jef.parking.data.Data;
import com.jef.parking.managers.DataManager;

public class DataService <T extends Data> 
{	
	
	private Connection connection;
	private Class<T> type;
	
	private boolean autoCloseConnection = true;
	
	protected HashMap <String, Class> dataTypes = new HashMap <String, Class> ();//TODO: map up all columns on start
	

	
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
		PreparedStatement result = null;
		
		try
		{
			result = getConnection().prepareStatement(command);
		}
		catch (Exception e)
		{
			System.err.println("Count not prepare Statement for Command: " + command);
			e.printStackTrace();
		}
		
		if (parameters != null)
		{
			
			for (var i = 0; i < parameters.size(); ++i)
			{
				try
				{
					result.setObject(i + 1, parameters.get(i));
				}
				catch (Exception e)
				{
					System.err.println("Could not add Parameter (" + i + ": " + parameters.get(i) + ") to command: " + command);
					e.printStackTrace();
				}
				
			}
		}
		
		return result;
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
		var result = new HashMap <String, Object> ();
		
		for (var method : type.getMethods())
		{
			var setter = method.getAnnotation(DatabaseSetter.class);
			
			if (setter == null) 
			{
				continue;
			}				
			
			try
			{
				result.put(setter.name(), resultSet.getObject(setter.name()));
			}
			catch (Exception e){} //this is alright, we are not expecting every column to be in the result set
		}
		
		return result;
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

	
	public T convertMapToObject (HashMap <String, Object> data)
	{
		var result = newItem();
		
		for (var method : result.getClass().getMethods())
		{
			var setter = method.getAnnotation(DatabaseSetter.class);
			
			if (setter == null) 
			{
				continue;
			}
			
			if (!data.containsKey(setter.name()))
			{
				continue;
			}
			
			var value = data.get(setter.name());
			
			try
			{
				method.invoke(data.get(setter.name()));	
			}
		
			catch (Exception e) 
			{
				System.err.println("method: " + setter.name() + " value: " + value + " valueType: " + value.getClass().getName());
				e.printStackTrace();
			}
		}
		
		return result;
	}
	
	public ArrayList <String> compileInsertColumnList (HashMap <String, Object> data)
	{
		var result = new ArrayList <String> ();
		
		for (String columnName : data.keySet())	
		{
			result.add(columnName);
		}
		
		return result;
	}
	
	public String compileInsertColumnString (ArrayList <String> columnList)
	{
		return "(" + String.join(",", columnList.toArray(new String [columnList.size()]))  + ")";
	}
	
	public String compileInsertItemValueString  (HashMap <String, Object> data, ArrayList<String> columnList, ArrayList <Object> parameters)
	{
		var result = "values (";
		
		var first = true;
		
		for (var columnName : columnList)
		{
			var value = data.get(columnName);
			
			if (!first)
			{
				result += ",";
			}
			
			first = false;
			result += "?";
			parameters.add(value);
		}
		
		return result + ")";
	}
	
	public void insert (HashMap <String, Object> data)
	{		
		if (data.isEmpty())
		{
			return;
		}
		
		var table = type.getAnnotation(DatabaseTable.class);
		
		var parameters = new ArrayList<Object>();
		
		var columnList = this.compileInsertColumnList (data);
		
		var primaryKey = data.getOrDefault(table.primaryKey(), null);
		if (primaryKey == null)
		{
			data.put(table.primaryKey(), UUID.randomUUID());
		}
		
		var commandText = "insert into " + table.name() + " " + compileInsertColumnString(columnList) + " " + compileInsertItemValueString(data, columnList, parameters) + ";";
		
		var statement = prepareStatement(commandText, parameters);
		
		try
		{
			statement.execute();
			statement.close();
		}
		catch (Exception e) {
			System.err.println("Error executing command: " + commandText);
			System.err.println("Parameters: " + parameters.size());
			
			var i = 1;
			for (var parameter : parameters)
			{
				System.err.println("\t" + i + ": " + parameter);
				++i;
			}
			
			e.printStackTrace();
		}
		
		if (this.autoCloseConnection)
		{
			this.closeConnection();
		}
	}
	
	public void insert(T object)
	{
		insert(convertObjectToMap(object));
	}
	
	public void insert (ArrayList <HashMap <String, Object>> data)
	{
		var autoClose = this.autoCloseConnection;
		this.autoCloseConnection = false;
		
		for (var item : data)
		{
			insert (item);
		}
		
		if (autoClose)
		{
			this.closeConnection();
		}
		
		this.autoCloseConnection = autoClose;
	}
	
	public void insertObjects (ArrayList <T> data)
	{
		var mapList = new ArrayList <HashMap <String, Object>> ();
		for (var item : data)
		{
			mapList.add(convertObjectToMap(item));
		}
		
		insert(mapList);
	}
	
	public String compileUpdateItemValueString(HashMap <String, Object> data, ArrayList <Object> parameters)
	{
		var result = "";
		var table = type.getAnnotation(DatabaseTable.class);
		
		var first = true;
		
		for (var columnName : data.keySet())
		{
			if (columnName == table.primaryKey())
			{
				continue;
			}
			
			if (!first)
			{
				result += ", ";
			}
			first = false;
			
			result += columnName + " = ?";
			parameters.add(data.get(columnName));
		}
		
		return result;
	}
	
	
	public void update (HashMap <String, Object> data)	
	{
		if (data.isEmpty())
		{
			return;
		}
		
		var table = type.getAnnotation(DatabaseTable.class);
		
		var primaryKey = data.getOrDefault(table.primaryKey(), null);
		
		if (primaryKey == null)
		{
			return;
		}
		
		var parameters = new ArrayList<Object>();
		
		var commandText = "update " + table.name() + " set " + this.compileUpdateItemValueString(data, parameters) + " where id = ?";
		
		parameters.add(primaryKey);
		
		var statement = prepareStatement(commandText, parameters);
		
		try
		{
			statement.execute();
			statement.close();
		}
		catch (Exception e) 
		{
			System.err.println("Error executing command: " + commandText);
			System.err.println("Parameters: " + parameters.size());
			
			var i = 1;
			for (var parameter : parameters)
			{
				System.err.println("\t" + i + ": " + parameter);
				++i;
			}
			
			e.printStackTrace();
		}
		
		if (this.autoCloseConnection)
		{
			this.closeConnection();
		}
	}
	
	public void update(T data)
	{
		update(convertObjectToMap(data));
	}
	
	public void update (ArrayList <HashMap <String, Object>> data)
	{
		var autoClose = this.autoCloseConnection;
		this.autoCloseConnection = false;
		
		for (var item : data)
		{
			update (item);
		}
		
		if (autoClose)
		{
			this.closeConnection();
		}
		
		this.autoCloseConnection = autoClose;
	}
	
	public void updateObjects (ArrayList <T> data)
	{
		var mapList = new ArrayList <HashMap <String, Object>> ();
		for (var item : data)
		{
			mapList.add(convertObjectToMap(item));
		}
		
		update(mapList);
	}
	
	public void save (HashMap <String, Object> data)
	{
		var table = type.getAnnotation(DatabaseTable.class);		
		
		var primaryKey = data.getOrDefault(table.primaryKey(), null);
		if (primaryKey == null)
		{
			insert(data);
		}
		else 
		{
			update(data);
		}		
	}
	
	
	public void save(T data)
	{
		save(convertObjectToMap(data));
	}
	
	
	public void save (ArrayList <HashMap <String, Object>> data)
	{
		var autoClose = this.autoCloseConnection;
		this.autoCloseConnection = false;
		
		for (var item : data)
		{
			save (item);
		}
		
		if (autoClose)
		{
			this.closeConnection();
		}
		
		this.autoCloseConnection = autoClose;
	}
	
	public void saveObjects (ArrayList <T> data)
	{
		var mapList = new ArrayList <HashMap <String, Object>> ();
		for (var item : data)
		{
			mapList.add(convertObjectToMap(item));
		}
		
		save(mapList);
	}
	
	
	/*
	 
	public void upsert ()
	{
		var data = this.data;
		var table = type.getAnnotation(DatabaseTable.class); 
		var command = "insert into " + table.name();
		var parameters = new ArrayList<Object>();
		
		var columnList = this.compileInsertColumnList(data);		
		
		var columns = " (" + String.join(",", columnList.toArray(new String [columnList.size()])) + ")";
		
		var values = " values ";		
		
		var first = true;
		for (var item : data)
		{
			if (!item.containsKey(table.primaryKey()))
			{
				item.put(table.primaryKey(), UUID.randomUUID());
			}
			
			if (item.get(table.primaryKey()) == null)
			{
				item.put(table.primaryKey(), UUID.randomUUID());
			}
			
			if (!first) 
			{
				values += ",";
			}
			
			first = false;
			
			var dataString = this.compileInsertItemValueList(item, columnList, parameters);
			if (dataString == null)
			{
				continue;
			}
			
			values += dataString;
		}
		
		var statement = prepareStatement(command + columns + values + "on conflict", parameters);
		
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
	}*/
	
	/*public String compileUpdateColumnString (ArrayList <String> columnList)
	{
		String result = "";
		
		var first = true;
		
		for (String columnName : columnList)
		{
			if (!first)
			{
				result += ",";
			}
			first = false;
			
			result += columnName += " = c." + columnName; 
		}
		
		return result;
	}
	
	
	public void update (ArrayList <HashMap <String, Object>> data)	
	{
		if (data.isEmpty())
		{
			return;
		}
		
		var table = type.getAnnotation(DatabaseTable.class);
		
		var parameters = new ArrayList<Object>();
		
		var columnList = this.compileColumnList (data);
		var commandText = "update " + table.name() + " as t set " + this.compileUpdateColumnString(columnList) + " from (" + this.compileValueString(data, columnList, parameters) + ") as c" + this.compileColumnString(columnList) + " where c." + table.primaryKey() + " = t." + table.primaryKey() + ";";
		
		var statement = prepareStatement(commandText, parameters);
		
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
	
	public void update (HashMap <String, Object> data)
	{
		var list = new ArrayList <HashMap <String, Object>> ();
		list.add(data);
		update (list);
	}
	
	public void update(T item)
	{
		update(convertObjectToMap(item));
	}*/
	
	/*public void insert(HashMap <String, Object> map)
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
	}*/
	
	/*
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
	}*/
	
	/*public void insert (ArrayList <HashMap <String, Object>> data)
	{
		if (data.isEmpty())
		{
			return;
		}
		
		
		var table = type.getAnnotation(DatabaseTable.class);
		
		var parameters = new ArrayList<Object>();
		
		for (var item : data)
		{
			var primaryKey = item.getOrDefault(table.primaryKey(), null);
			if (primaryKey == null)
			{
				item.put(table.primaryKey(), UUID.randomUUID());
			}
		}
		
		var columnList = this.compileColumnList (data);
		var commandText = "insert into " + table.name() + " " + this.compileColumnString(columnList) + " " + this.compileValueString(data, columnList, parameters);	
		
		var statement = prepareStatement(commandText, parameters);
		
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
	
	
	
	
	public void insert (HashMap <String, Object> data)
	{
		var list = new ArrayList <HashMap <String, Object>> ();
		list.add(data);
		insert (list);
	}
	
	public void insert(T item)
	{
		insert(convertObjectToMap(item));
	}*/

	/*public String compileInsertItemValueString (ArrayList <HashMap <String, Object>> data, ArrayList <String> columnList, ArrayList <Object> parameters)
	{
		var result = "values ";
		
		var first = true;
		for (var item : data)
		{
			if (!first)
			{
				result += ",";				
			}
			first = false;
			
			result += this.compileItemValueString (item, columnList, parameters);
					
		}
		
		return result;
	}*/
	
	/*public String compileInsertItemValueString  (HashMap <String, Object> data, ArrayList<String> columnList, ArrayList <Object> parameters)
	{
		var result = "values (";
		
		var first = true;
		
		for (var columnName : columnList)
		{
			if (!data.containsKey(columnName))
			{
				return null;
			}
			
			var value = data.get(columnName);
			
			if (!first)
			{
				result += ",";
			}
			
			first = false;
			
			
			var flag = false; // I Know I Know
			if (value == null)
			{
				if (dataTypes.containsKey(columnName))
				{
					var clas = dataTypes.get(columnName);
					if (clas == UUID.class)
					{
						result += "cast(null as uuid)";
						flag = true;
					}
				}
			}
			
			if (flag == false)
			{
				result += "?";
				parameters.add(value);
			}		
			
			
			System.out.println(columnName + ": " + value);
		}
		
		return result + ")";
	}*/
	
	/*public void update (HashMap <String, Object> data)
	{
		var list = new ArrayList <HashMap <String, Object>> ();
		list.add(data);
		update (list);
	}
	
	public void update(T item)
	{
		update(convertObjectToMap(item));
	}*/
	
	/*public String compileUpdateColumnString (ArrayList <String> columnList)
	{
		String result = "";
		
		var first = true;
		
		for (String columnName : columnList)
		{
			if (!first)
			{
				result += ",";
			}
			first = false;
			
			result += columnName += " = c." + columnName; 
		}
		
		return result;
	}
	
	
	public void update (ArrayList <HashMap <String, Object>> data)	
	{
		if (data.isEmpty())
		{
			return;
		}
		
		var table = type.getAnnotation(DatabaseTable.class);
		
		var parameters = new ArrayList<Object>();
		
		var columnList = this.compileColumnList (data);
		var commandText = "update " + table.name() + " as t set " + this.compileUpdateColumnString(columnList) + " from (" + this.compileValueString(data, columnList, parameters) + ") as c" + this.compileColumnString(columnList) + " where c." + table.primaryKey() + " = t." + table.primaryKey() + ";";
		
		var statement = prepareStatement(commandText, parameters);
		
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
	}*/

	/*public void update (HashMap <String, Object> data)
	{
		var list = new ArrayList <HashMap <String, Object>> ();
		list.add(data);
		update (list);
	}
	
	public void update(T item)
	{
		update(convertObjectToMap(item));
	}*/
	
	/*public String compileUpdateColumnString (ArrayList <String> columnList)
	{
		String result = "";
		
		var first = true;
		
		for (String columnName : columnList)
		{
			if (!first)
			{
				result += ",";
			}
			first = false;
			
			result += columnName += " = c." + columnName; 
		}
		
		return result;
	}*/
	
}
