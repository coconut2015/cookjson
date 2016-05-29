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

import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;

import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;

/**
 * @author	Heng Yuan
 */
public class CheckedBsonGenerator extends FastBsonGenerator
{
	/**
	 * @param	os
	 * 			the output stream
	 */
	public CheckedBsonGenerator (OutputStream os)
	{
		super (os);
	}

	@Override
	public JsonGenerator writeStartObject ()
	{
		validateAction (GeneratorAction.WRITE_ARRAY_VALUE);
		return super.writeStartObject ();
	}

	@Override
	public JsonGenerator writeStartObject (String name)
	{
		validateAction (GeneratorAction.WRITE_OBJECT_VALUE);
		return super.writeStartObject (name);
	}

	@Override
	public JsonGenerator writeStartArray ()
	{
		validateAction (GeneratorAction.WRITE_ARRAY_VALUE);
		return super.writeStartArray ();
	}

	@Override
	public JsonGenerator writeStartArray (String name)
	{
		validateAction (GeneratorAction.WRITE_OBJECT_VALUE);
		return super.writeStartArray (name);
	}

	@Override
	public JsonGenerator write (String name, String value)
	{
		validateAction (GeneratorAction.WRITE_OBJECT_VALUE);
		return super.write (name, value);
	}

	@Override
	public JsonGenerator write (String name, JsonValue value)
	{
		validateAction (GeneratorAction.WRITE_OBJECT_VALUE);
		return super.write (name, value);
	}

	@Override
	public JsonGenerator write (String name, BigInteger value)
	{
		validateAction (GeneratorAction.WRITE_OBJECT_VALUE);
		return super.write (name, value);
	}

	@Override
	public JsonGenerator write (String name, BigDecimal value)
	{
		validateAction (GeneratorAction.WRITE_OBJECT_VALUE);
		return super.write (name, value);
	}

	@Override
	public JsonGenerator write (String name, int value)
	{
		validateAction (GeneratorAction.WRITE_OBJECT_VALUE);
		return super.write (name, value);
	}

	@Override
	public JsonGenerator write (String name, long value)
	{
		validateAction (GeneratorAction.WRITE_OBJECT_VALUE);
		return super.write (name, value);
	}

	@Override
	public JsonGenerator write (String name, double value)
	{
		validateAction (GeneratorAction.WRITE_OBJECT_VALUE);
		return super.write (name, value);
	}

	@Override
	public JsonGenerator write (String name, boolean value)
	{
		validateAction (GeneratorAction.WRITE_OBJECT_VALUE);
		return super.write (name, value);
	}

	@Override
	public JsonGenerator writeNull (String name)
	{
		validateAction (GeneratorAction.WRITE_OBJECT_VALUE);
		return super.writeNull (name);
	}

	@Override
	public JsonGenerator writeEnd ()
	{
		validateAction (GeneratorAction.END_ARRAY_OBJECT);
		return super.writeEnd ();
	}

	@Override
	public JsonGenerator write (String value)
	{
		validateAction (GeneratorAction.WRITE_ARRAY_VALUE);
		return super.write (value);
	}

	@Override
	public JsonGenerator write (JsonValue value)
	{
		validateAction (GeneratorAction.WRITE_ARRAY_VALUE);
		return super.write (value);
	}

	@Override
	public JsonGenerator write (BigDecimal value)
	{
		validateAction (GeneratorAction.WRITE_ARRAY_VALUE);
		return super.write (value);
	}

	@Override
	public JsonGenerator write (BigInteger value)
	{
		validateAction (GeneratorAction.WRITE_ARRAY_VALUE);
		return super.write (value);
	}

	@Override
	public JsonGenerator write (int value)
	{
		validateAction (GeneratorAction.WRITE_ARRAY_VALUE);
		return super.write (value);
	}

	@Override
	public JsonGenerator write (long value)
	{
		validateAction (GeneratorAction.WRITE_ARRAY_VALUE);
		return super.write (value);
	}

	@Override
	public JsonGenerator write (double value)
	{
		validateAction (GeneratorAction.WRITE_ARRAY_VALUE);
		return super.write (value);
	}

	@Override
	public JsonGenerator write (boolean value)
	{
		validateAction (GeneratorAction.WRITE_ARRAY_VALUE);
		return super.write (value);
	}

	@Override
	public JsonGenerator writeNull ()
	{
		validateAction (GeneratorAction.WRITE_ARRAY_VALUE);
		return super.writeNull ();
	}

	@Override
	public void close ()
	{
		validateAction (GeneratorAction.CLOSE);
		super.close ();
	}
}
