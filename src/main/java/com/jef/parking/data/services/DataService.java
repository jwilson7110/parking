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

public class DataService <T extends Data> //primary use is to map objects to database rows
{	
	
	private Connection connection;//allows us to hold onto a connection for multiple queries
	private Class<T> type;
	
	private boolean autoCloseConnection = true;//when false, we have to close the connection on our own
	
	protected HashMap <String, Class> dataTypes = new HashMap <String, Class> ();//TODO: map up all columns on start
	
	public DataService (Class<T> type)
	{
		this.type = type;//stores the class representing the database table we are mapping to, must be the same as T. TODO: automate this process, i swear ive done it before
	}
	
	public Connection newConnection() 
	{
		return DataManager.newConnection();
	}
	
	public Connection getConnection () //get the connection we are holding onto or get a new connection and hold onto it 
	{		
		if (connection == null) 
		{
			connection = newConnection();
			
		}
		
		return connection;
	}
	
	public void closeConnection ()//close the connection we are holding onto and set our reference to null so the next time we try to get a new connection, we get a new one instead of the one we closed
	{
		try
		{		
			getConnection().close();
			connection = null;
		}		
		catch (Exception e) 
		{
			System.err.println("Connection could not be close");
			e.printStackTrace();
		}
	}
	
	public void printCommandExecutionError(String command, ArrayList <Object> parameters) //prints common command data on error 
	{
		if (command != null) //command text may not have been passed in to function executing the command
		{
			System.err.println("Quering result set failed, command text: " + command);
			if (parameters != null) //we dont always have parameters
			{
				System.err.println("Parametrers: ");
				for (var i = 0; i < parameters.size(); ++i)
				{
					System.err.println("\t" + i + 1 + ": " + parameters.get(i));
				}
			}
		}
		
	}
	
