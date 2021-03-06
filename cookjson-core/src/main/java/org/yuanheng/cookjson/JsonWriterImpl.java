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

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonStructure;
import javax.json.JsonWriter;
import javax.json.stream.JsonGenerator;

/**
 * @author	Heng Yuan
 */
class JsonWriterImpl implements JsonWriter
{
	private final JsonGenerator m_g;

	public JsonWriterImpl (JsonGenerator g)
	{
		m_g = g;
	}

	@Override
	public void writeArray (JsonArray array)
	{
		m_g.write (array);
	}

	@Override
	public void writeObject (JsonObject object)
	{
		m_g.write (object);
	}

	@Override
	public void write (JsonStructure value)
	{
		m_g.write (value);
	}

	@Override
	public void close ()
	{
		m_g.close ();
	}

}
