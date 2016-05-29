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
import java.util.Stack;

import javax.json.JsonException;
import javax.json.JsonNumber;
import javax.json.JsonValue;
import javax.json.stream.JsonLocation;
import javax.json.stream.JsonParser;

import org.yuanheng.cookjson.value.CookJsonBinary;
import org.yuanheng.cookjson.value.CookJsonBoolean;
import org.yuanheng.cookjson.value.CookJsonNumber;
import org.yuanheng.cookjson.value.CookJsonString;

/**
 * @author	Heng Yuan
 */
public class BasicBsonParser implements JsonParser
{
	private final BsonInputStream m_is;

	private BsonField m_field = new BsonField ();
	private Event m_event;
	private JsonValue m_value;
	private Stack<Integer> m_states = new Stack<Integer> ();
	private int m_state = ParserState.INITIAL;
	private JsonLocationImpl m_location = new JsonLocationImpl ();

	public BasicBsonParser (InputStream is)
	{
		m_is = new BsonInputStream (is);
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

	public JsonValue getValue ()
	{
		try
		{
			switch (m_field.type)
			{
				case BsonType.Double:
					return new CookJsonNumber (m_is.readDouble ());
				case BsonType.Integer:
					return new CookJsonNumber (m_is.readInt ());
				case BsonType.DateTime:
				case BsonType.TimeStamp:
				case BsonType.Long:
					return new CookJsonNumber (m_is.readLong ());
				case BsonType.JavaScript:
				case BsonType.Deprecated:
				case BsonType.String:
					return new CookJsonString (m_is.getStringValue ());
				case BsonType.Boolean:
					return m_is.readBoolean () ? CookJsonBoolean.TRUE : CookJsonBoolean.FALSE;
				case BsonType.ObjectId:
					return new CookJsonBinary (m_is.getObjectId ());
				case BsonType.Binary:
					return new CookJsonBinary (m_is.getBinary ());
			}
		}
		catch (IOException ex)
		{
			throw new JsonException (ex.getMessage (), ex);
		}
		throw new JsonException ("Cannot handle the BSON type.");
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
				m_event = Event.END_OBJECT;
				m_value = null;
				break;
			}
			case BsonType.Array:	// we handle arrays pretty as objects
			case BsonType.Document:
			{
				if (m_state != ParserState.INITIAL &&
					m_state != ParserState.IN_FIELD)
					throw new IllegalStateException ();
				pushState (ParserState.IN_OBJECT);
				m_event = Event.START_OBJECT;
				m_value = null;
				m_is.readInt ();	// skip size;
				break;
			}
			default:
			{
				// makes sure that we are dealing with values.
				if (m_state != ParserState.IN_FIELD)
					throw new IllegalStateException ();
				popState ();	// closes the field
				m_value = getValue ();
				switch (m_value.getValueType ())
				{
					case TRUE:
						m_event = Event.VALUE_TRUE;
						break;
					case FALSE:
						m_event = Event.VALUE_FALSE;
						break;
					case NUMBER:
						m_event = Event.VALUE_NUMBER;
						break;
					case NULL:
						m_event = Event.VALUE_NULL;
						break;
					case STRING:
						m_event = Event.VALUE_STRING;
						break;
					default:
						throw new IllegalStateException ();	// should not get here.
				}
			}
		}
		return m_event;
	}

	@Override
	public Event next ()
	{
		if (!hasNext ())
			throw new IllegalStateException ();
		try
		{
			assert Debug.debug ("-- STATE: " + m_state);
			switch (m_state)
			{
				case ParserState.INITIAL:
				{
					m_location.setStreamOffset (0);
					// we need to setup as a nameless document
					m_field.type = BsonType.Document;
					m_field.name = null;
					return getEventFromType (m_field.type);
				}
				case ParserState.IN_FIELD:
				{
					// get the value
					return getEventFromType (m_field.type);
				}
				case ParserState.IN_OBJECT:
				case ParserState.END_OBJECT:
				{
					getField ();
					assert Debug.debug ("FIELD: " + m_field);
					if (m_field.name != null)
					{
						pushState (ParserState.IN_FIELD);
						m_event = Event.KEY_NAME;
						m_value = new CookJsonString (m_field.name);
					}
					else
					{
						return getEventFromType (m_field.type);
					}
					return m_event;
				}
				default:
				{
					throw new IllegalStateException ();
				}
			}
		}
		catch (IOException ex)
		{
			throw new JsonException (ex.getMessage (), ex);
		}
	}

	@Override
	public String getString ()
	{
		if (m_event != Event.VALUE_STRING &&
			m_event != Event.VALUE_NUMBER &&
			m_event != Event.KEY_NAME)
			throw new IllegalStateException ();
		return m_value.toString ();
	}

	@Override
	public boolean isIntegralNumber ()
	{
		if (m_event != Event.VALUE_NUMBER)
			throw new IllegalStateException ();
		return ((JsonNumber)m_value).isIntegral ();
	}

	@Override
	public int getInt ()
	{
		if (m_event != Event.VALUE_NUMBER)
			throw new IllegalStateException ();
		return ((JsonNumber)m_value).intValue ();
	}

	@Override
	public long getLong ()
	{
		if (m_event != Event.VALUE_NUMBER)
			throw new IllegalStateException ();
		return ((JsonNumber)m_value).longValue ();
	}

	@Override
	public BigDecimal getBigDecimal ()
	{
		if (m_event != Event.VALUE_NUMBER)
			throw new IllegalStateException ();
		return ((JsonNumber)m_value).bigDecimalValue ();
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
}
