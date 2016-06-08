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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.NoSuchElementException;

import javax.json.spi.JsonProvider;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;

import org.glassfish.json.JsonProviderImpl;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * This test compares the performance of TextJsonGenerator against
 * glassfish JsonGenerator.
 *
 * @author	Heng Yuan
 */
public class TextJsonGeneratorPerformanceTest
{
	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder ();

	public final static int ITERATIONS = 100;

	private long cookJsonTest (File inFile, File outFile) throws IOException
	{
		JsonProvider glassFishProvider = new JsonProviderImpl ();
		JsonProvider provider = new CookJsonProvider ();
		long start = System.currentTimeMillis ();
		for (int i = 0; i < ITERATIONS; ++i)
		{
			JsonParser p = glassFishProvider.createParser (new FileInputStream (inFile));
			JsonGenerator g = provider.createGenerator (new FileOutputStream (outFile));
			Utils.convert (p, g);
			p.close ();
			g.close ();
		}
		long end = System.currentTimeMillis ();
		return end - start;
	}

	private long glassFishTest (File inFile, File outFile) throws IOException
	{
		JsonProvider glassFishProvider = new JsonProviderImpl ();
		long start = System.currentTimeMillis ();
		for (int i = 0; i < ITERATIONS; ++i)
		{
			JsonParser p = glassFishProvider.createParser (new FileInputStream (inFile));
			JsonGenerator g = glassFishProvider.createGenerator (new FileOutputStream (outFile));
			Utils.convert (p, g);
			p.close ();
			g.close ();
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

//		String jsonFile = "../tests/data/large.json";
		String jsonFile = "../tests/data/string.json";
//		String jsonFile = "../tests/data/number2.json";
		jsonFile.replace ('/', File.separatorChar);

		JsonParser p;
		File inFile = new File (jsonFile);

		File outFile = testFolder.newFile ("dummy.json");

		// prime the file read
		glassFishTest (inFile, outFile);
		cookJsonTest (inFile, outFile);

		long glassFishTime = glassFishTest (inFile, outFile);
		long cookJsonTime = cookJsonTest (inFile, outFile);

		System.out.println ("== JsonGenerator Performance Test ==");
		System.out.println ("cookjson: " + cookJsonTime);
		System.out.println ("glassfish: " + glassFishTime);
		System.out.printf ("Percentage: %2.2f\n", ((double)cookJsonTime / glassFishTime));
		System.out.flush ();
	}
}
