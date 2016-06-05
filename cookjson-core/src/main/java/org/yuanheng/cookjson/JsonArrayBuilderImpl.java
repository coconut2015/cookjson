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

import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.yuanheng.cookjson.value.*;

/**
 * @author	Heng Yuan
 */
class JsonArrayBuilderImpl implements JsonArrayBuilder
{
	private final CookJsonArray m_array = new CookJsonArray ();

	@Override
	public JsonArrayBuilder add (JsonValue value)
	{
		m_array.add (value);
		return this;
	}

	@Override
	public JsonArrayBuilder add (String value)
	{
		m_array.add (new CookJsonString (value));
		return this;
	}

	@Override
	public JsonArrayBuilder add (BigDecimal value)
	{
		m_array.add (new CookJsonNumber (value));
		return this;
	}

	@Override
	public JsonArrayBuilder add (BigInteger value)
	{
		m_array.add (new CookJsonNumber (value));
		return this;
	}

	@Override
	public JsonArrayBuilder add (int value)
	{
		m_array.add (new CookJsonNumber (value));
		return this;
	}

	@Override
	public JsonArrayBuilder add (long value)
	{
		m_array.add (new CookJsonNumber (value));
		return this;
	}

	@Override
	public JsonArrayBuilder add (double value)
	{
		m_array.add (new CookJsonNumber (value));
		return this;
	}

	@Override
	public JsonArrayBuilder add (boolean value)
	{
		m_array.add (value ? CookJsonBoolean.TRUE : CookJsonBoolean.FALSE);
		return this;
	}

	@Override
	public JsonArrayBuilder addNull ()
	{
		m_array.add (CookJsonNull.NULL);
		return this;
	}

	@Override
	public JsonArrayBuilder add (JsonObjectBuilder builder)
	{
		m_array.add (builder.build ());
		return this;
	}

	@Override
	public JsonArrayBuilder add (JsonArrayBuilder builder)
	{
		m_array.add (builder.build ());
		return this;
	}

	@Override
	public JsonArray build ()
	{
		return m_array;
	}
}