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
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import javax.json.JsonException;
import javax.json.JsonValue;

import org.yuanheng.cookjson.value.CookJsonArray;
import org.yuanheng.cookjson.value.CookJsonObject;

import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.spi.json.AbstractJsonProvider;

/**
 * This class is a very simple javax.json based Jayway {@link com.jayway.jsonpath.spi.json.JsonProvider}.
 *
 * @author	Heng Yuan
 */
public class JsonPathProvider extends AbstractJsonProvider
{
	@Override
	public Object createArray ()
	{
		return new CookJsonArray ();
	}

	@Override
	public Object createMap ()
	{
		return new CookJsonObject ();
	}

	@Override
	public Object parse (String jsonString) throws InvalidJsonException
	{
		try
		{
			Reader r = new StringReader (jsonString);
			TextJsonParser p = new TextJsonParser (r);
			p.next ();	// read the very first token to get initiated.
			JsonValue v = p.getValue ();
			p.close ();
			return v;
		}
		catch (JsonException ex)
		{
			throw new InvalidJsonException (ex);
		}
	}

	@Override
	public Object parse (InputStream is, String charset) throws InvalidJsonException
	{
		try
		{
			CookJsonParser p = TextJsonConfigHandler.getJsonParser (is, Charset.forName (charset));
			p.next ();	// read the very first token to get initiated.
			JsonValue v = p.getValue ();
			p.close ();
			return v;
		}
		catch (UnsupportedCharsetException ex)
		{
			throw new InvalidJsonException (ex);
		}
		catch (JsonException ex)
		{
			throw new InvalidJsonException (ex);
		}
	}

	@Override
	public String toJson (Object obj)
	{
		StringWriter writer = new StringWriter ();
		TextJsonGenerator g = new TextJsonGenerator (writer);
		g.write ((JsonValue) obj);
		g.close ();
		return writer.toString ();
	}
}
