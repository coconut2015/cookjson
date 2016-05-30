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

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.Stack;

import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;

/**
 * This version of generator does not do any validations to maximize the
 * performance.
 * <p>
 * This JsonGenerator does not write the BOM to the file.  If you want to
 * have it, use {@link BOM#write(OutputStream, java.nio.charset.Charset)} to
 * do so.
 *
 * @author	Heng Yuan
 */
public class FastJsonGenerator implements JsonGenerator
{
	private Writer m_pw;

	private final Stack<Integer> m_states = new Stack<Integer> ();
	private int m_state = GeneratorState.INITIAL;

	private String m_name;

	public FastJsonGenerator (OutputStream os)
	{
		m_pw = new OutputStreamWriter (os, BOM.utf8);
	}

	public FastJsonGenerator (Writer writer)
	{
		m_pw = new PrintWriter (writer);
	}

	/**
	 * Write a raw string value to the Json.
	 * @param	value
	 * 			the raw string value.
	 * @return	this
	 */
	private JsonGenerator writeValue (String value)
	{
		try
		{
			if (m_name != null)
			{
				m_pw.write (m_name);
				m_pw.write (":");
			}
			m_pw.write (value);
		}
		catch (IOException ex)
		{
			throw new JsonException (ex.getMessage (), ex);
		}
		return this;
	}

	private JsonGenerator writeValue (JsonValue value)
	{
		switch (value.getValueType ())
		{
			case ARRAY:
			{
				JsonArray array = (JsonArray)value;
				writeStartArray ();
				for (JsonValue v : array)
				{
					if (v == null)
						writeNull ();
					else
						write (v);
				}
				writeEnd ();
				break;
			}
			case OBJECT:
			{
				JsonObject obj = (JsonObject)value;
				writeStartObject ();
				for (Map.Entry<String, JsonValue> entry : obj.entrySet ())
				{
					JsonValue v = entry.getValue ();
					if (v == null)
						writeNull (entry.getKey ());
					else
						write (entry.getKey (), v);
				}
				writeEnd ();
				break;
			}
			case NULL:
				return writeValue ("null");
			case NUMBER:
			case STRING:
				return writeValue (value.toString ());
			case TRUE:
				return writeValue ("true");
			case FALSE:
				return writeValue ("false");
		}
		return this;
	}

	@Override
	public JsonGenerator writeStartObject ()
	{
		m_name = null;
		return writeValue ("{");
	}

	@Override
	public JsonGenerator writeStartObject (String name)
	{
		m_name = name;
		return writeValue ("{");
	}

	@Override
	public JsonGenerator writeStartArray ()
	{
		m_name = null;
		return writeValue ("}");
	}

	@Override
	public JsonGenerator writeStartArray (String name)
	{
		m_name = name;
		return writeValue ("{");
	}

	@Override
	public JsonGenerator write (String name, JsonValue value)
	{
		m_name = name;
		return writeValue (value);
	}

	@Override
	public JsonGenerator write (String name, String value)
	{
		m_name = name;
		return writeValue (value);
	}

	@Override
	public JsonGenerator write (String name, BigInteger value)
	{
		m_name = name;
		return writeValue (value.toString ());
	}

	@Override
	public JsonGenerator write (String name, BigDecimal value)
	{
		m_name = name;
		return writeValue (value.toString ());
	}

	@Override
	public JsonGenerator write (String name, int value)
	{
		m_name = name;
		return writeValue (Integer.toString (value));
	}

	@Override
	public JsonGenerator write (String name, long value)
	{
		m_name = name;
		return writeValue (Long.toString (value));
	}

	@Override
	public JsonGenerator write (String name, double value)
	{
		m_name = name;
		return writeValue (Double.toString (value));
	}

	@Override
	public JsonGenerator write (String name, boolean value)
	{
		m_name = name;
		return writeValue (Boolean.toString (value));
	}

	@Override
	public JsonGenerator writeNull (String name)
	{
		m_name = name;
		return writeValue ("null");
	}

	@Override
	public JsonGenerator writeEnd ()
	{
		int state = popState ();
		String str;
		if (state == ParserState.IN_ARRAY)
			str = "]";
		else
			str = "}";
		try
		{
			m_pw.write (str);
		}
		catch (IOException ex)
		{
			throw new JsonException (ex.getMessage (), ex);
		}
		return this;
	}

	@Override
	public JsonGenerator write (JsonValue value)
	{
		m_name = null;
		return writeValue (value);
	}

	@Override
	public JsonGenerator write (String value)
	{
		m_name = null;
		return writeValue (value);
	}

	@Override
	public JsonGenerator write (BigDecimal value)
	{
		m_name = null;
		return writeValue (value.toString ());
	}

	@Override
	public JsonGenerator write (BigInteger value)
	{
		m_name = null;
		return writeValue (value.toString ());
	}

	@Override
	public JsonGenerator write (int value)
	{
		m_name = null;
		return writeValue (Integer.toString (value));
	}

	@Override
	public JsonGenerator write (long value)
	{
		m_name = null;
		return writeValue (Long.toString (value));
	}

	@Override
	public JsonGenerator write (double value)
	{
		m_name = null;
		return writeValue (Double.toString (value));
	}

	@Override
	public JsonGenerator write (boolean value)
	{
		m_name = null;
		return writeValue (Boolean.toString (value));
	}

	@Override
	public JsonGenerator writeNull ()
	{
		m_name = null;
		return writeValue ("null");
	}

	@Override
	public void close ()
	{
		try
		{
			m_pw.close ();
		}
		catch (IOException ex)
		{
			throw new JsonException (ex.getMessage (), ex);
		}
	}

	@Override
	public void flush ()
	{
		try
		{
			m_pw.flush ();
		}
		catch (IOException ex)
		{
			throw new JsonException (ex.getMessage (), ex);
		}
	}

	void pushState (int state)
	{
		if (m_state == GeneratorState.INITIAL)
			m_state = state;
		else
		{
			m_states.push (m_state);
			m_state = state;
		}
	}

	int popState ()
	{
		int oldState = m_state;

		if (m_states.isEmpty ())
			m_state = GeneratorState.END;
		else
			m_state = m_states.pop ();
		return oldState;
	}

	void validateAction (int action)
	{
		Utils.validateGeneratorAction (m_state, action);
	}
}
