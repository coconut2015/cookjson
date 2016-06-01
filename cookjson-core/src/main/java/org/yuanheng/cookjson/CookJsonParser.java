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
import java.util.Stack;

import javax.json.JsonException;
import javax.json.stream.JsonLocation;
import javax.json.stream.JsonParser;

/**
 * @author	Heng Yuan
 */
public class CookJsonParser extends JsonScan implements JsonParser
{
	private final Stack<Integer> m_states = new Stack<Integer> ();
	private int m_state = ParserState.INITIAL;

	private boolean m_firstElement = true;
	private int m_lastToken;

	private Event m_event;

	public CookJsonParser (Reader r)
	{
		super (r);
	}
	/**
	 * Get the string inside a pair of double quotes.
	 *
	 * @param	str
	 * 			double quoted string.
	 * @return	the generated string.
	 */
	@Override
	public Event next ()
	{
		if (m_state == ParserState.END)
			throw new IllegalStateException ();
		try
		{
			boolean readAgain;
			do
			{
				readAgain = false;
				int token = yyLex ();
				switch (token)
				{
					case START_ARRAY:
					{
						checkValueState (true);
						m_firstElement = true;
						pushState (ParserState.IN_ARRAY);
						m_event = Event.START_ARRAY;
						break;
					}
					case END_ARRAY:
					{
						if (m_state != ParserState.IN_ARRAY)
							throw new IllegalStateException ();
						if (m_states.isEmpty ())
							m_state = ParserState.END;
						popState ();
						m_firstElement = false;
						m_event = Event.END_ARRAY;
						break;
					}
					case START_OBJECT:
					{
						checkValueState (true);
						m_firstElement = true;
						pushState (ParserState.IN_OBJECT);
						m_event = Event.START_OBJECT;
						break;
					}
					case END_OBJECT:
					{
						if (m_state != ParserState.IN_OBJECT)
							throw new IllegalStateException ();
						if (m_states.isEmpty ())
							m_state = ParserState.END;
						popState ();
						m_firstElement = false;
						m_event = Event.END_ARRAY;
						break;
					}
					case VALUE_STRING:
					{
						if (m_state == ParserState.IN_OBJECT &&
							((m_firstElement && m_lastToken == START_OBJECT) ||
							 (!m_firstElement && m_lastToken == COMMA)))
						{
							pushState (ParserState.IN_FIELD);
							m_event = Event.KEY_NAME;
						}
						else
						{
							checkValueState (false);
							m_event = Event.VALUE_STRING;
						}
						break;
					}
					case JsonScan.VALUE_NULL:
					{
						checkValueState (false);
	
						m_event = Event.VALUE_NULL;
						break;
					}
					case JsonScan.VALUE_TRUE:
					{
						checkValueState (false);
	
						m_event = Event.VALUE_TRUE;
						break;
					}
					case JsonScan.VALUE_FALSE:
					{
						checkValueState (false);
	
						m_event = Event.VALUE_FALSE;
						break;
					}
					case JsonScan.VALUE_NUMBER:
					{
						checkValueState (false);
	
						m_event = Event.VALUE_NUMBER;
						break;
					}
					case JsonScan.COMMA:
					{
						if (m_firstElement ||
							(m_state != ParserState.IN_ARRAY &&
							 m_state != ParserState.IN_OBJECT) ||
							m_lastToken == JsonScan.COMMA ||
							m_lastToken == JsonScan.COLON)
							throw new IllegalStateException ();
						readAgain = true;
						break;
					}
					case JsonScan.COLON:
					{
						if (m_state != ParserState.IN_FIELD ||
							m_lastToken == JsonScan.COMMA ||
							m_lastToken == JsonScan.COLON)
							throw new IllegalStateException ();
						readAgain = true;
						break;
					}
					default:
						throw new IllegalStateException ();
				}
				m_lastToken = token;
			}
			while (readAgain);
		}
		catch (IOException ex)
		{
			throw new JsonException (ex.getMessage (), ex);
		}
		return m_event;
	}

	private void checkValueState (boolean allowInitial)
	{
		if (m_state == ParserState.INITIAL)
		{
			if (allowInitial && m_firstElement && m_lastToken == 0)
				;	// ok
			else
				throw new IllegalStateException ();
		}
		else if (m_state == ParserState.IN_ARRAY)
		{
			if (m_firstElement && m_lastToken == START_ARRAY)
				;	// ok
			else if (!m_firstElement && m_lastToken == COMMA)
				;	// ok
			else
				throw new IllegalStateException ();
		}
		else
		{
			if (m_state == ParserState.IN_FIELD &&
				m_lastToken == COLON)
			{
				// okay;
				popState ();
			}
			else
				throw new IllegalStateException ();
		}
		m_firstElement = false;
	}

	@Override
	public boolean hasNext ()
	{
		return m_state != ParserState.END;
	}

	@Override
	public boolean isIntegralNumber ()
	{
		if (m_event != Event.VALUE_NUMBER)
			throw new IllegalStateException ();
		return m_value.indexOf ('.') < 0;
	}

	@Override
	public int getInt ()
	{
		if (m_event != Event.VALUE_NUMBER)
			throw new IllegalStateException ();
		return Integer.valueOf (m_value);
	}

	@Override
	public long getLong ()
	{
		if (m_event != Event.VALUE_NUMBER)
			throw new IllegalStateException ();
		return Long.valueOf (m_value);
	}

	@Override
	public BigDecimal getBigDecimal ()
	{
		if (m_event != Event.VALUE_NUMBER)
			throw new IllegalStateException ();
		return new BigDecimal (m_value);
	}

	@Override
	public JsonLocation getLocation ()
	{
		JsonLocationImpl location = new JsonLocationImpl ();
		location.setColumnNumber (m_savedColumn);
		location.setLineNumber (m_savedLine);
		location.setStreamOffset (m_savedOffset);
		return location;
	}

	@Override
	public String getString ()
	{
		if (m_event != Event.VALUE_STRING &&
			m_event != Event.VALUE_NUMBER &&
			m_event != Event.KEY_NAME)
			throw new IllegalStateException ();
		return m_value;
	}

	@Override
	public void close ()
	{
		try
		{
			super.close ();
		}
		catch (IOException ex)
		{
			throw new JsonException (ex.getMessage (), ex);
		}
	}

	void pushState (int state)
	{
		if (m_state == ParserState.INITIAL)
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
			m_state = ParserState.END;
		else
			m_state = m_states.pop ();
		return oldState;
	}
}
