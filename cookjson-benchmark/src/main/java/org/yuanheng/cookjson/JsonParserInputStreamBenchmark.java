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

import org.glassfish.json.JsonProviderImpl;
import org.openjdk.jmh.annotations.*;

import com.fasterxml.jackson.core.JsonFactory;

/**
 * @author	Heng Yuan
 */
@OutputTimeUnit (TimeUnit.MILLISECONDS)
@State (value = Scope.Benchmark)
@Fork (value = 1)
@Warmup (iterations = 5)
@BenchmarkMode (Mode.AverageTime)
@Measurement(iterations = 20)
public class JsonParserInputStreamBenchmark
{
	private byte[] m_bytes;

	private InputStream getInputStream () throws IOException
	{
		return new ByteArrayInputStream (m_bytes);
	}

	@Setup
	public void setup () throws IOException
	{
		String jsonFile = "../tests/data/large.json";
		// String jsonFile = "../tests/data/string.json";
		// String jsonFile = "../tests/data/number.json";
		File file = new File (jsonFile.replace ('/', File.separatorChar));
		byte[] buf = new byte[4096];
		ByteArrayOutputStream os = new ByteArrayOutputStream ();
		InputStream is = new FileInputStream (file);
		int len;
		while ((len = is.read (buf)) > 0)
		{
			os.write (buf, 0, len);
		}
		is.close ();
		os.close ();
		m_bytes = os.toByteArray ();
	}

	private void perfTest (JsonParser p)
	{
		try
		{
			for (;;)
			{
				p.next ();
			}
		}
		catch (NoSuchElementException ex)
		{
		}
	}

	public void perfTest2 (com.fasterxml.jackson.core.JsonParser p) throws IOException
	{
		while (p.nextToken () != null)
		{
		}
	}

	@Benchmark
	public void testCookJson () throws IOException
	{
		JsonProvider provider = new CookJsonProvider ();
		JsonParser p = provider.createParser (getInputStream ());
		perfTest (p);
		p.close ();
	}

	@Benchmark
	public void testGlassFish () throws IOException
	{
		JsonProvider provider = new JsonProviderImpl ();
		JsonParser p = provider.createParser (getInputStream ());
		perfTest (p);
		p.close ();
	}

	@Benchmark
	public void testJackson () throws IOException
	{
		JsonFactory jsonFactory = new JsonFactory ();
		com.fasterxml.jackson.core.JsonParser p = jsonFactory.createParser (getInputStream ());
		perfTest2 (p);
		p.close ();
	}

	@Benchmark
	public void testJacksonNoCanonical () throws IOException
	{
		JsonFactory jsonFactory = new JsonFactory ();
		jsonFactory.disable (JsonFactory.Feature.CANONICALIZE_FIELD_NAMES);
		com.fasterxml.jackson.core.JsonParser p = jsonFactory.createParser (getInputStream ());
		perfTest2 (p);
		p.close ();
	}
}
