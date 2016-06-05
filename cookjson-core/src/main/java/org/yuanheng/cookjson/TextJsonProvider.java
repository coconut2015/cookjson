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
	public final static String COMMENT = "comment";

	@Override
	public JsonParser createParser (Reader reader)
	{
		return new TextJsonParser (reader);
	}

	@Override
	public JsonParser createParser (InputStream is)
	{
		return new TextJsonParser (is);
	}

	@Override
	public JsonParserFactory createParserFactory (Map<String, ?> config)
	{
		return new TextJsonParserFactory (config);
	}

	@Override
	public JsonGenerator createGenerator (Writer writer)
	{
		return new TextJsonGenerator (writer);
	}

	@Override
	public JsonGenerator createGenerator (OutputStream out)
	{
		return new TextJsonGenerator (out);
	}

	@Override
	public JsonGeneratorFactory createGeneratorFactory (Map<String, ?> config)
	{
		return new TextJsonGeneratorFactory (config);
	}

	@Override
	public JsonReader createReader (Reader reader)
	{
		return new JsonReaderImpl (new TextJsonParser (reader));
	}

	@Override
	public JsonReader createReader (InputStream is)
	{
		return new JsonReaderImpl (new TextJsonParser (is));
	}

	@Override
	public JsonWriter createWriter (Writer writer)
	{
		return new JsonWriterImpl (new TextJsonGenerator (writer));
	}

	@Override
	public JsonWriter createWriter (OutputStream os)
	{
		return new JsonWriterImpl (new TextJsonGenerator (os));
	}

	@Override
	public JsonWriterFactory createWriterFactory (Map<String, ?> config)
	{
		return new TextJsonWriterFactory (config);
	}

	@Override
	public JsonReaderFactory createReaderFactory (Map<String, ?> config)
	{
		return new TextJsonReaderFactory (config);
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

	public static CookJsonParser createParser (Map<String, ?> config, Reader reader)
	{
		boolean allowComments = false;
		Object obj = config.get (TextJsonProvider.COMMENT);
		if (obj != null)
			allowComments = "true".equals (obj.toString ());
		TextJsonParser p = new TextJsonParser (reader);
		p.setAllowComments (allowComments);
		return p;
	}

	public static CookJsonParser createParser (Map<String, ?> config, InputStream is)
	{
		boolean allowComments = false;
		Object obj = config.get (TextJsonProvider.COMMENT);
		if (obj != null)
			allowComments = "true".equals (obj.toString ());
		TextJsonParser p = new TextJsonParser (is);
		p.setAllowComments (allowComments);
		return p;
	}

	public static JsonGenerator createGenerator (Map<String, ?> config, Writer writer)
	{
		boolean pretty = false;
		Object obj = config.get (JsonGenerator.PRETTY_PRINTING);
		if (obj != null)
			pretty = "true".equals (obj.toString ());
		TextJsonGenerator g;
		if (pretty)
			g = new PrettyTextJsonGenerator (writer);
		else
			g = new TextJsonGenerator (writer);
		return g;
	}

	public static JsonGenerator createGenerator (Map<String, ?> config, OutputStream os)
	{
		boolean pretty = false;
		Object obj = config.get (JsonGenerator.PRETTY_PRINTING);
		if (obj != null)
			pretty = "true".equals (obj.toString ());
		TextJsonGenerator g;
		if (pretty)
			g = new PrettyTextJsonGenerator (os);
		else
			g = new TextJsonGenerator (os);
		return g;
	}

}
