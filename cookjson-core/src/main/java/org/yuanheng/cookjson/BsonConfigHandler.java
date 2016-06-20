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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Map;

import javax.json.JsonException;
import javax.json.stream.JsonGenerator;

/**
 * @author	Heng Yuan
 */
class BsonConfigHandler implements ConfigHandler
{
	private final static ConfigHandler s_instance = new BsonConfigHandler ();

	public static ConfigHandler getInstance ()
	{
		return s_instance;
	}

	private BsonConfigHandler ()
	{
	}

	@Override
	public CookJsonParser createParser (Map<String, ?> config, Reader reader)
	{
		throw new JsonException ("Cannot create a BSON parser from a Reader.");
	}

	@Override
	public CookJsonParser createParser (Map<String, ?> config, InputStream is)
	{
		BsonParser p = new BsonParser (is);

		boolean rootAsArray = false;
		Object obj = config.get (CookJsonProvider.ROOT_AS_ARRAY);
		if (obj != null)
			rootAsArray = "true".equals (obj.toString ());
		p.setRootAsArray (rootAsArray);

		int binaryFormat = BinaryFormat.BINARY_FORMAT_BASE64;
		obj = config.get (CookJsonProvider.BINARY_FORMAT);
		if (obj != null)
		{
			if (CookJsonProvider.BINARY_FORMAT_HEX.equals (obj.toString ()))
			{
				binaryFormat = BinaryFormat.BINARY_FORMAT_HEX;
			}
		}
		p.setBinaryFormat (binaryFormat);
		return p;
	}

	@Override
	public CookJsonParser createParser (Map<String, ?> config, InputStream is, Charset charset)
	{
		return createParser (config, is);
	}

	@Override
	public JsonGenerator createGenerator (Map<String, ?> config, Writer writer)
	{
		throw new JsonException ("Cannot create a BSON generator from a Writer.");
	}

	@Override
	public JsonGenerator createGenerator (Map<String, ?> config, OutputStream os)
	{
		BsonGenerator g = new BsonGenerator (os);

		boolean writeDouble = false;
		Object obj = config.get (CookJsonProvider.USE_DOUBLE);
		if (obj != null)
			writeDouble = "true".equals (obj.toString ());
		g.setUseDouble (writeDouble);

		return g;
	}

	@Override
	public JsonGenerator createGenerator (Map<String, ?> config, OutputStream os, Charset charset)
	{
		return createGenerator (config, os);
	}
}
