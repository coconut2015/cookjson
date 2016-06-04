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

import javax.json.spi.JsonProvider;
import javax.json.stream.JsonParser;

import org.junit.Test;

/**
 * @author	Heng Yuan
 */
public class ReadPerfTest
{
	public final static int ITERATIONS = 100;

	private Reader getReader (File f) throws IOException
	{
		return new InputStreamReader (new FileInputStream (f), "utf-8");
	}

	private JsonParser getGlassFishParser (File f) throws IOException
	{
		return JsonProvider.provider ().createParser (getReader (f));
	}

	private JsonParser getCookJsonParser (File f) throws IOException
	{
		return new TextJsonParser (getReader (f));
	}

	public void perfTest (JsonParser p)
	{
		while (p.hasNext ())
		{
			switch (p.next ())
			{
				case KEY_NAME:
				case VALUE_STRING:
					p.getString ();
					break;
				case VALUE_NUMBER:
					p.getBigDecimal ();
					break;
				default:
					break;
			}
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

		String jsonFile = "src/test/resources/large.json";
//		String jsonFile = "src/test/resources/string.json";
		jsonFile.replace ('/', File.separatorChar);

		JsonParser p;
		File file = new File (jsonFile);

		// prime the file read
		p = getGlassFishParser (file);
		perfTest (p);
		p.close ();

		p = getCookJsonParser (file);
		perfTest (p);
		p.close ();

		long start;
		long end;

		start = System.currentTimeMillis ();
		for (int i = 0; i < ITERATIONS; ++i)
		{
			p = getGlassFishParser (file);
			perfTest (p);
			p.close ();
		}
		end = System.currentTimeMillis ();

		long glassFishTime = end - start;

		start = System.currentTimeMillis ();
		for (int i = 0; i < ITERATIONS; ++i)
		{
			p = getCookJsonParser (file);
			perfTest (p);
			p.close ();
		}
		end = System.currentTimeMillis ();

		long cookJsonTime = end - start;

		System.out.println ("== JsonParser Performance Test ==");
		System.out.println ("cookjson: " + cookJsonTime);
		System.out.println ("glassfish: " + glassFishTime);
	}
}
