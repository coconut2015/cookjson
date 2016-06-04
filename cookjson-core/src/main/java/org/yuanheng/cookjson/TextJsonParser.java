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
import java.io.Reader;
import java.math.BigDecimal;
import java.util.ArrayList;

import javax.json.JsonException;
import javax.json.JsonValue;
import javax.json.stream.JsonLocation;

import org.yuanheng.cookjson.value.CookJsonBoolean;
import org.yuanheng.cookjson.value.CookJsonNull;
import org.yuanheng.cookjson.value.CookJsonNumber;
import org.yuanheng.cookjson.value.CookJsonString;

/**
 * @author	Heng Yuan
 */
public class TextJsonParser implements CookJsonParser
{
	private final static int START = 1;
	private final static int VALUE = 2;
	private final static int COMMA = 3;
	private final static int COLON = 4;

	private final Reader m_reader;
	/** append buffer for storing output string */
	private char[] m_appendBuf = new char[8192];
	/** position tracking for append buffer */
	private int m_appendPos;

	private long m_line;
	private long m_offset;
	private long m_column;
	private long savedLine;
	private long savedColumn;
	private long savedOffset;

	private char[] m_readBuf = new char[8192];
	private int m_readPos = 0;
	private int m_readMax = 0;

	private final ArrayList<Boolean> m_states = new ArrayList<Boolean> ();
	private int m_state = ParserState.INITIAL;
	private int m_lastToken;

	private Event m_event;
	private String m_string;
	private BigDecimal m_value;

	public TextJsonParser (Reader r)
	{
		m_reader = r;
	}

	private void saveLocation ()
	{
		savedLine = m_line;
		savedOffset = m_offset;
		savedColumn = m_column;
	}

	private void append (char ch)
	{
		int len = m_appendBuf.length;
		if (m_appendPos >= len)
		{
			// we need to expand the buffer by 50%
			char[] newBuffer = new char[len + len / 2];
			System.arraycopy (m_appendBuf, 0, newBuffer, 0, len);
			m_appendBuf = newBuffer;
		}
		m_appendBuf[m_appendPos++] = ch;
	}

	private void stateError ()
	{
		JsonLocation location = getLocation ();
		throw new JsonException ("Line " + location.getLineNumber () + ", column " + location.getColumnNumber () + ", offset " + location.getStreamOffset () + ": invalid token.");
	}

	private void ioError (String msg)
	{
		JsonLocation location = getLocation ();
		throw new JsonException ("Line " + location.getLineNumber () + ", column " + location.getColumnNumber () + ", offset " + location.getStreamOffset () + ": " + msg);
	}

	/**
	 * put the last character read back into the stream.
	 */
	private void unread ()
	{
		--m_readPos;
	}

	private char read () throws IOException
	{
		++m_offset;
		++m_column;
		if (m_readPos >= m_readMax)
			fill ();
		return m_readBuf[m_readPos++];
	}

	private void fill () throws IOException
	{
		m_readPos = 0;
		m_readMax = m_reader.read (m_readBuf);
		if (m_readMax <= 0)
			ioError ("unexpected eof.");
	}

	private void readNull () throws IOException
	{
		char ch;
		ch = read ();
		if (ch != 'u')
			ioError ("expecting 'u'");
		ch = read ();
		if (ch != 'l')
			ioError ("expecting 'l'");
		ch = read ();
		if (ch != 'l')
			ioError ("expecting 'l'");
	}

	private void readTrue () throws IOException
	{
		char ch;
		ch = read ();
		if (ch != 'r')
			ioError ("expecting 'r'");
		ch = read ();
		if (ch != 'u')
			ioError ("expecting 'u'");
		ch = read ();
		if (ch != 'e')
			ioError ("expecting 'e'");
	}

	private void readFalse () throws IOException
	{
		int ch;
		ch = read ();
		if (ch != 'a')
			ioError ("expecting 'a'");
		ch = read ();
		if (ch != 'l')
			ioError ("expecting 'l'");
		ch = read ();
		if (ch != 's')
			ioError ("expecting 's'");
		ch = read ();
		if (ch != 'e')
			ioError ("expecting 'e'");
	}

