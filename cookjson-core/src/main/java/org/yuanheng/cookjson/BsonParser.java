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

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Stack;

import javax.json.JsonValue;
import javax.json.stream.JsonLocation;

/**
 * This JsonParser is used to handle the Array behavior of BSON.
 * <p/>
 * In BSON, arrays are stored as objects with "0", "1", ... etc as KEY_NAME.
 * So we want to detect this behavior by checking if "0" is the first
 * element's KEY_NAME.  If so, we return array type instead of object type.
 *
 * @author	Heng Yuan
 */
public class BsonParser implements CookJsonParser
{
	private final CookJsonParser m_parser;
	private Stack<Boolean> m_inArrayStack = new Stack<Boolean> ();
	private Event m_event;
	private String m_keyName;
	private Event m_emptyObject;

	public BsonParser (InputStream is)
	{
		m_parser = new BasicBsonParser (is);
	}

	public BsonParser (CookJsonParser parser)
	{
		m_parser = parser;
	}

	@Override
	public boolean hasNext ()
	{
		if (m_keyName == null)
			return m_parser.hasNext ();
		return true;
	}

	@Override
	public Event getEvent ()
	{
		return m_event;
	}

	@Override
	public Event next ()
	{
		// clear the saved keyName when we move pass it.
		if (m_event == Event.KEY_NAME &&
			m_keyName != null)
		{
			m_keyName = null;
		}

		if (m_event == Event.START_OBJECT &&
			m_keyName != null)
		{
			// use the saved key name
			m_event = Event.KEY_NAME;
		}
		else if (m_emptyObject != null &&
				 (m_event == Event.START_OBJECT ||
				  m_event == Event.START_ARRAY))
		{
			m_event = m_emptyObject;
			m_emptyObject = null;
		}
		else
		{
			Event e = m_parser.next ();

			switch (e)
			{
				case START_OBJECT:
				{
					// check if we are dealing with an array
					Event e2 = m_parser.next ();
					if (e2 != Event.KEY_NAME)
					{
						if (e2 == Event.END_OBJECT)
						{
							if ((m_parser instanceof BasicBsonParser) &&
								((BasicBsonParser)m_parser).isObjectIsArray ())
							{
								e = Event.START_ARRAY;
								m_emptyObject = Event.END_ARRAY;
							}
							else
							{
								m_emptyObject = Event.END_OBJECT;
							}
							break;
						}
						else
							throw new IllegalStateException ();
					}
					String keyName = m_parser.getString ();
					if ("0".equals (keyName))
					{
						m_keyName = null;
						e = Event.START_ARRAY;
						m_inArrayStack.push (Boolean.TRUE);
					}
					else
					{
						m_keyName = keyName;
						e = Event.START_OBJECT;
						m_inArrayStack.push (Boolean.FALSE);
					}
					break;
				}
				case KEY_NAME:
				{
					if (Boolean.TRUE == m_inArrayStack.peek ())
					{
						return next ();
					}
					break;
				}
				case END_ARRAY:
				case END_OBJECT:
				{
					m_inArrayStack.pop ();
					break;
				}
				default:
					break;
			}

			m_event = e;
		}
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
			default:
				return m_parser.getValue ();
		}
	}

	@Override
	public String getString ()
	{
		if (m_event == Event.KEY_NAME &&
			m_keyName != null)
		{
			return m_keyName;
		}
		else if (m_event == Event.START_OBJECT &&
				 m_keyName != null)
		{
			throw new IllegalStateException ();
		}
		return m_parser.getString ();
	}

	@Override
	public boolean isIntegralNumber ()
	{
		return m_parser.isIntegralNumber ();
	}

	@Override
	public int getInt ()
	{
		return m_parser.getInt ();
	}

	@Override
	public long getLong ()
	{
		return m_parser.getLong ();
	}

	@Override
	public BigDecimal getBigDecimal ()
	{
		return m_parser.getBigDecimal ();
	}

	@Override
	public JsonLocation getLocation ()
	{
		return m_parser.getLocation ();
	}

	@Override
	public void close ()
	{
		m_parser.close ();
	}
}
