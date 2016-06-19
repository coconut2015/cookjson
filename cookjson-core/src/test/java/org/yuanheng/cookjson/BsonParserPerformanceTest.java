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
import java.io.IOException;
import java.lang.management.ThreadMXBean;
import java.util.NoSuchElementException;

import javax.json.stream.JsonParser;

import org.junit.Test;

import de.undercouch.bson4jackson.BsonFactory;

/**
 * This test compares the performance of TextJsonParser against
 * glassfish JsonParser and Jackson's JsonParser.
 *
 * @author	Heng Yuan
 */
public class BsonParserPerformanceTest
{
	public final static int ITERATIONS = 100;

	private long cookJsonTest (File file) throws IOException
	{
		ThreadMXBean mxBean = java.lang.management.ManagementFactory.getThreadMXBean();
		long start = mxBean.getCurrentThreadCpuTime ();
		for (int i = 0; i < ITERATIONS; ++i)
		{
			BsonParser p = new BsonParser (new FileInputStream (file));
			perfTest (p);
			p.close ();
		}
		long end = mxBean.getCurrentThreadCpuTime ();
		return end - start;
	}

	private long jacksonTest (File file) throws IOException
	{
		BsonFactory bsonFactory = new BsonFactory();
		ThreadMXBean mxBean = java.lang.management.ManagementFactory.getThreadMXBean();
		long start = mxBean.getCurrentThreadCpuTime ();
		for (int i = 0; i < ITERATIONS; ++i)
		{
			de.undercouch.bson4jackson.BsonParser p = bsonFactory.createParser (new FileInputStream (file));
			perfTest2 (p);
			p.close ();
		}
		long end = mxBean.getCurrentThreadCpuTime ();
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

		String jsonFile = "../tests/data/large.bson";
		jsonFile.replace ('/', File.separatorChar);

		JsonParser p;
		File file = new File (jsonFile);

		// prime the file read
		jacksonTest (file);

		long cookJsonTime = cookJsonTest (file);

		long jacksonTime = jacksonTest (file);

		System.out.println ("== BsonParser Performance Test ==");
		System.out.printf ("cookjson: %1.3fs\n", ((double)cookJsonTime / 1000000000));
		System.out.printf ("jackson: %1.3fs\n", ((double)jacksonTime / 1000000000));
		System.out.printf ("Percentage: %2.2f\n", ((double)cookJsonTime / jacksonTime));
		System.out.flush ();
	}
}
