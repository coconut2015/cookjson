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
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
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
public class CookJsonProvider extends JsonProvider
{
	/** JSON format handling */
	public final static String FORMAT = "format";
	/** Specifies JSON format */
	public final static String FORMAT_JSON = "json";
	/** Specifies BSON format */
	public final static String FORMAT_BSON = "bson";

	// ---- JSON options
	/** If the value is true, allows line/block comments in the file. */
	public final static String COMMENT = "comment";

	// ---- BSON options
	/** If the value is true, stores BigDecimal / BigInteger as double. */
	public final static String USE_DOUBLE = "useDouble";
	/** If the value is true, root Document is treated as Array. */
	public final static String ROOT_AS_ARRAY = "rootAsArray";

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
		return new JsonParserFactoryImpl (config, getHandler (config));
	}

	@Override
	public JsonGenerator createGenerator (Writer writer)
	{
		return new TextJsonGenerator (writer);
	}

	@Override
	public JsonGenerator createGenerator (OutputStream os)
	{
		return new TextJsonGenerator (os);
	}

	@Override
	public JsonGeneratorFactory createGeneratorFactory (Map<String, ?> config)
	{
		return new JsonGeneratorFactoryImpl (config, getHandler (config));
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
		return new JsonWriterFactoryImpl (config, getHandler (config));
	}

	@Override
	public JsonReaderFactory createReaderFactory (Map<String, ?> config)
	{
		return new JsonReaderFactoryImpl (config, getHandler (config));
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

	private ConfigHandler getHandler (Map<String, ?> config)
	{
		boolean bson = FORMAT_BSON.equals (config.get (FORMAT));
		if (bson)
			return BsonConfigHandler.getInstance ();
		else
			return TextJsonConfigHandler.getInstance ();
	}
}
