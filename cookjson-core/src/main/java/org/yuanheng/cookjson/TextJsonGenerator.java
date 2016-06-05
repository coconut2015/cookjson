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
 * The idea is that once you verified the writing logic is correct using a
 * checked JsonGenerator, then switch to use this unchecked JsonGenerator.
 * <p>
 * This JsonGenerator does not write the BOM to the file.  If you want to
 * have it, use {@link BOM#write(OutputStream, java.nio.charset.Charset)} to
 * do so.
 *
 * @author	Heng Yuan
 */
public class TextJsonGenerator implements JsonGenerator
{
	/**
	 * Current key name being worked on.  null if we are dealing with an
	 * array.
	 */
	String m_name;
	/**
	 * If the name is already being escaped.
	 */
	boolean m_keyNameEscaped = true;
	/**
	 * The output writer.
	 */
	final Writer m_out;
	/**
	 * Saved state.
	 */
	final Stack<Integer> m_states = new Stack<Integer> ();
	/**
	 * The current state.
	 * <p>
	 * Subclasses should not modify this value.
	 */
	int m_state = GeneratorState.INITIAL;
	/**
	 * Are we dealing with the first element in an array / object?
	 * <p>
	 * Subclasses should not modify this value.
	 */
	boolean m_first = true;

	public TextJsonGenerator (OutputStream os)
	{
		m_out = new BufferedWriter (new OutputStreamWriter (os, BOM.utf8));
	}

	public TextJsonGenerator (Writer out)
	{
		if (!(out instanceof PrintWriter || out instanceof BufferedWriter))
			out = new BufferedWriter (out);
		m_out = out;
	}

	/**
	 * Write a raw string value to the Json.
	 * @param	value
	 * 			the raw string value.
	 * @return	this
	 */
	JsonGenerator writeValue (String value)
	{
		try
		{
			if (m_first)
				m_first = false;
			else
				m_out.write (',');
			if (m_name != null)
			{
				if (m_keyNameEscaped)
				{
					m_out.write (QuoteString.quote (m_name));
				}
				else
				{
					m_out.write ('"');
					m_out.write (m_name);
					m_out.write ('"');
				}
				m_out.write (":");
			}
			m_out.write (value);
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
				JsonArray array = (JsonArray) value;
				if (m_name == null)
					writeStartArray ();
				else
					writeStartArray (m_name);
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
				JsonObject obj = (JsonObject) value;
				if (m_name == null)
					writeStartObject ();
				else
					writeStartObject (m_name);
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
				return writeValue (value.toString ());
			case STRING:
				return writeValue (QuoteString.quote (value.toString ()));
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
		writeValue ("{");
		pushState (GeneratorState.IN_OBJECT);
		m_first = true;
		return this;
	}

	@Override
	public JsonGenerator writeStartObject (String name)
	{
		m_name = name;
		writeValue ("{");
		pushState (GeneratorState.IN_OBJECT);
		m_first = true;
		return this;
	}

	@Override
	public JsonGenerator writeStartArray ()
	{
		m_name = null;
		writeValue ("[");
		pushState (GeneratorState.IN_ARRAY);
		m_first = true;
		return this;
	}

	@Override
	public JsonGenerator writeStartArray (String name)
	{
		m_name = name;
		writeValue ("[");
		pushState (GeneratorState.IN_ARRAY);
		m_first = true;
		return this;
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
		return writeValue (QuoteString.quote (value));
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
		m_first = false;
		if (state == GeneratorState.IN_ARRAY)
			str = "]";
		else if (state == GeneratorState.IN_OBJECT)
			str = "}";
		else
		{
			throw new IllegalStateException ();
		}
		try
		{
			m_out.write (str);
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
		return writeValue (QuoteString.quote (value));
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
			m_out.close ();
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
			m_out.flush ();
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

	/**
	 * Check if key name is escaped.
	 * @return	true if key name is escaped.  false otherwise.
	 */
	public boolean isKeyNameEscaped ()
	{
		return m_keyNameEscaped;
	}

	/**
	 * By default, key name is escaped.  However, if you know the name
	 * is safe (or pre-escaped), you can avoid having them escaped.
	 * <p>
	 * This is useful in certain situations where the same key names
	 * were repeatedly used.  Such as exporting data to JSON from
	 * JDBC ResultSet.
	 *
	 * @param	b
	 *			true if the key name should be escaped.  false otherwise.
	 */
	public void setKeyNameEscaped (boolean b)
	{
		m_keyNameEscaped = b;
	}
}
