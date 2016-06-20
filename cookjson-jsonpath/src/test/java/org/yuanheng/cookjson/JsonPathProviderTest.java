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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.spi.JsonProvider;

import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.junit.Test;

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

		JsonProvider p = new CookJsonProvider ();
		HashMap<String, Object> readConfig = new HashMap<String, Object> ();
		JsonReaderFactory rf = p.createReaderFactory (readConfig);
		JsonReader reader = rf.createReader (new FileInputStream (file));
		JsonStructure obj = reader.read ();
		reader.close ();

		JsonValue value = path.read (obj, pathConfig);

		Assert.assertEquals ("[1,3,5,7]", provider.toJson (value));
	}

	@Test
	public void testParseString () throws IOException
	{
		BasicConfigurator.configure ();
		String f = "../tests/data/complex1.json";
		File file = new File (f.replace ('/', File.separatorChar));
		String str = Utils.getString (file);

		JsonPathProvider provider = new JsonPathProvider ();

		Configuration pathConfig = Configuration.defaultConfiguration ().jsonProvider (provider);
		JsonPath path = JsonPath.compile ("$.strange");
		JsonValue value = path.read (str, pathConfig);

		// we cannot directly compare the output since the attribute ordering
		// can vary.
		Assert.assertEquals ("{\"id\":5555,\"price\":[1,2,3],\"customer\":\"john...\"}".length (), provider.toJson (value).length ());
	}

	@Test
	public void testParseFile () throws IOException
	{
		BasicConfigurator.configure ();
		String f = "../tests/data/data3.json";
		File file = new File (f.replace ('/', File.separatorChar));

		JsonPathProvider provider = new JsonPathProvider ();

		Configuration pathConfig = Configuration.defaultConfiguration ().jsonProvider (provider);
		JsonPath path = JsonPath.compile ("$..A");
		JsonValue value = path.read (file, pathConfig);

		Assert.assertEquals ("[1,3,5,7]", provider.toJson (value));
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

		JsonProvider p = new CookJsonProvider ();
		HashMap<String, Object> readConfig = new HashMap<String, Object> ();
		readConfig.put (CookJsonProvider.FORMAT, CookJsonProvider.FORMAT_BSON);
		readConfig.put (CookJsonProvider.ROOT_AS_ARRAY, Boolean.TRUE);
		JsonReaderFactory rf = p.createReaderFactory (readConfig);
		JsonReader reader = rf.createReader (new FileInputStream (file));
		JsonStructure obj = reader.read ();
		reader.close ();

		JsonValue value = path.read (obj, pathConfig);

		Assert.assertEquals ("[1,3,5,7]", provider.toJson (value));
	}
}
