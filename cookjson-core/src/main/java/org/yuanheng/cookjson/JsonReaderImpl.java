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

import javax.json.*;
import javax.json.stream.JsonParser.Event;

/**
 * @author	Heng Yuan
 */
class JsonReaderImpl implements JsonReader
{
	private final CookJsonParser m_p;
	private boolean m_read;

	public JsonReaderImpl (CookJsonParser p)
	{
		m_p = p;
	}

	@Override
	public JsonStructure read ()
	{
		if (m_read)
			throw new IllegalStateException ();
		m_read = true;
		if (!m_p.hasNext ())
			throw new IllegalStateException ();
		Event e = m_p.next ();
		if (e != Event.START_ARRAY &&
			e != Event.START_OBJECT)
			throw new IllegalStateException ();
		return (JsonStructure)Utils.getValue (m_p);
	}

	@Override
	public JsonObject readObject ()
	{
		if (m_read)
			throw new IllegalStateException ();
		m_read = true;
		if (!m_p.hasNext ())
			throw new IllegalStateException ();
		Event e = m_p.next ();
		if (e != Event.START_OBJECT)
			throw new IllegalStateException ();
		return (JsonObject)Utils.getValue (m_p);
	}

	@Override
	public JsonArray readArray ()
	{
		if (m_read)
			throw new IllegalStateException ();
		m_read = true;
		if (!m_p.hasNext ())
			throw new IllegalStateException ();
		Event e = m_p.next ();
		if (e != Event.START_ARRAY &&
			e != Event.START_OBJECT)
			throw new IllegalStateException ();
		return (JsonArray)Utils.getValue (m_p);
	}

	@Override
	public void close ()
	{
		m_p.close ();
	}
}