	private void readExp () throws IOException
	{
		char ch = read ();
		if (ch >= '0' && ch <= '9')
		{
			m_appendBuf[m_appendPos++] = ch;
		}
		else if (ch == '+' || ch == '-')
		{
			m_appendBuf[m_appendPos++] = ch;
			ch = read ();
			if (ch >= '0' && ch <= '9')
			{
				m_appendBuf[m_appendPos++] = ch;
			}
			else
			{
				ioError ("unexpected character '" + ch + "'");
			}
		}
		else
		{
			ioError ("unexpected character '" + ch + "'");
		}
		

		for (;;)
		{
			ch = read ();
			if (ch >= '0' && ch <= '9')
			{
				m_appendBuf[m_appendPos++] = ch;
				continue;
			}
			unread ();
			return;
		}
	}

	private void readNumber (char firstChar) throws IOException
	{
		m_string = null;
		m_value = null;
		m_appendBuf[0] = firstChar;
		m_appendPos = 1;

		boolean hasFrac = false;

		char[] buf = m_readBuf;
		for (;;)
		{
			int readPos = m_readPos;
			int readMax = m_readMax;
			while (readPos < readMax)
			{
				char ch = buf[readPos++];
				if (ch >= '0')
				{
					if (ch <= '9')
					{
						m_appendBuf[m_appendPos++] = ch;
						continue;
					}
					else if (ch == 'E' || ch == 'e')
					{
						if (firstChar == '-' && m_appendPos == 1)
							ioError ("unexpected character '" + ch + "'");
						m_appendBuf[m_appendPos++] = ch;
						int len = readPos - m_readPos;
						m_readPos = readPos;
						m_column += len;
						m_offset += len;
						readExp ();
						return;
					}
				}
				else if (ch == '.')
				{
					if (hasFrac)
						ioError ("unexpected character '.'");
					hasFrac = true;
					m_appendBuf[m_appendPos++] = ch;
					continue;
				}
				int len = readPos - m_readPos;
				m_readPos = readPos - 1;
				m_column += len - 1;
				m_offset += len - 1;
				return;
			}
			fill ();
		}
	}

	private void readEscape () throws IOException
	{
		char ch = read ();
		switch (ch)
		{
			case 'b':
				append ('\b');
				break;
			case 'f':
				append ('\f');
				break;
			case 'n':
				append ('\n');
				break;
			case 'r':
				append ('\r');
				break;
			case 't':
				append ('\t');
				break;
			case '\\':
			case '/':
			case '"':
				append (ch);
				break;
			case 'u':
			{
				int val = 0;
				for (int i = 0; i < 4; ++i)
				{
					ch = read ();
					if (ch >= '0')
					{
						if (ch <= '9')
						{
							int hex = ch - '0';
							val = (val << 4) | hex;
							continue;
						}
						else if (ch >= 'A')
						{
							if (ch <= 'F')
							{
								int hex = ch - 'A' + 10;
								val = (val << 4) | hex;
								continue;
							}
							else if (ch >= 'a')
							{
								if (ch <= 'f')
								{
									int hex = ch - 'a' + 10;
									val = (val << 4) | hex;
									continue;
								}
							}
						}
					}
					ioError ("unexpected character '" + ch + "'");
				}
				append ((char) val);
				break;
			}
			default:
				ioError ("unknown escape sequence '\\" + ch + "'");
		}
	}

	private void readString () throws IOException
	{
		m_string = null;
		m_appendPos = 0;

		char[] buf = m_readBuf;
		for (;;)
		{
			int readPos = m_readPos;
			int readMax = m_readMax;
			while (readPos < readMax)
			{
				char ch = buf[readPos++];
				++m_offset;
				++m_column;
				switch (ch)
				{
					case '\n':
						m_column = 0;
						++m_line;
						append (ch);
						break;
					case '"':
						m_readPos = readPos;
						return;
					case '\\':
						m_readPos = readPos;
						readEscape ();
						readPos = m_readPos;
						readMax = m_readMax;
						break;
					default:
						append (ch);
						break;
				}
			}
			fill ();
		}
	}

