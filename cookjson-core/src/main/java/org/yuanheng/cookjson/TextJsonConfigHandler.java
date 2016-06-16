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
import java.nio.charset.Charset;
import java.util.Map;

import javax.json.JsonException;
import javax.json.stream.JsonGenerator;

/**
 * @author	Heng Yuan
 */
class TextJsonConfigHandler implements ConfigHandler
{
	private final static ConfigHandler s_instance = new TextJsonConfigHandler ();

	public static ConfigHandler getInstance ()
	{
		return s_instance;
	}

	private TextJsonConfigHandler ()
	{
	}

	@Override
	public CookJsonParser createParser (Map<String, ?> config, Reader reader)
	{
		boolean allowComments = false;
		Object obj = config.get (CookJsonProvider.COMMENT);
		if (obj != null)
			allowComments = "true".equals (obj.toString ());
		TextJsonParser p = new TextJsonParser (reader);
		p.setAllowComments (allowComments);
		return p;
	}

	@Override
	public CookJsonParser createParser (Map<String, ?> config, InputStream is)
	{
		PushbackInputStream pis = new PushbackInputStream (is, 3);
		Charset charset;
		try
		{
			charset = BOM.guessCharset (pis);
		}
		catch (IOException ex)
		{
			throw new JsonException (ex.getMessage (), ex);
		}
		return createParser (config, new InputStreamReader (pis, charset));
	}

	@Override
	public CookJsonParser createParser (Map<String, ?> config, InputStream is, Charset charset)
	{
		return createParser (config, new InputStreamReader (is, charset));
	}

	@Override
	public JsonGenerator createGenerator (Map<String, ?> config, Writer writer)
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
		obj = config.get (CookJsonProvider.BINARY_FORMAT);
		if (obj != null)
		{
			if (CookJsonProvider.BINARY_FORMAT_HEX.equals (obj.toString ()))
			{
				g.setBinaryFormat (TextJsonGenerator.BINARY_FORMAT_HEX);
			}
		}
		return g;
	}

	@Override
	public JsonGenerator createGenerator (Map<String, ?> config, OutputStream os)
	{
		return createGenerator (config, new OutputStreamWriter (os, BOM.utf8));
	}

	@Override
	public JsonGenerator createGenerator (Map<String, ?> config, OutputStream os, Charset charset)
	{
		return createGenerator (config, new OutputStreamWriter (os, charset));
	}
}
