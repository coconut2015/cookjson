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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.Stack;

import javax.json.*;
import javax.json.stream.JsonGenerator;

import org.yuanheng.cookjson.value.CookJsonBinary;

/**
 * This version of generator does not do any validations to maximize the
 * performance.
 *
 * @author	Heng Yuan
 */
public class BsonGenerator implements JsonGenerator
{
	private final BufferedOutputStream m_os;
	private final byte[] m_bytes = new byte[8];

	private final Stack<Integer> m_states = new Stack<Integer> ();
	private int m_state = GeneratorState.INITIAL;

	private final Stack<Integer> m_arrayCounts = new Stack<Integer> ();
	private String m_name;

	private boolean m_validateName;

	private boolean m_useDouble = false;

	public BsonGenerator (OutputStream os)
	{
		m_os = new BufferedOutputStream (os);
	}

	public void setUseDouble (boolean b)
	{
		m_useDouble = true;
	}

	private void writeCString (String name) throws IOException
	{
		if (name.length () == 0)
			m_os.write (0);
		else
		{
			byte[] bytes = name.getBytes (BOM.utf8);
			if (m_validateName)
			{
				for (int i = 0; i < bytes.length; ++i)
					if (bytes[i] == 0)
						throw new IOException ("Cannot store the name string: " + name);
			}
			m_os.write (bytes);
			m_os.write (0);
		}
	}

	private JsonGenerator writeElement (int type, String name, byte[] bytes, int length)
	{
		try
		{
			m_os.write (type);
			writeCString (name);
			m_os.write (bytes, 0, length);
		}
		catch (IOException ex)
		{
			throw new JsonException (ex.getMessage (), ex);
		}
		return this;
	}

	private JsonGenerator writeRootObject ()
	{
		try
		{
			m_os.write (m_bytes, 0, 4);
		}
		catch (IOException ex)
		{
			throw new JsonException (ex.getMessage (), ex);
		}
		return this;
	}

	private JsonGenerator writeObject (boolean root)
	{
		pushState (GeneratorState.IN_OBJECT);
		Utils.setInt (m_bytes, 0);	// length.  For streaming, we set to 0.
		if (root)
			return writeRootObject ();
		else
			return writeElement (BsonType.Document, m_name, m_bytes, 4);
	}

	private JsonGenerator writeArray (boolean root)
	{
		pushState (GeneratorState.IN_ARRAY);
		Utils.setInt (m_bytes, 0);	// length.  For streaming, we set to 0.
		m_arrayCounts.push (0);		// start index value at 0.
		if (root)
			return writeRootObject ();
		else
			return writeElement (BsonType.Array, m_name, m_bytes, 4);
	}

	private JsonGenerator writeValue (byte[] value)
	{
		try
		{
			Utils.setInt (m_bytes, value.length);
			m_os.write (m_bytes);
			m_os.write (0);
			m_os.write (value);
		}
		catch (IOException ex)
		{
			throw new JsonException (ex.getMessage (), ex);
		}
		return this;
	}

	private JsonGenerator writeValue (String value)
	{
		byte[] bytes = value.getBytes (BOM.utf8);
		Utils.setInt (m_bytes, bytes.length + 1);
		writeElement (BsonType.String, m_name, m_bytes, 4);
		try
		{
			m_os.write (bytes);
			m_os.write (0);
		}
		catch (IOException ex)
		{
			throw new JsonException (ex.getMessage (), ex);
		}
		return this;
	}

	private JsonGenerator writeValue (int value)
	{
		Utils.setInt (m_bytes, value);
		return writeElement (BsonType.Integer, m_name, m_bytes, 4);
	}

	private JsonGenerator writeValue (long value)
	{
		Utils.setLong (m_bytes, value);
		return writeElement (BsonType.Long, m_name, m_bytes, 8);
	}
	
	private JsonGenerator writeValue (double value)
	{
		long v = Double.doubleToLongBits (value);
		Utils.setLong (m_bytes, v);
		return writeElement (BsonType.Double, m_name, m_bytes, 8);
	}

	private JsonGenerator writeValue (boolean value)
	{
		m_bytes[0] = (byte) (value ? 1 : 0);
		return writeElement (BsonType.Boolean, m_name, m_bytes, 1);
	}