	private String getBufferString ()
	{
		if (m_string == null)
			m_string = new String (m_appendBuf, 0, m_appendPos);
		return m_string;
	}

	@Override
	public Event getEvent ()
	{
		return m_event;
	}

	@Override
	public JsonValue getValue ()
	{
		switch (m_event)
		{
			case START_ARRAY:
			case START_OBJECT:
				return Utils.getValue (this);
			case END_ARRAY:
			case END_OBJECT:
			case KEY_NAME:
				stateError ();
			case VALUE_TRUE:
			case VALUE_FALSE:
				return CookJsonBoolean.FALSE;
			case VALUE_NULL:
				return CookJsonNull.NULL;
			case VALUE_NUMBER:
				return new CookJsonNumber (getBigDecimal ());
			case VALUE_STRING:
				return new CookJsonString (getString ());
		}
		stateError ();
		return null;	// should not get here.
	}

	@Override
	public Event next ()
	{
		if (m_state == ParserState.END)
			stateError ();
		try
		{
			for (;;)
			{
				char ch = read ();
				switch (ch)
				{
					case '[':
					{
						checkValueState (true);
						pushState (true);
						m_event = Event.START_ARRAY;
						m_lastToken = START;
						return m_event;
					}
					case ']':
					{
						if (m_state != ParserState.IN_ARRAY)
							stateError ();
						if (m_states.isEmpty ())
							m_state = ParserState.END;
						popState ();
						m_event = Event.END_ARRAY;
						m_lastToken = VALUE;
						return m_event;
					}
					case '{':
					{
						checkValueState (true);
						pushState (false);
						m_event = Event.START_OBJECT;
						m_lastToken = START;
						return m_event;
					}
					case '}':
					{
						if (m_state != ParserState.IN_OBJECT)
							stateError ();
						if (m_states.isEmpty ())
							m_state = ParserState.END;
						popState ();
						m_event = Event.END_ARRAY;
						m_lastToken = VALUE;
						return m_event;
					}
					case 'n':
					{
						checkValueState (true);
						readNull ();
						m_event = Event.VALUE_NULL;
						m_lastToken = VALUE;
						return m_event;
					}
					case 't':
					{
						checkValueState (true);
						readTrue ();
						m_event = Event.VALUE_TRUE;
						m_lastToken = VALUE;
						return m_event;
					}
					case 'f':
					{
						checkValueState (true);
						readFalse ();
						m_event = Event.VALUE_FALSE;
						m_lastToken = VALUE;
						return m_event;
					}
					case '-':
					case '0':
					case '1':
					case '2':
					case '3':
					case '4':
					case '5':
					case '6':
					case '7':
					case '8':
					case '9':
					{
						checkValueState (true);
						readNumber (ch);
						m_event = Event.VALUE_NUMBER;
						m_lastToken = VALUE;
						return m_event;
					}
					case ',':
					{
						if (m_lastToken != VALUE)
							stateError ();
						m_lastToken = COMMA;
						break;
					}
					case ':':
					{
						if (m_event != Event.KEY_NAME ||
							m_lastToken != VALUE)
							stateError ();
						m_lastToken = COLON;
						break;
					}
					case '"':
					{
						saveLocation ();
						readString ();

						if (m_state == ParserState.IN_OBJECT)
						{
							if (m_lastToken == START ||
								m_lastToken == COMMA)
							{
								m_event = Event.KEY_NAME;
								m_lastToken = VALUE;
								return m_event;
							}
							else if (m_lastToken == COLON)
							{
								m_event = Event.VALUE_STRING;
								m_lastToken = VALUE;
								return m_event;
							}
						}
						else if (m_state == ParserState.IN_ARRAY)
						{
							if (m_lastToken == START ||
								m_lastToken == COMMA)
							{
								m_event = Event.VALUE_STRING;
								m_lastToken = VALUE;
								return m_event;
							}
						}
						stateError ();
					}
					case ' ':
					case '\t':
					case '\r':
						break;
					case '\n':
						m_column = 0;
						++m_line;
						break;
					default:
						ioError ("unexpected character '" + ch + "'");
				}
			}
		}
		catch (IOException ex)
		{
			throw new JsonException (ex.getMessage (), ex);
		}
	}

