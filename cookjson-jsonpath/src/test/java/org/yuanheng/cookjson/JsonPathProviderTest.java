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

import org.apache.log4j.BasicConfigurator;
import org.junit.*;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

/**
 * @author	Heng Yuan
 */
public class JsonPathProviderTest
{
	@Test
	public void testJson () throws IOException
	{
		BasicConfigurator.configure ();
		String f = "../tests/data/data3.json";
		File file = new File (f.replace ('/', File.separatorChar));

		JsonPathProvider provider = new JsonPathProvider ();

		Configuration pathConfig = Configuration.defaultConfiguration ().jsonProvider (provider);
		JsonPath path = JsonPath.compile ("$..A");

		JsonProvider p = JsonProvider.provider ();
		HashMap<String, Object> readConfig = new HashMap<String, Object> ();
		JsonReaderFactory rf = p.createReaderFactory (readConfig);
		JsonReader reader = rf.createReader (new FileInputStream (file));
		JsonStructure obj = reader.read ();
		reader.close ();

		JsonValue value = path.read (obj, pathConfig);

		HashMap<String, Object> writeConfig = new HashMap<String, Object> ();
		JsonWriterFactory wf = p.createWriterFactory (writeConfig);
		StringWriter sw = new StringWriter ();
		JsonWriter writer = wf.createWriter (sw);
		writer.write ((JsonStructure) value);
		writer.close ();

		Assert.assertEquals ("[1,3,5,7]", sw.toString ());
	}

	@Test
	public void testBson () throws IOException
	{
		BasicConfigurator.configure ();
		String f = "../tests/data/data1.bson";
		File file = new File (f.replace ('/', File.separatorChar));

		JsonPathProvider provider = new JsonPathProvider ();

		Configuration pathConfig = Configuration.defaultConfiguration ().jsonProvider (provider);
		JsonPath path = JsonPath.compile ("$..A");

		JsonProvider p = JsonProvider.provider ();
		HashMap<String, Object> readConfig = new HashMap<String, Object> ();
		readConfig.put (CookJsonProvider.FORMAT, CookJsonProvider.FORMAT_BSON);
		JsonReaderFactory rf = p.createReaderFactory (readConfig);
		JsonReader reader = rf.createReader (new FileInputStream (file));
		JsonStructure obj = reader.read ();
		reader.close ();

		JsonValue value = path.read (obj, pathConfig);

		HashMap<String, Object> writeConfig = new HashMap<String, Object> ();
		JsonWriterFactory wf = p.createWriterFactory (writeConfig);
		StringWriter sw = new StringWriter ();
		JsonWriter writer = wf.createWriter (sw);
		writer.write ((JsonStructure) value);
		writer.close ();

		Assert.assertEquals ("[1,3,5,7]", sw.toString ());
	}
}
