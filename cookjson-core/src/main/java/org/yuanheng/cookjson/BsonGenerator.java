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
package org.yuanheng.cookjson;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Map;

import javax.json.*;
import javax.json.stream.JsonGenerationException;
import javax.json.stream.JsonGenerator;

import org.yuanheng.cookjson.value.CookJsonBinary;

/**
 * A simple implementation of JsonGenerator that generates
 * output in BSON format.
 * <p>
 * Because BSON does not support {@link BigDecimal} and {@link BigInteger}
 * types, they are stored as string literals.  It is possible to store
 * them as double (for certain range of values).  To do so, use the
 * function {@link #setUseDouble(boolean)}.
 * <p>
 * It should be noted that the generated BSON file is in a stream format
 * that has 0's for Document / Array type lengths.  While {@link BsonParser}
 * has no problems reading the file, some utilities such as bsondump do
 * require them to be correctly specified.  Use {@link BsonFixLength#fix(java.io.File)}
 * to update the length information.
 *
 * @author	Heng Yuan
 */
public class BsonGenerator implements CookJsonGenerator
{
	private final static byte[] DIGITS = "0123456789".getBytes ();
	private final OutputStream m_os;
	/**
	 * Internal write buffer.  We manually buffer it rather than
	 * using BufferedWriter due to slight performance improvement.
	 */
	private final static int m_max = 8192;
	private final byte[] m_buffer = new byte[m_max + 1];	// +1 so that our single byte padding logic is simpler
	/** Buffer position */
	private int m_pos;

	private final static int m_valueLen = 22;
	private final byte[] m_bytes = new byte[m_valueLen];

	final ArrayList<Boolean> m_states = new ArrayList<Boolean> ();
	private int m_state = GeneratorState.INITIAL;

	private final ArrayList<Integer> m_arrayCounts = new ArrayList<Integer> ();
	private String m_name;
	private int m_index;

	private boolean m_useDouble;

	/**
	 * Constructor for BsonGenerator.
	 *
	 * @param	os
	 * 			the output stream.
	 */
	public BsonGenerator (OutputStream os)
	{
		m_os = os;
	}

	/**
	 * If the flag is set to true, {@link BigDecimal} / {@link BigInteger}
	 * values are stored as double instead of string.
	 *
	 * @param	b
	 * 			boolean flag.
	 */
	public void setUseDouble (boolean b)
	{
		m_useDouble = b;
	}

	private void w (byte[] bytes) throws IOException
	{
		w (bytes, 0, bytes.length);
	}

	private void w (byte[] bytes, int offset, int length) throws IOException
	{
		int pos = m_pos;
		byte[] buf = m_buffer;
		if (pos + length < m_max)
		{
			for (int i = 0; i < length; ++i)
				buf[pos++] = bytes[offset++];
			m_pos = pos;
			return;
		}
		{
			int len = m_max - pos;
			while (pos < m_max)
				buf[pos++] = bytes[offset++];
			m_os.write (buf, 0, m_max);
			length -= len;
		}
		while (length > m_max)
		{
			m_os.write (bytes, offset, m_max);
			offset += m_max;
			length -= m_max;
		}

		for (int i = 0; i < length; ++i)
			buf[i] = bytes[offset++];
		m_pos = length;
	}

	void w (int b) throws IOException
	{
		byte[] buf = m_buffer;
		int pos = m_pos;
		buf[pos++] = (byte)b;
		if (pos >= m_max)
		{
			m_os.write (buf, 0, pos);
			m_pos = 0;
		}
		else
		{
			m_pos = pos;
		}
	}

	/**
	 * Write the array index, including the padding 0 byte.
	 *
	 * @param	value
	 *			the index value.  It should not be negative.
	 * @throws	IOException
	 * 			in case of I/O error.
	 */
	private void wi (int value) throws IOException
	{
		byte[] buf = m_bytes;

		int pos = m_valueLen;
		buf[--pos] = 0;	// terminating null
		// use do-while to generate at least 1 digit.
		do
		{
			int v = value / 10;
			int r = value - v * 10;
			value = v;
			buf[--pos] = DIGITS[r];
		}
		while (value > 0);
		w (buf, pos, m_valueLen - pos);
	}

	private void checkName (String name)
	{
		if (name.indexOf (0) >= 0)
			throw new IllegalArgumentException ("Name string contains \\0.");
		m_name = name;
	}

	private void writeCString (String name) throws IOException
	{
		if (name == null)
		{
			wi (m_index++);
		}
		else if (name.length () == 0)
			w (0);
		else
		{
			byte[] bytes = name.getBytes (BOM.utf8);
			w (bytes);
			w (0);
		}
	}

