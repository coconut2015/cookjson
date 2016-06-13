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
import java.nio.charset.Charset;
import java.util.Map;

import javax.json.JsonException;
import javax.json.stream.JsonGenerator;

/**
 * @author	Heng Yuan
 */
class BsonHandler implements CookJsonHandler
{
	private final static CookJsonHandler s_instance = new BsonHandler ();

	public static CookJsonHandler getInstance ()
	{
		return s_instance;
	}

	private BsonHandler ()
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
		boolean rootAsArray = false;
		Object obj = config.get (CookJsonProvider.ROOT_AS_ARRAY);
		if (obj != null)
			rootAsArray = "true".equals (obj.toString ());
		BsonParser p = new BsonParser (is);
		p.setRootAsArray (rootAsArray);
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
