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
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import javax.json.spi.JsonProvider;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.glassfish.json.JsonProviderImpl;
import org.openjdk.jmh.annotations.*;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonToken;

/**
 * @author	Heng Yuan
 */
@OutputTimeUnit (TimeUnit.MILLISECONDS)
@State (value = Scope.Benchmark)
@Fork (value = 1)
@Warmup (iterations = 5)
@BenchmarkMode (Mode.AverageTime)
@Measurement(iterations = 20)
public class JsonParserReaderBenchmark
{
	private char[] m_chars;

	private Reader getReader () throws IOException
	{
		return new CharArrayReader (m_chars);
	}

	@Setup
	public void setup () throws IOException
	{
		String jsonFile = "../tests/data/large.json";
		// String jsonFile = "../tests/data/string.json";
		// String jsonFile = "../tests/data/number.json";
		File file = new File (jsonFile.replace ('/', File.separatorChar));
		char[] buf = new char[4096];
		StringBuilder builder = new StringBuilder ();
		Reader in = new FileReader (file);
		int len;
		while ((len = in.read (buf)) > 0)
		{
			builder.append (buf, 0, len);
		}
		in.close ();
		m_chars = builder.toString ().toCharArray ();
	}

	private void perfTest (JsonParser p)
	{
		try
		{
			for (;;)
			{
				Event e = p.next ();
				switch (e)
				{
					case KEY_NAME:
					case VALUE_NUMBER:
					case VALUE_STRING:
						p.getString ();
						break;
					default:
						break;
					
				}
			}
		}
		catch (NoSuchElementException ex)
		{
		}
	}

	public void perfTest2 (com.fasterxml.jackson.core.JsonParser p) throws IOException
	{
		JsonToken e;
		while ((e = p.nextToken ()) != null)
		{
			switch (e)
			{
				case FIELD_NAME:
				case VALUE_STRING:
				case VALUE_NUMBER_INT:
				case VALUE_NUMBER_FLOAT:
					p.getText ();
					break;
				default:
					break;
				
			}
		}
	}

	@Benchmark
	public void testGlassFish () throws IOException
	{
		JsonProvider provider = new JsonProviderImpl ();
		JsonParser p = provider.createParser (getReader ());
		perfTest (p);
		p.close ();
	}

	@Benchmark
	public void testCookJson () throws IOException
	{
		JsonProvider provider = new CookJsonProvider ();
		JsonParser p = provider.createParser (getReader ());
		perfTest (p);
		p.close ();
	}

	@Benchmark
	public void testAJackson () throws IOException
	{
		JsonFactory jsonFactory = new JsonFactory ();
		com.fasterxml.jackson.core.JsonParser p = jsonFactory.createParser (getReader ());
		perfTest2 (p);
		p.close ();
	}

	@Benchmark
	public void testAJacksonNoCanonical () throws IOException
	{
		JsonFactory jsonFactory = new JsonFactory ();
		jsonFactory.disable (JsonFactory.Feature.CANONICALIZE_FIELD_NAMES);
		com.fasterxml.jackson.core.JsonParser p = jsonFactory.createParser (getReader ());
		perfTest2 (p);
		p.close ();
	}
}