	private JsonGenerator writeElement (int type, String name, int length)
	{
		try
		{
			w (type);
			writeCString (name);
			w (m_bytes, 0, length);
		}
		catch (IOException ex)
		{
			throw new JsonGenerationException (ex.getMessage (), ex);
		}
		return this;
	}

	private JsonGenerator writeRootObject ()
	{
		try
		{
			w (m_bytes, 0, 4);
		}
		catch (IOException ex)
		{
			throw new JsonGenerationException (ex.getMessage (), ex);
		}
		return this;
	}

	private JsonGenerator writeObject (boolean root)
	{
		pushState (false);
		Utils.setInt (m_bytes, 0);	// length.  For streaming, we set to 0.
		if (root)
			return writeRootObject ();
		else
			return writeElement (BsonType.Document, m_name, 4);
	}

	private JsonGenerator writeArray (boolean root)
	{
		pushState (true);
		Utils.setInt (m_bytes, 0);	// length.  For streaming, we set to 0.
		if (root)
		{
			m_arrayCounts.add (0);
			m_index = 0;
			writeRootObject ();
			m_name = null;
			return this;
		}
		else
		{
			writeElement (BsonType.Array, m_name, 4);
			m_arrayCounts.add (m_index);
			m_index = 0;
			m_name = null;
			return this;
		}
	}

	private JsonGenerator writeValue (byte[] value)
	{
		try
		{
			Utils.setInt (m_bytes, value.length);
			writeElement (BsonType.Binary, m_name, 4);
			w (0);
			w (value);
		}
		catch (IOException ex)
		{
			throw new JsonGenerationException (ex.getMessage (), ex);
		}
		return this;
	}

	private JsonGenerator writeValue (String value)
	{
		byte[] bytes = value.getBytes (BOM.utf8);
		Utils.setInt (m_bytes, bytes.length + 1);
		writeElement (BsonType.String, m_name, 4);
		try
		{
			w (bytes);
			w (0);
		}
		catch (IOException ex)
		{
			throw new JsonGenerationException (ex.getMessage (), ex);
		}
		return this;
	}

	private JsonGenerator writeValue (int value)
	{
		Utils.setInt (m_bytes, value);
		return writeElement (BsonType.Integer, m_name, 4);
	}

	private JsonGenerator writeValue (long value)
	{
		Utils.setLong (m_bytes, value);
		return writeElement (BsonType.Long, m_name, 8);
	}
	
	private JsonGenerator writeValue (double value)
	{
		long v = Double.doubleToLongBits (value);
		Utils.setLong (m_bytes, v);
		return writeElement (BsonType.Double, m_name, 8);
	}

	private JsonGenerator writeValue (boolean value)
	{
		m_bytes[0] = (byte) (value ? 1 : 0);
		return writeElement (BsonType.Boolean, m_name, 1);
	}

