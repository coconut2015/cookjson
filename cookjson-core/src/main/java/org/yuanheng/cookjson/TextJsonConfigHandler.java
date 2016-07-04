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

import java.io.*;
import java.nio.charset.Charset;
import java.util.Map;

import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParsingException;

/**
 * @author	Heng Yuan
 */
class TextJsonConfigHandler implements ConfigHandler
{
	private final static ConfigHandler s_instance = new TextJsonConfigHandler ();

	public static CookJsonParser getJsonParser (InputStream is)
	{
		PushbackInputStream pis = new PushbackInputStream (is, 3);
		Charset charset;
		try
		{
			charset = BOM.guessCharset (pis);
		}
		catch (IOException ex)
		{
			JsonLocationImpl location = new JsonLocationImpl ();
			location.m_streamOffset = 0;
			throw new JsonParsingException (ex.getMessage (), ex, location);
		}
		if (charset == BOM.utf8)
			return new UTF8TextJsonParser (pis);
		return new TextJsonParser (new InputStreamReader (pis, charset));
	}

	public static CookJsonParser getJsonParser (InputStream is, Charset charset)
	{
		if (BOM.utf8.equals (charset))
			return new UTF8TextJsonParser (is);
		else
			return new TextJsonParser (new InputStreamReader (is, charset));
	}

	public static ConfigHandler getInstance ()
	{
		return s_instance;
	}

	private TextJsonConfigHandler ()
	{
	}

	private void configure (Map<String, ?> config, CookJsonParser p)
	{
		boolean allowComments = false;
		Object obj = config.get (CookJsonProvider.COMMENT);
		if (obj != null)
			allowComments = "true".equals (obj.toString ());
		((CommentJsonParser)p).setAllowComments (allowComments);
	}

	@Override
	public CookJsonParser createParser (Map<String, ?> config, Reader reader)
	{
		CookJsonParser p = new TextJsonParser (reader);
		configure (config, p);
		return p;
	}

	@Override
	public CookJsonParser createParser (Map<String, ?> config, InputStream is)
	{
		CookJsonParser p = getJsonParser (is);
		configure (config, p);
		return p;
	}

	@Override
	public CookJsonParser createParser (Map<String, ?> config, InputStream is, Charset charset)
	{
		CookJsonParser p = getJsonParser (is, charset);
		configure (config, p);
		return p;
	}

	@Override
	public JsonGenerator createGenerator (Map<String, ?> config, Writer writer)
	{
		TextJsonGenerator g;

		boolean pretty = false;
		Object obj = config.get (JsonGenerator.PRETTY_PRINTING);
		if (obj != null)
			pretty = "true".equals (obj.toString ());
		if (pretty)
			g = new PrettyTextJsonGenerator (writer);
		else
			g = new TextJsonGenerator (writer);

		int binaryFormat = BinaryFormat.BINARY_FORMAT_BASE64;
		obj = config.get (CookJsonProvider.BINARY_FORMAT);
		if (obj != null)
		{
			if (CookJsonProvider.BINARY_FORMAT_HEX.equals (obj.toString ()))
			{
				binaryFormat = BinaryFormat.BINARY_FORMAT_HEX;
			}
		}
		g.setBinaryFormat (binaryFormat);

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