	private void checkValueState (boolean initial)
	{
		switch (m_state)
		{
			case ParserState.INITIAL:
				if (!initial)
					stateError ();
				break;
			case ParserState.IN_ARRAY:
				if (m_lastToken != START &&
					m_lastToken != COMMA)
					stateError ();
				break;
			case ParserState.IN_OBJECT:
				if (m_lastToken != COLON)
					stateError ();
				break;
		}
	}

	@Override
	public boolean hasNext ()
	{
		return m_state != ParserState.END;
	}

	@Override
	public boolean isIntegralNumber ()
	{
		return getBigDecimal ().scale () <= 0;
	}

	@Override
	public int getInt ()
	{
		if (m_event != Event.VALUE_NUMBER)
			stateError ();
		if (m_value == null)
			return Integer.valueOf (getBufferString ());
		return m_value.intValue ();
	}

	@Override
	public long getLong ()
	{
		if (m_event != Event.VALUE_NUMBER)
			stateError ();
		if (m_value == null)
			return Long.valueOf (getBufferString ());
		return m_value.longValue ();
	}

	@Override
	public BigDecimal getBigDecimal ()
	{
		if (m_event != Event.VALUE_NUMBER)
			stateError ();
		if (m_value == null)
			m_value = new BigDecimal (getBufferString ());
		return m_value;
	}

	@Override
	public JsonLocation getLocation ()
	{
		JsonLocationImpl location = new JsonLocationImpl ();
		long diff = 0;
		switch (m_event)
		{
			case START_OBJECT:
			case START_ARRAY:
			case END_OBJECT:
			case END_ARRAY:
				diff = 1;
				break;
			case VALUE_NUMBER:
				diff = m_appendPos;
				break;
			case KEY_NAME:
			case VALUE_STRING:
			{
				location.setColumnNumber (savedColumn - 1);	// minus 1 to account for '"'
				location.setStreamOffset (savedOffset - 1);	// minus 1 to account for '"'
				location.setLineNumber (savedLine);
				return location;
			}
			case VALUE_FALSE:
				diff = 5;
				break;
			case VALUE_NULL:
			case VALUE_TRUE:
				diff = 4;
				break;
		}
		location.setColumnNumber (m_column - diff);
		location.setStreamOffset (m_offset - diff);
		location.setLineNumber (m_line);
		return location;
	}

	@Override
	public String getString ()
	{
		if (m_event != Event.VALUE_STRING &&
			m_event != Event.VALUE_NUMBER &&
			m_event != Event.KEY_NAME)
			stateError ();
		return getBufferString ();
	}

	@Override
	public void close ()
	{
		try
		{
			m_reader.close ();
		}
		catch (IOException ex)
		{
			throw new JsonException (ex.getMessage (), ex);
		}
	}

	private void pushState (boolean isArray)
	{
		if (m_state != ParserState.INITIAL)
			m_states.add (Boolean.valueOf (m_state == ParserState.IN_ARRAY));
		m_state = isArray ? ParserState.IN_ARRAY : ParserState.IN_OBJECT;
	}

	private void popState ()
	{
		if (m_states.isEmpty ())
			m_state = ParserState.END;
		else
		{
			boolean b = m_states.remove (m_states.size () - 1);
			m_state = b ? ParserState.IN_ARRAY : ParserState.IN_OBJECT;
		}
	}
}
