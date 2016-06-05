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
	private String getString (File file, JsonProvider provider) throws IOException
	{
		// create reader
		HashMap<String, Object> readConfig = new HashMap<String, Object> ();
		JsonReaderFactory rf = provider.createReaderFactory (readConfig);
		JsonReader reader = rf.createReader (new FileInputStream (file));
		JsonStructure obj = reader.read ();
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

	@Test
	public void test () throws IOException
	{
		String f = "../tests/data/complex1.json";
		File file = new File (f.replace ('/', File.separatorChar));

		String str1 = getString (file, new CookJsonProvider ());
		String str2 = getString (file, new JsonProviderImpl ());

		// Because JsonObject's name ordering is somewhat random,
		// we cannot do string comparison.  But we can compare the
		// string length which should be equal.
		Assert.assertEquals (str1.length (), str2.length ());
	}
}