	private JsonGenerator writeNullValue ()
	{
		try
		{
			w (BsonType.Null);
			writeCString (m_name);
		}
		catch (IOException ex)
		{
			throw new JsonGenerationException (ex.getMessage (), ex);
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
				return writeValue (((JsonString)value).getString ());
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
//		assert Debug.debug ("WRITE: START_OBJECT");
		if (m_state != GeneratorState.INITIAL &&
			m_state != GeneratorState.IN_ARRAY)
			throw new JsonGenerationException (ErrorMessage.invalidContext);
		return writeObject (m_state == GeneratorState.INITIAL);
	}

	@Override
	public JsonGenerator writeStartObject (String name)
	{
//		assert Debug.debug ("WRITE: KEY_NAME: " + name);
//		assert Debug.debug ("WRITE: START_OBJECT");
		if (m_state != GeneratorState.IN_OBJECT)
			throw new JsonGenerationException (ErrorMessage.notInObjectContext);
		checkName (name);
		return writeObject (false);
	}

	@Override
	public JsonGenerator writeStartArray ()
	{
//		assert Debug.debug ("WRITE: START_ARRAY");
		if (m_state != GeneratorState.INITIAL &&
			m_state != GeneratorState.IN_ARRAY)
			throw new JsonGenerationException (ErrorMessage.invalidContext);
		return writeArray (m_state == GeneratorState.INITIAL);
	}

	@Override
	public JsonGenerator writeStartArray (String name)
	{
//		assert Debug.debug ("WRITE: KEY_NAME: " + name);
//		assert Debug.debug ("WRITE: START_ARRAY");
		if (m_state != GeneratorState.IN_OBJECT)
			throw new JsonGenerationException (ErrorMessage.notInObjectContext);
		checkName (name);
		return writeArray (false);
	}

	@Override
	public JsonGenerator write (String name, JsonValue value)
	{
//		assert Debug.debug ("WRITE: KEY_NAME: " + name);
//		assert Debug.debug ("WRITE: JsonValue");
		if (m_state != GeneratorState.IN_OBJECT)
			throw new JsonGenerationException (ErrorMessage.notInObjectContext);
		checkName (name);
		return writeValue (value);
	}

	@Override
	public JsonGenerator write (String name, byte[] value)
	{
//		assert Debug.debug ("WRITE: KEY_NAME: " + name);
//		assert Debug.debug ("WRITE: VALUE_BINARY");
		if (m_state != GeneratorState.IN_OBJECT)
			throw new JsonGenerationException (ErrorMessage.notInObjectContext);
		checkName (name);
		return writeValue (value);
	}

	@Override
	public JsonGenerator write (String name, String value)
	{
//		assert Debug.debug ("WRITE: KEY_NAME: " + name);
//		assert Debug.debug ("WRITE: VALUE_STRING");
		if (m_state != GeneratorState.IN_OBJECT)
			throw new JsonGenerationException (ErrorMessage.notInObjectContext);
		checkName (name);
		return writeValue (value);
	}

	@Override
	public JsonGenerator write (String name, BigInteger value)
	{
//		assert Debug.debug ("WRITE: KEY_NAME: " + name);
//		assert Debug.debug ("WRITE: VALUE_NUMBER: (BigInteger)" + value);
		if (m_state != GeneratorState.IN_OBJECT)
			throw new JsonGenerationException (ErrorMessage.notInObjectContext);
		checkName (name);
		if (m_useDouble)
			return writeValue (value.doubleValue ());
		return writeValue (value.toString ());
	}

	@Override
	public JsonGenerator write (String name, BigDecimal value)
	{
//		assert Debug.debug ("WRITE: KEY_NAME: " + name);
//		assert Debug.debug ("WRITE: VALUE_NUMBER: (BigDecimal)" + value);
		if (m_state != GeneratorState.IN_OBJECT)
			throw new JsonGenerationException (ErrorMessage.notInObjectContext);
		checkName (name);
		if (m_useDouble)
			return writeValue (value.doubleValue ());
		return writeValue (value.toString ());
	}

	@Override
	public JsonGenerator write (String name, int value)
	{
//		assert Debug.debug ("WRITE: KEY_NAME: " + name);
//		assert Debug.debug ("WRITE: VALUE_NUMBER: (int)" + value);
		if (m_state != GeneratorState.IN_OBJECT)
			throw new JsonGenerationException (ErrorMessage.notInObjectContext);
		checkName (name);
		return writeValue (value);
	}

	@Override
	public JsonGenerator write (String name, long value)
	{
//		assert Debug.debug ("WRITE: KEY_NAME: " + name);
//		assert Debug.debug ("WRITE: VALUE_NUMBER: (long)" + value);
		if (m_state != GeneratorState.IN_OBJECT)
			throw new JsonGenerationException (ErrorMessage.notInObjectContext);
		checkName (name);
		return writeValue (value);
	}

	@Override
	public JsonGenerator write (String name, double value)
	{
//		assert Debug.debug ("WRITE: KEY_NAME: " + name);
//		assert Debug.debug ("WRITE: VALUE_NUMBER: (double)" + value);
		if (m_state != GeneratorState.IN_OBJECT)
			throw new JsonGenerationException (ErrorMessage.notInObjectContext);
		checkName (name);
		return writeValue (value);
	}

	@Override
	public JsonGenerator write (String name, boolean value)
	{
//		assert Debug.debug ("WRITE: KEY_NAME: " + name);
//		assert Debug.debug ("WRITE: VALUE_" + (value ? "TRUE" : "FALSE"));
		if (m_state != GeneratorState.IN_OBJECT)
			throw new JsonGenerationException (ErrorMessage.notInObjectContext);
		checkName (name);
		return writeValue (value);
	}

	@Override
	public JsonGenerator writeNull (String name)
	{
//		assert Debug.debug ("WRITE: KEY_NAME: " + name);
//		assert Debug.debug ("WRITE: VALUE_NULL");
		if (m_state != GeneratorState.IN_OBJECT)
			throw new JsonGenerationException (ErrorMessage.notInObjectContext);
		checkName (name);
		return writeNullValue ();
	}

	@Override
	public JsonGenerator writeEnd ()
	{
		boolean isArray = popState ();
		if (isArray)
		{
			m_index = m_arrayCounts.remove (m_arrayCounts.size () - 1);
		}
//		assert Debug.debug ("WRITE: " + (isArray ? "END_ARRAY" : "END_OBJECT"));
		try
		{
			w (0);
			m_name = null;
		}
		catch (IOException ex)
		{
			throw new JsonGenerationException (ex.getMessage (), ex);
		}
		return this;
	}

	@Override
	public JsonGenerator write (JsonValue value)
	{
//		assert Debug.debug ("WRITE: JsonValue");
		if (m_state != GeneratorState.IN_ARRAY)
		{
			if (m_state == GeneratorState.INITIAL)
			{
				if (!(value instanceof JsonArray) &&
					!(value instanceof JsonObject))
					throw new JsonGenerationException (ErrorMessage.invalidContext);
			}
			else
				throw new JsonGenerationException (ErrorMessage.notInArrayContext);
		}
		return writeValue (value);
	}

	@Override
	public JsonGenerator write (byte[] value)
	{
//		assert Debug.debug ("WRITE: VALUE_BINARY");
		if (m_state != GeneratorState.IN_ARRAY)
			throw new JsonGenerationException (ErrorMessage.notInArrayContext);
		return writeValue (value);
	}

	@Override
	public JsonGenerator write (String value)
	{
//		assert Debug.debug ("WRITE: VALUE_STRING");
		if (m_state != GeneratorState.IN_ARRAY)
			throw new JsonGenerationException (ErrorMessage.notInArrayContext);
		return writeValue (value);
	}

	@Override
	public JsonGenerator write (BigDecimal value)
	{
//		assert Debug.debug ("WRITE: VALUE_NUMBER: (BigDecimal)" + value);
		if (m_state != GeneratorState.IN_ARRAY)
			throw new JsonGenerationException (ErrorMessage.notInArrayContext);
		if (m_useDouble)
			return writeValue (value.doubleValue ());
		return writeValue (value.toString ());
	}

	@Override
	public JsonGenerator write (BigInteger value)
	{
//		assert Debug.debug ("WRITE: VALUE_NUMBER: (BigInteger)" + value);
		if (m_state != GeneratorState.IN_ARRAY)
			throw new JsonGenerationException (ErrorMessage.notInArrayContext);
		if (m_useDouble)
			return writeValue (value.doubleValue ());
		return writeValue (value.toString ());
	}

	@Override
	public JsonGenerator write (int value)
	{
//		assert Debug.debug ("WRITE: VALUE_NUMBER: (int)" + value);
		if (m_state != GeneratorState.IN_ARRAY)
			throw new JsonGenerationException (ErrorMessage.notInArrayContext);
		return writeValue (value);
	}

	@Override
	public JsonGenerator write (long value)
	{
//		assert Debug.debug ("WRITE: VALUE_NUMBER: (long)" + value);
		if (m_state != GeneratorState.IN_ARRAY)
			throw new JsonGenerationException (ErrorMessage.notInArrayContext);
		return writeValue (value);
	}

	@Override
	public JsonGenerator write (double value)
	{
//		assert Debug.debug ("WRITE: VALUE_NUMBER: (double)" + value);
		if (m_state != GeneratorState.IN_ARRAY)
			throw new JsonGenerationException (ErrorMessage.notInArrayContext);
		return writeValue (value);
	}

	@Override
	public JsonGenerator write (boolean value)
	{
//		assert Debug.debug ("WRITE: VALUE_" + (value ? "TRUE" : "FALSE"));
		if (m_state != GeneratorState.IN_ARRAY)
			throw new JsonGenerationException (ErrorMessage.notInArrayContext);
		return writeValue (value);
	}

	@Override
	public JsonGenerator writeNull ()
	{
//		assert Debug.debug ("WRITE: VALUE_NULL");
		if (m_state != GeneratorState.IN_ARRAY)
			throw new JsonGenerationException (ErrorMessage.notInArrayContext);
		return writeNullValue ();
	}

	@Override
	public void close ()
	{
		try
		{
			flush ();
			m_os.close ();
		}
		catch (IOException ex)
		{
			throw new JsonGenerationException (ex.getMessage (), ex);
		}
	}

	@Override
	public void flush ()
	{
		try
		{
			if (m_pos > 0)
			{
				m_os.write (m_buffer, 0, m_pos);
				m_pos = 0;
			}
			m_os.flush ();
		}
		catch (IOException ex)
		{
			throw new JsonGenerationException (ex.getMessage (), ex);
		}
	}

	void pushState (boolean isArray)
	{
		m_state = isArray ? GeneratorState.IN_ARRAY : GeneratorState.IN_OBJECT;
		m_states.add (isArray);
	}

	boolean popState ()
	{
		ArrayList<Boolean> states = m_states;
		int index = states.size () - 1;
		boolean isArray = states.remove (index);
		if (index == 0)
			m_state = GeneratorState.END;
		else
		{
			m_state = states.get (index - 1) ? GeneratorState.IN_ARRAY : GeneratorState.IN_OBJECT;
		}
		return isArray;
	}
}