	public PreparedStatement prepareStatement(String command, ArrayList <Object> parameters) //establish a statement object and supply the parameters
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
			return result;
		}
		
		if (parameters != null)
		{
			for (var i = 0; i < parameters.size(); ++i)
			{
				try
				{
					result.setObject(i + 1, parameters.get(i)); //statement parameters start at index 1
				}
				catch (Exception e)
				{
					System.err.println("Could not add Parameter (" + i + ": " + parameters.get(i) + ") to command: " + command);
					e.printStackTrace();
					return null;
				}
				
			}
		}
		
		return result;
	}
	
	public PreparedStatement prepareStatement(String command) //shortcut for when we have no parameters
	{
		return prepareStatement(command, null);
	}
	
	public ResultSet query(String command, ArrayList <Object> parameters)//get a result set for a query and supplied parameters  
	{		
		try 
		{
			var statement =  prepareStatement(command,parameters);
			if (statement == null)
			{
				return null;
			}
				
			var result = statement.executeQuery();
			
			return result;
			
		} 
		catch (SQLException e)		
		{
			System.err.println("Quering result set failed");
			printCommandExecutionError(command, parameters);
			e.printStackTrace();
		}
			
		return null;
	}
	
	public ResultSet query(String command)//shortcut for when we have no parameters
	{
		return query (command, null);
	}
	
	
	public T newItem () //get new instance of T
	{
		T result = null;
		
		try
		{	
			result = this.type.getConstructor(null).newInstance(new Object [] {}); //WARNING: this requires us to have a constructor with no parameters
		} 
		catch (Exception e) 
		{
			System.err.println("Could not create new instance of " + type.getName());
			e.printStackTrace();
		}
		
		return result;
	}
	
	
	
	public HashMap <String, Object> convertResultSetToMap (ResultSet resultSet, String command, ArrayList <Object> parameters)//maps a result set item to a hashmap using T's DatabaseSetter annotations
	{
		var result = new HashMap <String, Object> ();
		
		for (var method : type.getMethods())
		{
			var setter = method.getAnnotation(DatabaseSetter.class);
			
			if (setter == null) //not every method is a setter
			{
				continue;
			}				
			
			try
			{
				result.put(setter.name(), resultSet.getObject(setter.name()));
			}
			catch (SQLException e){} //this is alright, we are not expecting every column to be in the result set
			catch (Exception e)
			{
				System.err.println("Could not map result set");
				printCommandExecutionError(command, parameters);
				e.printStackTrace();
				//we report the error keep setting values
			}
		}
		
		return result;
	}
	
	public HashMap <String, Object> convertResultSetToMap(ResultSet resultSet)//shortcut in case we are not supplying the command text and parameters, this only means error reporting will not be as verbose
	{
		return convertResultSetToMap(resultSet, null, null);
	}
	
	public ArrayList <HashMap <String, Object>> convertResultSetToMapList (ResultSet resultSet, String command, ArrayList <Object> parameters) //maps a result set to a list of hashmap using T's DatabaseSetter annotations 
	{
		var result = new ArrayList <HashMap <String, Object>> ();
		
		try 
		{
			while (resultSet.next())
			{
				result.add(convertResultSetToMap(resultSet));
			}
		} 
		catch (SQLException e) 
		{
			System.err.println("Could not map result set");
			printCommandExecutionError(command, parameters);
			e.printStackTrace();
			//we report the error keep setting values
		}
		
		return result;
	}
	
	public ArrayList <HashMap <String, Object>> convertResultSetToMapList (ResultSet resultSet) //shortcut in case we are not supplying the command text and parameters, this only means error reporting will not be as verbose
	{
		return convertResultSetToMapList(resultSet, null, null);
	}
	
	
	public ArrayList <HashMap<String, Object>> queryMapList (String command, ArrayList <Object> parameters)//query and return a set of HashMaps
	{
		var resultSet = query (command, parameters);
		
		var result = convertResultSetToMapList(resultSet, command, parameters);
		
		try 
		{
			resultSet.close();
		} 
		catch (SQLException e) 
		{
			System.err.println("Could not close result set for command " + command);
			e.printStackTrace();
		}
		
		if (this.autoCloseConnection)
		{
			this.closeConnection();
		}
		
		return result;		
	}
	
	public ArrayList <HashMap<String, Object>> queryMapList (String command)//shortcut when we dont have parameters
	{
		return queryMapList (command, null);
	}
	
	public T convertResultSetToObject (ResultSet resultSet, String command, ArrayList <Object> parameters) //maps a result set item to a T using T's DatabaseSetter annotations
	{	
		T result = newItem();
		
		for (var method : result.getClass().getMethods()) //we got though each method in the class looking for database setters
		{
			var setter = method.getAnnotation(DatabaseSetter.class);
			
			
			if (setter == null) //not every method is a setter
			{
				continue;
			}
			
			Object value = null;
			
			try
			{
				value = resultSet.getObject(setter.name());
				if (resultSet.wasNull())
				{
					value = null;
				}
			}
			catch (Exception e)
			{
				System.err.println("Could not retrieve field (" + setter.name() + ") from result set");
				printCommandExecutionError(command, parameters);
				e.printStackTrace();
				continue;//we report the error keep setting values
			}
				
			//TODO: more conversions, we only currently ones for fields we are using
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
				method.invoke(result, value);//call the setter
			}
			catch (Exception e)//we keep setting after this and return what we got
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
	}
	
	public T convertResultSetToObject (ResultSet resultSet) //maps a result set item to a T using T's DatabaseSetter annotations
	{
		return convertResultSetToObject (resultSet, null, null);
	}
	
	public ArrayList <T> convertResultSetToObjectList (ResultSet resultSet, String command, ArrayList <Object> parameters)//maps a result set to a list of Ts using T's DatabaseSetter annotations 
	{
		var result = new ArrayList <T> ();
		
		try 
		{
			while (resultSet.next())
			{
				result.add(convertResultSetToObject(resultSet));
			}
		} 
		catch (SQLException e) 
		{
			System.err.println("Could not map result set");
			printCommandExecutionError(command, parameters);
			e.printStackTrace();
			//we report the error keep setting values
		}
		
		return result;
	}
	
	public ArrayList <HashMap <String, Object>> convertResultSetToObjectList (ResultSet resultSet) //shortcut in case we are not supplying the command text and parameters, this only means error reporting will not be as verbose
	{
		return convertResultSetToMapList(resultSet, null, null);
	}
	
	public ArrayList <T> queryObjectList (String command, ArrayList <Object> parameters)//query and return a set of HashMaps
	{
		var resultSet = query (command, parameters);		
		
		var result = convertResultSetToObjectList(resultSet, command, parameters);
		
		try 
		{
			resultSet.close();
		} 
		catch (SQLException e) {
			System.err.println("Could not close result set for command " + command);
			e.printStackTrace();
		}
		
		if (this.autoCloseConnection)
		{
			this.closeConnection();
		}
		
		return result;
	}
	
	public ArrayList <T> queryObjectList (String command)//shortcut when we dont have parameters
	{
		return queryObjectList (command, null);
	}
	
	
	public HashMap <String, Object> convertObjectToMap (T obj) //convert an object representing a database row to a hashmap
	{
		var result = new HashMap <String, Object> ();
		
		for (var method : obj.getClass().getMethods()) //go through every method in T and find all the DatabaseGetters
		{
			var getter = method.getAnnotation(DatabaseGetter.class);
			
			if (getter == null) //not every mothod is a getter
			{
				continue;
			}
			
			try
			{
				result.put(getter.name(), method.invoke(obj, new Object [] {})); //execute the getter and add it to the result using the column name as the key				
			}
		
			catch (Exception e) 
			{
				System.err.println("Could Not Execute Setter: " + getter.name());
				e.printStackTrace();
			}
		}
		
		return result;
	}

	
	public T convertMapToObject (HashMap <String, Object> data)//convert a hashmap to an object representing a database row 
	{
		var result = newItem();
		
		for (var method : result.getClass().getMethods())//go through every method in T and find all the DatabaseSetters
		{
			var setter = method.getAnnotation(DatabaseSetter.class);
			
			if (setter == null) 
			{
				continue;
			}
			
			if (!data.containsKey(setter.name()))//not every mothod is a setter
			{
				continue;
			}
			
			var value = data.get(setter.name());
			
			try
			{
				method.invoke(data.get(setter.name()));	//set the objects field using the value found in the map
			}
		
			catch (Exception e) 
			{
				System.err.println("Could not Execute Setter: " + setter.name() + " value: " + value + " valueType: " + value.getClass().getName());
				e.printStackTrace();
			}
		}
		
		return result;
	}
	
	public ArrayList <String> compileInsertColumnList (HashMap <String, Object> data) //get a list of all the column names for the insert command
	{
		var result = new ArrayList <String> ();
		
		for (String columnName : data.keySet())	//each key in the map should align to a column
		{
			result.add(columnName);
		}
		
		return result;
	}
	
	public String compileInsertColumnString (ArrayList <String> columnList) //get the command text for the set of columns in an sql command insert
	{
		return "(" + String.join(",", columnList.toArray(new String [columnList.size()]))  + ")";
	}
	
	//get the command text for the values of an sql command insert
	//these will likely be a set of comma seperated ?'s
	//a parameters list needs to be supplied as a reference
	public String compileInsertItemValueString  (HashMap <String, Object> data, ArrayList<String> columnList, ArrayList <Object> parameters) 
	{
		var result = "values (";
		
		var first = true;		
		for (var columnName : columnList)
		{
			var value = data.get(columnName);
			
			if (!first)//we dont add a comma to the first item
			{
				result += ",";
			}
			
			first = false;
			result += "?";
			parameters.add(value);
		}
		
		return result + ")";
	}
	
	public void insert (HashMap <String, Object> data) //execute an insert command using the map provided, only column names in the maps keys will be inserted
	{		
		if (data.isEmpty()) //nothing to save
		{
			return;
		}
		
		var table = type.getAnnotation(DatabaseTable.class); //get the table meta data from T's DatabaseTable
		
		var parameters = new ArrayList<Object>();
		
		var columnList = this.compileInsertColumnList (data); //get the command text of the columns
		
		var primaryKey = data.getOrDefault(table.primaryKey(), null); 
		if (primaryKey == null)
		{
			data.put(table.primaryKey(), UUID.randomUUID()); //provide a primary key if one doesnt currently exist TODO: add support for different types of primary keys and multiple primary keys
		}
		
		var commandText = "insert into " + table.name() + " " + compileInsertColumnString(columnList) + " " + compileInsertItemValueString(data, columnList, parameters) + ";";//build the command text
		
		var statement = prepareStatement(commandText, parameters);
		
		try
		{
			statement.execute();
			statement.close();
		}
		catch (Exception e) {
			System.err.println("Error executing insert command");
			printCommandExecutionError(commandText, parameters);
			e.printStackTrace();
		}
		
		if (this.autoCloseConnection)
		{
			this.closeConnection();
		}
	}
	
	public void insert(T object) //insert a T, converts to hashmap first, we use a map as a primary so we dont always have to provide a value for every column
	{
		insert(convertObjectToMap(object));
	}
	
	public void insert (ArrayList <HashMap <String, Object>> data) //insert multiple rows
	{
		var autoClose = this.autoCloseConnection; //store the current auto close value
		this.autoCloseConnection = false; //we will not be auto closing since we have multiple commands to run
		
		for (var item : data)
		{
			insert (item);
		}
		
		if (autoClose) //close if we were on auto close
		{
			this.closeConnection();
		}
		
		this.autoCloseConnection = autoClose; //reset auto close to its previous value
	}
	
	public void insertObjects (ArrayList <T> data)//insert a list of Ts, converts to hashmap first, we use a map as a primary so we dont always have to provide a value for every column
	{
		var mapList = new ArrayList <HashMap <String, Object>> ();
		for (var item : data)
		{
			mapList.add(convertObjectToMap(item));
		}
		
		insert(mapList);
	}
	
	//get the command text for the columns value pairs of an update command
	//these will likely be a set of comma seperated {{columnName = ?}}'s
	//a parameters list needs to be supplied as a reference
	public String compileUpdateItemValueString(HashMap <String, Object> data, ArrayList <Object> parameters)
	{
		var result = "";
		var table = type.getAnnotation(DatabaseTable.class);
		
		var first = true;
		
		for (var columnName : data.keySet())
		{
			if (columnName == table.primaryKey())//we dont update the primary key
			{
				continue;
			}
			
			if (!first) //we dont add a comma to the first item
			{
				result += ", ";
			}
			first = false;
			
			result += columnName + " = ?";
			parameters.add(data.get(columnName)); //add the value to the parameter list
		}
		
		return result;
	}
	
	
	public void update (HashMap <String, Object> data)	//execute an update command using the map provided, only column names in the maps keys will be updated
	{
		if (data.isEmpty())
		{
			return;
		}
		
		var table = type.getAnnotation(DatabaseTable.class);//get the table meta data from T's DatabaseTable
		
		var primaryKey = data.getOrDefault(table.primaryKey(), null);
		
		if (primaryKey == null)//if we dont have a primary key to update on, return TODO: handle this better
		{
			return;
		}
		
		var parameters = new ArrayList<Object>();
		
		var commandText = "update " + table.name() + " set " + this.compileUpdateItemValueString(data, parameters) + " where id = ?"; //build the command text, adding the condition of the primary key on the end
		
		parameters.add(primaryKey); //add the primary key as the last parameter
		
		var statement = prepareStatement(commandText, parameters);
		
		try
		{
			statement.execute();
			statement.close();
		}
		catch (Exception e) 
		{
			System.err.println("Error executing update command");
			printCommandExecutionError(commandText, parameters);
			e.printStackTrace();
		}
		
		if (this.autoCloseConnection)
		{
			this.closeConnection();
		}
	}
	
	public void update(T data)//update a T, converts to hashmap first, we use a map as a primary so we dont always have to provide a value for every column
	{
		update(convertObjectToMap(data));
	}
	
	public void update (ArrayList <HashMap <String, Object>> data)//update multiple rows
	{
		var autoClose = this.autoCloseConnection; //store the current auto close value
		this.autoCloseConnection = false; //we will not be auto closing since we have multiple commands to run
		
		for (var item : data)
		{
			update (item);
		}
		
		if (autoClose) //close if we were on auto close
		{
			this.closeConnection();
		}
		
		this.autoCloseConnection = autoClose; //reset auto close to its previous value
	}
	
	public void updateObjects (ArrayList <T> data)//update a list of Ts, converts to hashmap first, we use a map as a primary so we dont always have to provide a value for every column
	{
		var mapList = new ArrayList <HashMap <String, Object>> ();
		for (var item : data)
		{
			mapList.add(convertObjectToMap(item));
		}
		
		update(mapList);
	}
	
	public void save (HashMap <String, Object> data) //insert of update a map of values depending on if it has a primary key
	{
		var table = type.getAnnotation(DatabaseTable.class);
		
		var primaryKey = data.getOrDefault(table.primaryKey(), null);
		if (primaryKey == null) //if we cant find a primary key, we insert, otherwise, we update
		{
			insert(data);
		}
		else 
		{
			update(data);
		}		
	}
	
	
	public void save(T data) //insert or update a T, converts to hashmap first, we use a map as a primary so we dont always have to provide a value for every column
	{
		save(convertObjectToMap(data));
	}
	
	
	public void save (ArrayList <HashMap <String, Object>> data)//insert or update multiple rows
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
	
	public void saveObjects (ArrayList <T> data)//insert or update a list of Ts, converts to hashmap first, we use a map as a primary so we dont always have to provide a value for every column
	{
		var mapList = new ArrayList <HashMap <String, Object>> ();
		for (var item : data)
		{
			mapList.add(convertObjectToMap(item));
		}
		
		save(mapList);
	}
	
		
}
