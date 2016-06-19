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
import java.io.StringWriter;
import java.util.HashMap;

import javax.json.*;
import javax.json.spi.JsonProvider;

import org.glassfish.json.JsonProviderImpl;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author	Heng Yuan
 */
public class JsonReaderWriterTest
{
	private String getString (File file, JsonProvider provider, int readCase) throws IOException
	{
		// create reader
		HashMap<String, Object> readConfig = new HashMap<String, Object> ();
		JsonReaderFactory rf = provider.createReaderFactory (readConfig);
		JsonReader reader = rf.createReader (new FileInputStream (file));
		JsonStructure obj;
		switch (readCase)
		{
			case 1:
				obj = reader.readArray ();
				break;
			case 2:
				obj = reader.readObject ();
				break;
			default:	// 0
				obj = reader.read ();
		}
		reader.close ();

		// write it out
		HashMap<String, Object> writeConfig = new HashMap<String, Object> ();
		JsonWriterFactory wf = provider.createWriterFactory (writeConfig);
		StringWriter sw = new StringWriter ();
		JsonWriter writer = wf.createWriter (sw);
		writer.write (obj);
		writer.close ();

		return sw.toString ();
	}

	private void testFile (String fileName, int readCase) throws IOException
	{
		File file = new File (fileName.replace ('/', File.separatorChar));

		String str1 = "";
		try
		{
			str1 = getString (file, new CookJsonProvider (), readCase);
		}
		catch (Exception ex)
		{
		}
		String str2 = "";
		try
		{
			str2 = getString (file, new JsonProviderImpl (), readCase);
		}
		catch (Exception ex)
		{
		}

		// Because JsonObject's name ordering is somewhat random,
		// we cannot do string comparison.  But we can compare the
		// string length which should be equal.
//		System.out.println (str1);
//		System.out.println (str2);
		Assert.assertEquals (str1.length (), str2.length ());
	}

	@Test
	public void test () throws IOException
	{
		for (int i = 0; i < 3; ++i)
			testFile ("../tests/data/complex1.json", i);
		for (int i = 0; i < 3; ++i)
			testFile ("../tests/data/string2.json", i);
		for (int i = 0; i < 3; ++i)
			testFile ("../tests/data/types.json", i);
	}
}
