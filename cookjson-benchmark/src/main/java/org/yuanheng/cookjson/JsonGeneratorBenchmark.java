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
import java.util.concurrent.TimeUnit;

import javax.json.spi.JsonProvider;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;

import org.glassfish.json.JsonProviderImpl;
import org.openjdk.jmh.annotations.*;

/**
 * This test compares the performance of TextJsonGenerator against
 * glassfish JsonGenerator.
 *
 * @author	Heng Yuan
 */
@OutputTimeUnit (TimeUnit.MILLISECONDS)
@State (value = Scope.Benchmark)
@Fork (value = 1)
@Warmup (iterations = 5)
@BenchmarkMode (Mode.AverageTime)
@Measurement(iterations = 20)
public class JsonGeneratorBenchmark
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

	@Benchmark
	public void testCookJson () throws IOException
	{
		JsonProvider glassFishProvider = new JsonProviderImpl ();
		JsonProvider provider = new CookJsonProvider ();
		JsonParser p = glassFishProvider.createParser (getReader ());
		JsonGenerator g = provider.createGenerator (new StringWriter ());
		Utils.convert (p, g);
		p.close ();
		g.close ();
	}

	@Benchmark
	public void testGlassFish () throws IOException
	{
		JsonProvider glassFishProvider = new JsonProviderImpl ();
		JsonParser p = glassFishProvider.createParser (getReader ());
		JsonGenerator g = glassFishProvider.createGenerator (new StringWriter ());
		Utils.convert (p, g);
		p.close ();
		g.close ();
	}
}
