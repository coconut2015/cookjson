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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import javax.json.*;
import javax.json.stream.JsonLocation;

/**
 * @author	Heng Yuan
 */
public class JsonStructureParser implements CookJsonParser
{
	private JsonStructure m_s;

	private ArrayList<Object> m_queue = new ArrayList<Object> ();
	private Event m_event;

	private String m_name;
	private JsonValue m_value;

	private static class ArrayQueue
	{
		JsonArray array;
		int index;
	}

	private static class ObjectQueue
	{
		JsonObject obj;
		LinkedList<String> names;
	}

	public JsonStructureParser (JsonStructure s)
	{
		m_s = s;
		push (s);
	}

	private Object top ()
	{
		return m_queue.get (m_queue.size () - 1);
	}

	private void pop ()
	{
		m_queue.remove (m_queue.size () - 1);
	}

	private void push (JsonStructure value)
	{
		if (value instanceof JsonArray)
		{
			// for array, we just need to save the index position
			ArrayQueue q = new ArrayQueue ();
			q.array = (JsonArray) value;
			q.index = 0;
			m_queue.add (q);
		}
		else if (value instanceof JsonObject)
		{
			// for object, we need to store all the names in an array
			JsonObject o = (JsonObject) value;
			LinkedList<String> names = new LinkedList<String> ();
			names.addAll (o.keySet ());
			ObjectQueue q = new ObjectQueue ();
			q.obj = o;
			q.names = names;
			m_queue.add (q);
		}
	}

	@Override
	public boolean hasNext ()
	{
		return m_queue != null && !m_queue.isEmpty ();
	}

	@Override
	public Event next ()
	{
		if (m_queue == null || m_queue.size () == 0)
			throw new NoSuchElementException ();

		Event e = m_event;
		if (e == null)
		{
			m_value = m_s;
			m_event = getEvent (m_s);
			return m_event;
		}
		else if (e == Event.KEY_NAME)
		{
			// we are in an object
			ObjectQueue q = (ObjectQueue) top ();
			// we just handled the name, now get the value.
			JsonValue v = q.obj.get (m_name);
			m_name = null;
			m_value = v;
			if (v instanceof JsonStructure)
				push ((JsonStructure) v);
			m_event = getEvent (v);
		}
		else
		{
			Object o = top ();
			if (o instanceof ObjectQueue)
			{
				ObjectQueue q = (ObjectQueue) o;
				if (q.names.isEmpty ())
				{
					// no more objects
					m_value = null;
					pop ();
					m_event = Event.END_OBJECT;
				}
				else
				{
					m_name = q.names.removeFirst ();
					m_event = Event.KEY_NAME;
				}
			}
			else
			{
				ArrayQueue q = (ArrayQueue) o;
				JsonArray a = q.array;
				if (q.index < a.size ())
				{
					JsonValue v = a.get (q.index++);
					m_name = null;
					m_value = v;
					if (v instanceof JsonStructure)
						push ((JsonStructure) v);
					m_event = getEvent (v);
				}
				else
				{
					// no more values
					m_value = null;
					pop ();
					m_event = Event.END_ARRAY;
				}
			}
		}
		return m_event;
	}

	private Event getEvent (JsonValue v)
	{
		switch (v.getValueType ())
		{
			case ARRAY:
				return Event.START_ARRAY;
			case FALSE:
				return Event.VALUE_FALSE;
			case NULL:
				return Event.VALUE_NULL;
			case NUMBER:
				return Event.VALUE_NUMBER;
			case OBJECT:
				return Event.START_OBJECT;
			case STRING:
				return Event.VALUE_STRING;
			case TRUE:
				return Event.VALUE_TRUE;
		}
		throw new IllegalStateException ();
	}

	private void stateError ()
	{
		throw new IllegalStateException ();
	}

	@Override
	public String getString ()
	{
		switch (m_event)
		{
			case VALUE_STRING:
				return ((JsonString)m_value).getString ();
			case KEY_NAME:
				return m_name;
			case VALUE_NUMBER:
				return ((JsonNumber)m_value).toString ();
			default:
				stateError ();
				return null;	// to make compiler happy.
		}
	}

	@Override
	public boolean isIntegralNumber ()
	{
		if (m_event != Event.VALUE_NUMBER)
			stateError ();
		return ((JsonNumber)m_value).isIntegral ();
	}

	@Override
	public int getInt ()
	{
		if (m_event != Event.VALUE_NUMBER)
			stateError ();
		return ((JsonNumber)m_value).intValue ();
	}

	@Override
	public long getLong ()
	{
		if (m_event != Event.VALUE_NUMBER)
			stateError ();
		return ((JsonNumber)m_value).longValue ();
	}

	@Override
	public BigDecimal getBigDecimal ()
	{
		if (m_event != Event.VALUE_NUMBER)
			stateError ();
		return ((JsonNumber)m_value).bigDecimalValue ();
	}

	@Override
	public JsonLocation getLocation ()
	{
		return JsonLocationImpl.Unknown;
	}

	@Override
	public void close ()
	{
		m_s = null;
		m_queue = null;
	}

	@Override
	public Event getEvent ()
	{
		return m_event;
	}

	@Override
	public JsonValue getValue ()
	{
		return m_value;
	}
}
