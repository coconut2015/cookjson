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

import java.io.*;
import java.util.Map;

import javax.json.*;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;

/**
 * @author	Heng Yuan
 */
public class TextJsonProvider extends JsonProvider
{
	public final static String FORMAT = "format";
	/** Use the JSON format.  This is the default. */
	public final static String FORMAT_JSON = "json";
	/** Use the BSON format. */
	public final static String FORMAT_BSON = "bson";

	@Override
	public JsonParser createParser (Reader reader)
	{
		return new TextJsonParser (reader);
	}

	@Override
	public JsonParser createParser (InputStream is)
	{
		return null;
	}

	@Override
	public JsonParserFactory createParserFactory (Map<String, ?> config)
	{
		return new TextJsonParserFactory (config);
	}

	@Override
	public JsonGenerator createGenerator (Writer writer)
	{
		return new FastJsonGenerator (writer);
	}

	@Override
	public JsonGenerator createGenerator (OutputStream out)
	{
		return new FastJsonGenerator (out);
	}

	@Override
	public JsonGeneratorFactory createGeneratorFactory (Map<String, ?> config)
	{
		return new TextJsonGeneratorFactory (config);
	}

	@Override
	public JsonReader createReader (Reader reader)
	{
		return null;
	}

	@Override
	public JsonReader createReader (InputStream is)
	{
		return null;
	}

	@Override
	public JsonWriter createWriter (Writer writer)
	{
		return new JsonWriterImpl (new FastJsonGenerator (writer));
	}

	@Override
	public JsonWriter createWriter (OutputStream os)
	{
		OutputStreamWriter writer = new OutputStreamWriter (os);
		return new JsonWriterImpl (new FastJsonGenerator (writer));
	}

	@Override
	public JsonWriterFactory createWriterFactory (Map<String, ?> config)
	{
		return null;
	}

	@Override
	public JsonReaderFactory createReaderFactory (Map<String, ?> config)
	{
		return null;
	}

	@Override
	public JsonObjectBuilder createObjectBuilder ()
	{
		return new JsonObjectBuilderImpl ();
	}

	@Override
	public JsonArrayBuilder createArrayBuilder ()
	{
		return new JsonArrayBuilderImpl ();
	}

	@Override
	public JsonBuilderFactory createBuilderFactory (Map<String, ?> config)
	{
		return new JsonBuilderFactoryImpl (config);
	}
}
