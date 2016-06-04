/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.yuanheng.cookjson;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.yuanheng.cookjson.value.*;

/**
 * @author	Heng Yuan
 */
class JsonObjectBuilderImpl implements JsonObjectBuilder
{
	private final CookJsonObject m_object = new CookJsonObject ();

	@Override
	public JsonObjectBuilder add (String name, JsonValue value)
	{
		m_object.put (name, value);
		return this;
	}

	@Override
	public JsonObjectBuilder add (String name, String value)
	{
		m_object.put (name, new CookJsonString (value));
		return this;
	}

	@Override
	public JsonObjectBuilder add (String name, BigInteger value)
	{
		m_object.put (name, new CookJsonNumber (value));
		return this;
	}

	@Override
	public JsonObjectBuilder add (String name, BigDecimal value)
	{
		m_object.put (name, new CookJsonNumber (value));
		return this;
	}

	@Override
	public JsonObjectBuilder add (String name, int value)
	{
		m_object.put (name, new CookJsonNumber (value));
		return this;
	}

	@Override
	public JsonObjectBuilder add (String name, long value)
	{
		m_object.put (name, new CookJsonNumber (value));
		return this;
	}

	@Override
	public JsonObjectBuilder add (String name, double value)
	{
		m_object.put (name, new CookJsonNumber (value));
		return this;
	}

	@Override
	public JsonObjectBuilder add (String name, boolean value)
	{
		m_object.put (name, value ? CookJsonBoolean.TRUE : CookJsonBoolean.FALSE);
		return this;
	}

	@Override
	public JsonObjectBuilder addNull (String name)
	{
		m_object.put (name, CookJsonNull.NULL);
		return this;
	}

	@Override
	public JsonObjectBuilder add (String name, JsonObjectBuilder builder)
	{
		m_object.put (name, builder.build ());
		return this;
	}

	@Override
	public JsonObjectBuilder add (String name, JsonArrayBuilder builder)
	{
		m_object.put (name, builder.build ());
		return this;
	}

	@Override
	public JsonObject build ()
	{
		return m_object;
	}

}