	private JsonGenerator writeNullValue ()
	{
		try
		{
			m_os.write (BsonType.Null);
			writeCString (m_name);
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
				writeArray (m_state == GeneratorState.INITIAL);
				for (JsonValue v : array)
				{
					m_name = null;
					if (v == null)
						writeNullValue ();
					else
						writeValue (v);
				}
				writeEnd ();
				break;
			}
			case OBJECT:
			{
				JsonObject obj = (JsonObject)value;
				writeObject (m_state == GeneratorState.INITIAL);
				for (Map.Entry<String, JsonValue> entry : obj.entrySet ())
				{
					m_name = entry.getKey ();
					JsonValue v = entry.getValue ();
					if (v == null)
						writeNullValue ();
					else
						writeValue (v);
				}
				writeEnd ();
				break;
			}
			case NULL:
				return writeNullValue ();
			case NUMBER:
			{
				JsonNumber number = (JsonNumber)value;
				if (number.isIntegral ())
				{
					// try write the number in int / long first and see it
					// fits.  Otherwise, write it as the string literal.
					try
					{
						return writeValue (number.intValueExact ());
					}
					catch (ArithmeticException ex)
					{
						try
						{
							return writeValue (number.longValueExact ());
						}
						catch (ArithmeticException ex2)
						{
							if (m_useDouble)
								return writeValue (number.doubleValue ());
							return writeValue (number.toString ());
						}
					}
				}
				else
				{
					if (m_useDouble)
						return writeValue (number.doubleValue ());
					return writeValue (number.toString ());
				}
			}
			case STRING:
				if (value instanceof CookJsonBinary)
					return writeValue (((CookJsonBinary) value).getBytes ());
				return writeValue (value.toString ());
			case TRUE:
				return writeValue (true);
			case FALSE:
				return writeValue (false);
		}
		return this;
	}

	@Override
	public JsonGenerator writeStartObject ()
	{
		assert Debug.debug ("WRITE: START_OBJECT");
		if (m_state == GeneratorState.INITIAL)
			m_name = "";
		else
			m_name = getIndex ();
		m_validateName = false;
		return writeObject (m_state == GeneratorState.INITIAL);
	}

	@Override
	public JsonGenerator writeStartObject (String name)
	{
		assert Debug.debug ("WRITE: KEY_NAME: " + name);
		assert Debug.debug ("WRITE: START_OBJECT");
		m_name = name;
		m_validateName = true;
		return writeObject (false);
	}

	@Override
	public JsonGenerator writeStartArray ()
	{
		assert Debug.debug ("WRITE: START_ARRAY");
		if (m_state == GeneratorState.INITIAL)
			m_name = "";
		else
			m_name = getIndex ();
		m_validateName = false;
		return writeArray (m_state == GeneratorState.INITIAL);
	}

	@Override
	public JsonGenerator writeStartArray (String name)
	{
		assert Debug.debug ("WRITE: KEY_NAME: " + name);
		assert Debug.debug ("WRITE: START_ARRAY");
		m_name = name;
		m_validateName = true;
		return writeArray (false);
	}

	@Override
	public JsonGenerator write (String name, JsonValue value)
	{
		assert Debug.debug ("WRITE: KEY_NAME: " + name);
		assert Debug.debug ("WRITE: JsonValue");
		m_name = name;
		m_validateName = true;
		return writeValue (value);
	}

	public JsonGenerator write (String name, byte[] value)
	{
		assert Debug.debug ("WRITE: KEY_NAME: " + name);
		assert Debug.debug ("WRITE: VALUE_BINARY");
		m_name = name;
		m_validateName = true;
		return writeValue (value);
	}

	@Override
	public JsonGenerator write (String name, String value)
	{
		assert Debug.debug ("WRITE: KEY_NAME: " + name);
		assert Debug.debug ("WRITE: VALUE_STRING");
		m_name = name;
		m_validateName = true;
		return writeValue (value);
	}

	@Override
	public JsonGenerator write (String name, BigInteger value)
	{
		assert Debug.debug ("WRITE: KEY_NAME: " + name);
		assert Debug.debug ("WRITE: VALUE_NUMBER: (BigInteger)" + value);
		m_name = name;
		m_validateName = true;
		return writeValue (value.toString ());
	}

	@Override
	public JsonGenerator write (String name, BigDecimal value)
	{
		assert Debug.debug ("WRITE: KEY_NAME: " + name);
		assert Debug.debug ("WRITE: VALUE_NUMBER: (BigDecimal)" + value);
		m_name = name;
		m_validateName = true;
		if (m_useDouble)
			return writeValue (value.doubleValue ());
		return writeValue (value.toString ());
	}

	@Override
	public JsonGenerator write (String name, int value)
	{
		assert Debug.debug ("WRITE: KEY_NAME: " + name);
		assert Debug.debug ("WRITE: VALUE_NUMBER: (int)" + value);
		m_name = name;
		m_validateName = true;
		return writeValue (value);
	}

	@Override
	public JsonGenerator write (String name, long value)
	{
		assert Debug.debug ("WRITE: KEY_NAME: " + name);
		assert Debug.debug ("WRITE: VALUE_NUMBER: (long)" + value);
		m_name = name;
		m_validateName = true;
		return writeValue (value);
	}

	@Override
	public JsonGenerator write (String name, double value)
	{
		assert Debug.debug ("WRITE: KEY_NAME: " + name);
		assert Debug.debug ("WRITE: VALUE_NUMBER: (double)" + value);
		m_name = name;
		m_validateName = true;
		return writeValue (value);
	}

	@Override
	public JsonGenerator write (String name, boolean value)
	{
		assert Debug.debug ("WRITE: KEY_NAME: " + name);
		assert Debug.debug ("WRITE: VALUE_" + (value ? "TRUE" : "FALSE"));
		m_name = name;
		m_validateName = true;
		return writeValue (value);
	}

	@Override
	public JsonGenerator writeNull (String name)
	{
		assert Debug.debug ("WRITE: KEY_NAME: " + name);
		assert Debug.debug ("WRITE: VALUE_NULL");
		m_name = name;
		m_validateName = true;
		return writeNullValue ();
	}

	@Override
	public JsonGenerator writeEnd ()
	{
		int state = popState ();
		assert Debug.debug ("WRITE: " + (state == ParserState.IN_ARRAY ? "END_ARRAY" : "END_OBJECT"));
		try
		{
			m_os.write (0);
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
		assert Debug.debug ("WRITE: JsonValue");
		m_name = getIndex ();
		m_validateName = false;
		return writeValue (value);
	}

	public JsonGenerator write (byte[] value)
	{
		assert Debug.debug ("WRITE: VALUE_BINARY");
		m_name = getIndex ();
		m_validateName = false;
		return writeValue (value);
	}

	@Override
	public JsonGenerator write (String value)
	{
		assert Debug.debug ("WRITE: VALUE_STRING");
		m_name = getIndex ();
		m_validateName = false;
		return writeValue (value);
	}

	@Override
	public JsonGenerator write (BigDecimal value)
	{
		assert Debug.debug ("WRITE: VALUE_NUMBER: (BigDecimal)" + value);
		m_name = getIndex ();
		m_validateName = false;
		if (m_useDouble)
			return writeValue (value.doubleValue ());
		return writeValue (value.toString ());
	}

	@Override
	public JsonGenerator write (BigInteger value)
	{
		assert Debug.debug ("WRITE: VALUE_NUMBER: (BigInteger)" + value);
		m_name = getIndex ();
		m_validateName = false;
		if (m_useDouble)
			return writeValue (value.doubleValue ());
		return writeValue (value.toString ());
	}

	@Override
	public JsonGenerator write (int value)
	{
		assert Debug.debug ("WRITE: VALUE_NUMBER: (int)" + value);
		m_name = getIndex ();
		m_validateName = false;
		return writeValue (value);
	}

	@Override
	public JsonGenerator write (long value)
	{
		assert Debug.debug ("WRITE: VALUE_NUMBER: (long)" + value);
		m_name = getIndex ();
		m_validateName = false;
		return writeValue (value);
	}

	@Override
	public JsonGenerator write (double value)
	{
		assert Debug.debug ("WRITE: VALUE_NUMBER: (double)" + value);
		m_name = getIndex ();
		m_validateName = false;
		return writeValue (value);
	}

	@Override
	public JsonGenerator write (boolean value)
	{
		assert Debug.debug ("WRITE: VALUE_" + (value ? "TRUE" : "FALSE"));
		m_name = getIndex ();
		m_validateName = false;
		return writeValue (value);
	}

	@Override
	public JsonGenerator writeNull ()
	{
		assert Debug.debug ("WRITE: VALUE_NULL");
		m_name = getIndex ();
		m_validateName = false;
		return writeNullValue ();
	}

	@Override
	public void close ()
	{
		try
		{
			m_os.close ();
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
			m_os.flush ();
		}
		catch (IOException ex)
		{
			throw new JsonException (ex.getMessage (), ex);
		}
	}

	String getIndex ()
	{
		int index = m_arrayCounts.pop ();
		m_arrayCounts.push (index + 1);
		return Integer.toString (index);
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
		if (m_state == GeneratorState.IN_ARRAY)
			m_arrayCounts.pop ();

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
