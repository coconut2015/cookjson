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

import java.util.ArrayList;
import java.util.List;

import javax.json.*;

/**
 * @author	Heng Yuan
 */
public class CookJsonArray extends ArrayList<JsonValue> implements JsonArray
{
	private static final long serialVersionUID = 6159626473888359143L;

	@Override
	public ValueType getValueType ()
	{
		return ValueType.ARRAY;
	}

	@Override
	public JsonObject getJsonObject (int index)
	{
		return (JsonObject) get (index);
	}

	@Override
	public JsonArray getJsonArray (int index)
	{
		return (JsonArray) get (index);
	}

	@Override
	public JsonNumber getJsonNumber (int index)
	{
		return (JsonNumber) get (index);
	}

	@Override
	public JsonString getJsonString (int index)
	{
		return (JsonString) get (index);
	}

	@SuppressWarnings ("unchecked")
	@Override
	public <T extends JsonValue> List<T> getValuesAs (Class<T> clazz)
	{
		return (List<T>)this;
	}

	@Override
	public String getString (int index)
	{
		return ((JsonString) get (index)).toString ();
	}

	@Override
	public String getString (int index, String defaultValue)
	{
		try
		{
			return getString (index);
		}
		catch (Exception ex)
		{
			return defaultValue;
		}
	}

	@Override
	public int getInt (int index)
	{
		return ((JsonNumber) get (index)).intValue ();
	}

	@Override
	public int getInt (int index, int defaultValue)
	{
		try
		{
			return getInt (index);
		}
		catch (Exception ex)
		{
			return defaultValue;
		}
	}

	@Override
	public boolean getBoolean (int index)
	{
		JsonValue v = get (index);
		if (v.getValueType () == ValueType.TRUE)
			return true;
		if (v.getValueType () == ValueType.FALSE)
			return false;
		throw new ClassCastException ();
	}

	@Override
	public boolean getBoolean (int index, boolean defaultValue)
	{
		try
		{
			return getBoolean (index);
		}
		catch (Exception ex)
		{
			return defaultValue;
		}
	}

	@Override
	public boolean isNull (int index)
	{
		return get (index).getValueType () == ValueType.NULL;
	}
}
