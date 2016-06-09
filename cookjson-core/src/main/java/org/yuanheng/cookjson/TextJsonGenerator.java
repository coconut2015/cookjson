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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Map;

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
	 * If the name is already being escaped.
	 */
	boolean m_keyNameEscaped;
	/**
	 * The output writer.
	 */
	final Writer m_out;
	/**
	 * Saved state.
	 */
	final ArrayList<Boolean> m_states = new ArrayList<Boolean> ();
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

	/**
	 * Internal write buffer.  We manually buffer it rather than
	 * using BufferedWriter due to slight performance improvement.
	 */
	final static int m_max = 8192;
	char[] m_buffer = new char[m_max + 1];	// +1 so that our single char padding logic is simpler
	/** Buffer position */
	int m_pos;

	final static int m_valueLen = 13;
	char[] m_valueBuffer;

	public TextJsonGenerator (OutputStream os)
	{
		m_out = new OutputStreamWriter (os, BOM.utf8);
	}

	public TextJsonGenerator (Writer out)
	{
		m_out = out;
	}

	void writeComma () throws IOException
	{
		if (m_first)
			m_first = false;
		else
			w (',');
	}

	void writeName (String name) throws IOException
	{
		if (m_first)
			m_first = false;
		else
			w (',');
		if (m_keyNameEscaped)
			w (name);
		else
			quote (name);
		w (':');
	}

	void quote (String str) throws IOException
	{
		int strLength = str.length ();
		char[] esc = m_valueBuffer;
		int start = 0;
		int i;
		w ('"');
		for (i = 0; i < strLength; ++i)
		{
			char ch = str.charAt (i);
			// most frequent chars are lower case letters.
			if (ch > '\\')
				continue;
			// upper case letters and numbers are pretty frequent too
			else if (ch > '"')
			{
				if (ch < '\\')
					continue;
				// handle '\\' case
				int len = i - start + 1;
				w (str, start, len);
				start = i;
			}
			else if (ch >= ' ')
			{
				if (ch < '"')
					continue;
				// handle '"' case
				int len = i - start;
				if (len > 0)
				{
					w (str, start, len);
				}
				w ('\\');
				start = i;
			}
			else
			{
				int len = i - start;
				if (len > 0)
				{
					w (str, start, len);
				}
				start = i + 1;
				// handle the escape sequences
				if (esc == null)
				{
					esc = new char[m_valueLen];
					m_valueBuffer = esc;
					esc[0] = '\\';
					esc[2] = '0';
					esc[3] = '0';
				}
				switch (ch)
				{
					case '\b':	// 0x08
					{
						esc[1] = 'b';
						w (esc, 0, 2);
						break;
					}
					case '\t':	// 0x09
					{
						esc[1] = 't';
						w (esc, 0, 2);
						break;
					}
					case '\n':	// 0x0a
					{
						esc[1] = 'n';
						w (esc, 0, 2);
						break;
					}
					case '\r':	// 0x0d
					{
						esc[1] = 'r';
						w (esc, 0, 2);
						break;
					}
					case '\f':	// 0x0c
					{
						esc[1] = 'f';
						w (esc, 0, 2);
						break;
					}
					default:
					{
						esc[1] = 'u';
						esc[4] = Quote.hex[(ch >> 4) & 0x0f];
						esc[5] = Quote.hex[ch & 0x0f];
						w (esc, 0, 6);
						break;
					}
				}
			}
		}
		int len = i - start;
		if (len > 0)
		{
			w (str, start, len);
		}
		w ('"');
	}

	/* chars length is very small comparing to m_max */
	void w (char[] chars, int offset, int length) throws IOException
	{
		int pos = m_pos;
		char[] buf = m_buffer;
		if (pos + length < m_max)
		{
			for (int i = 0; i < length; ++i)
				buf[pos++] = chars[offset++];
			m_pos = pos;
			return;
		}
		{
			int len = m_max - pos;
			for (int i = 0; i < len; ++i)
				buf[pos++] = chars[offset++];
			m_out.write (buf, 0, m_max);
			length -= len;
		}
		while (length > m_max)
		{
			m_out.write (chars, offset, m_max);
			offset += m_max;
			length -= m_max;
		}

		for (int i = 0; i < length; ++i)
			buf[i] = chars[i];
		m_pos = pos;
	}

	void w (String str) throws IOException
	{
		w (str, 0, str.length ());
	}

	void w (String str, int offset, int length) throws IOException
	{
		int pos = m_pos;
		char[] buf = m_buffer;
		if (pos + length < m_max)
		{
			str.getChars (offset, offset + length, buf, pos);
			m_pos += length;
			return;
		}
		{
			int len = m_max - pos;
			str.getChars (offset, offset + len, buf, pos);
			m_out.write (buf, 0, m_max);
			offset += len;
			length -= len;
			pos = 0;
		}
		while (length > m_max)
		{
			m_out.write (str, offset, m_max);
			offset += m_max;
			length -= m_max;
		}
		if (length > 0)
		{
			str.getChars (offset, offset + length, buf, 0);
		}
		m_pos = length;
	}

	void w (char ch) throws IOException
	{
		char[] buf = m_buffer;
		int pos = m_pos;
		buf[pos++] = ch;
		if (pos >= buf.length)
		{
			m_out.write (buf, 0, pos);
			m_pos = 0;
		}
		else
		{
			m_pos = pos;
		}
	}

	void wi (int value) throws IOException
	{
		char[] buf = m_valueBuffer;
		if (buf == null)
		{
			buf = new char[m_valueLen];
			m_valueBuffer = buf;
		}

		int pos = m_valueLen;
		boolean negative;
		if (value < 0)
		{
			if (value == Integer.MIN_VALUE)
			{
				// this value cannot be negated.  So just print it out
				// and return.
				w ("-2147483648");
				return;
			}
			negative = true;
			value = -value;
		}
		else
			negative = false;
		char[] digits = Quote.hex;
		// use do-while to generate at least 1 digit.
		do
		{
			int v = value / 10;
			int r = value - v * 10;
			value = v;
			buf[--pos] = digits[r];
		}
		while (value > 0);
		if (negative)
			buf[--pos] = '-';
		w (buf, pos, m_valueLen - pos);
	}

	private JsonGenerator writeValue (JsonValue value)
	{
		try
		{
			switch (value.getValueType ())
			{
				case ARRAY:
				{
					JsonArray array = (JsonArray) value;
					w ('[');
					m_first = true;
					for (JsonValue v : array)
					{
						if (m_first)
							m_first = false;
						else
							w (',');
						if (v == null)
							w ("null");
						else
							writeValue (v);
					}
					w (']');
					m_first = false;
					break;
				}
				case OBJECT:
				{
					JsonObject obj = (JsonObject) value;
					w ('{');
					m_first = true;
					for (Map.Entry<String, JsonValue> entry : obj.entrySet ())
					{
						JsonValue v = entry.getValue ();
						writeName (entry.getKey ());
						if (v == null)
							w ("null");
						else
							writeValue (v);
					}
					w ('}');
					m_first = false;
					break;
				}
				case NULL:
				{
					w ("null");
					break;
				}
				case NUMBER:
				{
					w (value.toString ());
					break;
				}
				case STRING:
				{
					quote (value.toString ());
					break;
				}
				case TRUE:
				{
					w ("true");
					break;
				}
				case FALSE:
				{
					w ("false");
					break;
				}
			}
		}
		catch (IOException ex)
		{
			throw new JsonException (ex.getMessage (), ex);
		}
		return this;
	}

	@Override
	public JsonGenerator writeStartObject ()
	{
		try
		{
			writeComma ();
			w ('{');
			pushState (false);
			m_first = true;
			return this;
		}
		catch (IOException ex)
		{
			throw new JsonException (ex.getMessage (), ex);
		}
	}

	@Override
	public JsonGenerator writeStartObject (String name)
	{
		try
		{
			writeName (name);
			w ('{');
			pushState (false);
			m_first = true;
			return this;
		}
		catch (IOException ex)
		{
			throw new JsonException (ex.getMessage (), ex);
		}
	}

	@Override
	public JsonGenerator writeStartArray ()
	{
		try
		{
			writeComma ();
			w ('[');
			pushState (true);
			m_first = true;
			return this;
		}
		catch (IOException ex)
		{
			throw new JsonException (ex.getMessage (), ex);
		}
	}

	@Override
	public JsonGenerator writeStartArray (String name)
	{
		try
		{
			writeName (name);
			w ('[');
			pushState (true);
			m_first = true;
			return this;
		}
		catch (IOException ex)
		{
			throw new JsonException (ex.getMessage (), ex);
		}
	}

	@Override
	public JsonGenerator write (String name, JsonValue value)
	{
		try
		{
			writeName (name);
			writeValue (value);
			return this;
		}
		catch (IOException ex)
		{
			throw new JsonException (ex.getMessage (), ex);
		}
	}

	@Override
	public JsonGenerator write (String name, String value)
	{
		try
		{
			writeName (name);
			quote (value);
			return this;
		}
		catch (IOException ex)
		{
			throw new JsonException (ex.getMessage (), ex);
		}
	}

	@Override
	public JsonGenerator write (String name, BigInteger value)
	{
		try
		{
			writeName (name);
			w (value.toString ());
			return this;
		}
		catch (IOException ex)
		{
			throw new JsonException (ex.getMessage (), ex);
		}
	}

	@Override
	public JsonGenerator write (String name, BigDecimal value)
	{
		try
		{
			writeName (name);
			w (value.toString ());
			return this;
		}
		catch (IOException ex)
		{
			throw new JsonException (ex.getMessage (), ex);
		}
	}

	@Override
	public JsonGenerator write (String name, int value)
	{
		try
		{
			writeName (name);
			wi (value);
			return this;
		}
		catch (IOException ex)
		{
			throw new JsonException (ex.getMessage (), ex);
		}
	}

	@Override
	public JsonGenerator write (String name, long value)
	{
		try
		{
			writeName (name);
			w (Long.toString (value));
			return this;
		}
		catch (IOException ex)
		{
			throw new JsonException (ex.getMessage (), ex);
		}
	}

	@Override
	public JsonGenerator write (String name, double value)
	{
		try
		{
			writeName (name);
			w (Double.toString (value));
			return this;
		}
		catch (IOException ex)
		{
			throw new JsonException (ex.getMessage (), ex);
		}
	}

	@Override
	public JsonGenerator write (String name, boolean value)
	{
		try
		{
			writeName (name);
			w (value ? "true" : "false");
			return this;
		}
		catch (IOException ex)
		{
			throw new JsonException (ex.getMessage (), ex);
		}
	}

	@Override
	public JsonGenerator writeNull (String name)
	{
		try
		{
			writeName (name);
			w ("null");
			return this;
		}
		catch (IOException ex)
		{
			throw new JsonException (ex.getMessage (), ex);
		}
	}

	@Override
	public JsonGenerator writeEnd ()
	{
		boolean isArray = popState ();
		m_first = false;
		char ch = isArray ? ']' : '}';
		try
		{
			w (ch);
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
		return writeValue (value);
	}

	@Override
	public JsonGenerator write (String value)
	{
		try
		{
			writeComma ();
			quote (value);
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		return this;
	}

	@Override
	public JsonGenerator write (BigDecimal value)
	{
		try
		{
			writeComma ();
			w (value.toString ());
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		return this;
	}

	@Override
	public JsonGenerator write (BigInteger value)
	{
		try
		{
			writeComma ();
			w (value.toString ());
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		return this;
	}

	@Override
	public JsonGenerator write (int value)
	{
		try
		{
			writeComma ();
			wi (value);
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		return this;
	}

	@Override
	public JsonGenerator write (long value)
	{
		try
		{
			writeComma ();
			w (Long.toString (value));
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		return this;
	}

	@Override
	public JsonGenerator write (double value)
	{
		try
		{
			writeComma ();
			w (Double.toString (value));
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		return this;
	}

	@Override
	public JsonGenerator write (boolean value)
	{
		try
		{
			writeComma ();
			w (value ? "true" : "false");
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		return this;
	}

	@Override
	public JsonGenerator writeNull ()
	{
		try
		{
			writeComma ();
			w ("null");
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		return this;
	}

	@Override
	public void close ()
	{
		try
		{
			flush ();
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
			if (m_pos > 0)
			{
				m_out.write (m_buffer, 0, m_pos);
				m_pos = 0;
			}
			m_out.flush ();
		}
		catch (IOException ex)
		{
			throw new JsonException (ex.getMessage (), ex);
		}
	}

	void pushState (boolean isArray)
	{
		m_states.add (isArray);
	}

	boolean popState ()
	{
		if (m_states.isEmpty ())
			m_state = GeneratorState.END;
		return m_states.remove (m_states.size () - 1);
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
