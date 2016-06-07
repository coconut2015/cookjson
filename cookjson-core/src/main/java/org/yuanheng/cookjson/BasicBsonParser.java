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
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Stack;

import javax.json.JsonException;
import javax.json.JsonValue;
import javax.json.stream.JsonLocation;

import org.yuanheng.cookjson.value.*;

/**
 * This BSON parser treats Array and Document both as JSON objects.  This is
 * because BSON's root is always a Document type.  As the result, to exactly
 * match JSON's behavior, use BsonParser instead.  This parser only provides
 * very basic document parsing service instead.
 *
 * @author	Heng Yuan
 */
public class BasicBsonParser implements CookJsonParser
{
	private final BsonInputStream m_is;

	private BsonField m_field = new BsonField ();
	private Event m_event;
	private Object m_value;
	private Stack<Integer> m_states = new Stack<Integer> ();
	private int m_state = ParserState.INITIAL;
	private JsonLocationImpl m_location = new JsonLocationImpl ();
	private boolean m_objectIsArray;

	public BasicBsonParser (InputStream is)
	{
		m_is = new BsonInputStream (is);

		// neither column number and line number are meaningful
		// set them to unknown.
		m_location.setColumnNumber (-1);
		m_location.setLineNumber (-1);
	}

	@Override
	public boolean hasNext ()
	{
		return m_state != ParserState.END;
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
			case VALUE_STRING:
			{
				if (m_value instanceof byte[])
					return new CookJsonBinary ((byte[]) m_value);
				return new CookJsonString ((String) m_value);
			}
			case VALUE_NUMBER:
				return new CookJsonNumber ((Number) m_value);
			case VALUE_NULL:
				return CookJsonNull.NULL;
			case VALUE_TRUE:
				return CookJsonBoolean.TRUE;
			case VALUE_FALSE:
				return CookJsonBoolean.FALSE;
			default:
				throw new IllegalStateException ();
		}
	}

	private void getField () throws IOException
	{
		m_location.setStreamOffset (m_is.getLocation ());
		BsonField field = m_field;
		field.type = m_is.readByte () & 0xff;
		if (field.type == 0)
			field.name = null;
		else
			field.name = m_is.readCString ();

		// we treat Code w/ scope as Objects.  Not tested.
		switch (field.type)
		{
			case BsonType.JavaScriptScope:
			{
				// skip the following two fields
				m_is.readInt ();		// size
				m_is.getStringValue ();	// scope?
				getField ();
				break;
			}
			case BsonType.MinKey:
			case BsonType.MaxKey:
			{
				// for min/max key, we simply skip it.
				getField ();
				return;
			}
			default:
			{
				if (field.type >= 128 && field.type <= 255)
				{
					throw new IOException ("Cannot handle user defined type.");
				}
			}
		}
	}

	private Event getEventFromType (int type) throws IOException
	{
		switch (type)
		{
			case 0:
			{
				// make sure that we are closing an object
				if (m_state != ParserState.IN_OBJECT)
					throw new IllegalStateException ();
				popState ();	// closes the current object
				if (m_state == ParserState.IN_FIELD)
				{
					// the object is part of of a field.
					// close this field.
					popState ();
				}
				m_value = null;
				return Event.END_OBJECT;
			}
			case BsonType.Array:	// we handle arrays pretty as objects
			case BsonType.Document:
			{
				if (m_state != ParserState.INITIAL &&
					m_state != ParserState.IN_FIELD)
					throw new IllegalStateException ();
				pushState (ParserState.IN_OBJECT);
				m_value = null;
				m_is.readInt ();	// skip size;
				// sets a temporary flag that indicates the object obtained
				// was internally marked as Array.
				m_objectIsArray = (type == BsonType.Array);
				return Event.START_OBJECT;
			}
			default:
			{
				popState ();	// closes the field
				switch (m_field.type)
				{
					case BsonType.Null:
						m_value = null;
						return Event.VALUE_NULL;
					case BsonType.Double:
						m_value = m_is.readDouble ();
						return Event.VALUE_NUMBER;
					case BsonType.Integer:
						m_value = m_is.readInt ();
						return Event.VALUE_NUMBER;
					case BsonType.DateTime:
					case BsonType.TimeStamp:
					case BsonType.Long:
						m_value = m_is.readLong ();
						return Event.VALUE_NUMBER;
					case BsonType.JavaScript:
					case BsonType.Deprecated:
					case BsonType.String:
						m_value = m_is.getStringValue ();
						return Event.VALUE_STRING;
					case BsonType.Boolean:
						m_value = m_is.readBoolean ();
						return ((Boolean)m_value) ? Event.VALUE_TRUE : Event.VALUE_FALSE;
					case BsonType.ObjectId:
						m_value = m_is.getObjectId ();
						return Event.VALUE_STRING;
					case BsonType.Binary:
						m_value = m_is.getBinary ();
						return Event.VALUE_STRING;
					default:
						throw new IllegalStateException ();	// should not get here.
				}
			}
		}
	}

	@Override
	public Event next ()
	{
		try
		{
//			assert Debug.debug ("-- STATE: " + m_state);
			switch (m_state)
			{
				case ParserState.END:
					throw new NoSuchElementException ();
				case ParserState.INITIAL:
				{
					m_location.setStreamOffset (0);
					// we need to setup as a nameless document
					m_field.type = BsonType.Document;
					m_field.name = null;
					m_event = getEventFromType (m_field.type);
					break;
				}
				case ParserState.IN_FIELD:
				{
					// get the value
					m_event = getEventFromType (m_field.type);
					break;
				}
				case ParserState.IN_OBJECT:
				case ParserState.END_OBJECT:
				{
					getField ();
//					assert Debug.debug ("FIELD: " + m_field);
					if (m_field.name != null)
					{
						pushState (ParserState.IN_FIELD);
						m_event = Event.KEY_NAME;
						m_value = m_field.name;
					}
					else
					{
						m_event = getEventFromType (m_field.type);
					}
					break;
				}
				default:
				{
					throw new IllegalStateException ();
				}
			}
//			assert Debug.debug ("-- EVENT: " + m_event);
			return m_event;
		}
		catch (IOException ex)
		{
			throw new JsonException (ex.getMessage (), ex);
		}
	}

	@Override
	public String getString ()
	{
		switch (m_event)
		{
			case KEY_NAME:
			case VALUE_STRING:
				return (String) m_value;
			case VALUE_NUMBER:
				return m_value.toString ();
			default:
				throw new IllegalStateException ();
		}
	}

	@Override
	public boolean isIntegralNumber ()
	{
		if (m_event != Event.VALUE_NUMBER)
			throw new IllegalStateException ();
		return !(m_value instanceof Double);
	}

	@Override
	public int getInt ()
	{
		if (m_event != Event.VALUE_NUMBER)
			throw new IllegalStateException ();
		return ((Number)m_value).intValue ();
	}

	@Override
	public long getLong ()
	{
		if (m_event != Event.VALUE_NUMBER)
			throw new IllegalStateException ();
		return ((Number)m_value).longValue ();
	}

	@Override
	public BigDecimal getBigDecimal ()
	{
		if (m_event != Event.VALUE_NUMBER)
			throw new IllegalStateException ();
		if (m_value instanceof Integer)
			return new BigDecimal ((Integer)m_value);
		if (m_value instanceof Long)
			return new BigDecimal ((Long)m_value);
		return new BigDecimal ((Double)m_value);
	}

	@Override
	public JsonLocation getLocation ()
	{
		return m_location;
	}

	@Override
	public void close ()
	{
		try
		{
			m_states.clear ();
			m_is.close ();
		}
		catch (IOException ex)
		{
			throw new JsonException (ex.getMessage (), ex);
		}
	}

	/**
	 * Checks if the current Object being dealt with is an array.  It is
	 * only meaningful at START_OBJECT event.
	 * @return	true if the object internally is marked as array.
	 * 			false otherwise.
	 */
	public boolean isObjectIsArray ()
	{
		return m_objectIsArray;
	}
}
