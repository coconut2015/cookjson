/*
 * Copyright 2016 Heng Yuan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.yuanheng.cookjson.value;

import java.util.HashMap;

import javax.json.*;

/**
 * @author Heng Yuan
 */
public class CookJsonObject extends HashMap<String, JsonValue> implements JsonObject
{
	private static final long serialVersionUID = -4911944799507752602L;

	@Override
	public ValueType getValueType ()
	{
		return ValueType.OBJECT;
	}

	@Override
	public JsonArray getJsonArray (String name)
	{
		return (JsonArray) get (name);
	}

	@Override
	public JsonObject getJsonObject (String name)
	{
		return (JsonObject) get (name);
	}

	@Override
	public JsonNumber getJsonNumber (String name)
	{
		return (JsonNumber) get (name);
	}

	@Override
	public JsonString getJsonString (String name)
	{
		return (JsonString) get (name);
	}

	@Override
	public String getString (String name)
	{
		return getJsonString (name).toString ();
	}

	@Override
	public String getString (String name, String defaultValue)
	{
		try
		{
			return getString (name);
		}
		catch (Exception ex)
		{
			return defaultValue;
		}
	}

	@Override
	public int getInt (String name)
	{
		return getJsonNumber (name).intValue ();
	}

	@Override
	public int getInt (String name, int defaultValue)
	{
		try
		{
			return getJsonNumber (name).intValue ();
		}
		catch (Exception ex)
		{
			return defaultValue;
		}
	}

	@Override
	public boolean getBoolean (String name)
	{
		JsonValue v = get (name);
		if (v.getValueType () == ValueType.TRUE)
			return true;
		if (v.getValueType () == ValueType.FALSE)
			return false;
		throw new ClassCastException ();
	}

	@Override
	public boolean getBoolean (String name, boolean defaultValue)
	{
		try
		{
			return getBoolean (name);
		}
		catch (Exception ex)
		{
			return defaultValue;
		}
	}

	@Override
	public boolean isNull (String name)
	{
		return get (name).getValueType () == ValueType.NULL;
	}

}
