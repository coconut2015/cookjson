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
import java.util.NoSuchElementException;

import javax.json.spi.JsonProvider;
import javax.json.stream.JsonParser;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonFactory;

/**
 * This test compares the performance of TextJsonParser against
 * glassfish JsonParser and Jackson's JsonParser.
 *
 * @author	Heng Yuan
 */
public class TextJsonParserPerformanceTest
{
	public final static int ITERATIONS = 100;

	private Reader getReader (File f) throws IOException
	{
		return new InputStreamReader (new FileInputStream (f), "utf-8");
	}

	private long cookJsonTest (File file) throws IOException
	{
		long start = System.currentTimeMillis ();
		for (int i = 0; i < ITERATIONS; ++i)
		{
			JsonParser p = new TextJsonParser (getReader (file));
			perfTest (p);
			p.close ();
		}
		long end = System.currentTimeMillis ();
		return end - start;
	}

	private long glassFishTest (File file) throws IOException
	{
		JsonProvider provider = JsonProvider.provider ();
		long start = System.currentTimeMillis ();
		for (int i = 0; i < ITERATIONS; ++i)
		{
			JsonParser p = provider.createParser (getReader (file));
			perfTest (p);
			p.close ();
		}
		long end = System.currentTimeMillis ();
		return end - start;
	}

	private long jacksonTest (File file) throws IOException
	{
		JsonFactory jsonFactory = new JsonFactory();
		long start = System.currentTimeMillis ();
		for (int i = 0; i < ITERATIONS; ++i)
		{
			com.fasterxml.jackson.core.JsonParser p = jsonFactory.createParser (getReader (file));
			perfTest2 (p);
			p.close ();
		}
		long end = System.currentTimeMillis ();
		return end - start;
	}

	public void perfTest (JsonParser p)
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

	@SuppressWarnings ("unused")
	@Test
	public void test () throws IOException
	{
		if (ITERATIONS <= 0)
		{
			if (true)
				return;
		}

		String jsonFile = "../tests/data/large.json";
//		String jsonFile = "../tests/data/string.json";
//		String jsonFile = "../tests/data/number2.json";
		jsonFile.replace ('/', File.separatorChar);

		JsonParser p;
		File file = new File (jsonFile);

		// prime the file read
		glassFishTest (file);
		cookJsonTest (file);

		long glassFishTime = glassFishTest (file);

		long cookJsonTime = cookJsonTest (file);

		long jacksonTime = jacksonTest (file);

		System.out.println ("== JsonParser Performance Test ==");
		System.out.println ("cookjson: " + cookJsonTime);
		System.out.println ("glassfish: " + glassFishTime);
		System.out.println ("jackson: " + jacksonTime);
		System.out.printf ("Percentage: %2.2f\n", ((double)cookJsonTime / glassFishTime));
		System.out.flush ();
	}
}
