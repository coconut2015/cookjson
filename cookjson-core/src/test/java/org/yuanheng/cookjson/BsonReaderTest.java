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
import java.io.StringWriter;
import java.util.HashMap;

import javax.json.*;
import javax.json.spi.JsonProvider;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author	Heng Yuan
 */
public class BsonReaderTest
{
	private JsonStructure readModel (File file, HashMap<String, Object> config) throws IOException
	{
		BsonParser p = new BsonParser (new FileInputStream (file));
		p.setRootAsArray (true);
		JsonReader reader = new JsonReaderImpl (p);
		JsonStructure obj = reader.read ();
		reader.close ();
		return obj;
	}

	private JsonStructure readModel2 (File file, HashMap<String, Object> config) throws IOException
	{
		JsonProvider provider = new CookJsonProvider ();
		JsonReaderFactory rf = provider.createReaderFactory (config);
		JsonReader reader = rf.createReader (new FileInputStream (file));
		JsonStructure obj = reader.read ();
		reader.close ();
		return obj;
	}

	private String writeModel (JsonStructure struct) throws IOException
	{
		JsonProvider provider = new CookJsonProvider ();
		HashMap<String, Object> writeConfig = new HashMap<String, Object> ();
		JsonWriterFactory wf = provider.createWriterFactory (writeConfig);
		StringWriter sw = new StringWriter ();
		JsonWriter writer = wf.createWriter (sw);
		writer.write (struct);
		writer.close ();
		return sw.toString ();
	}

	@Test
	public void test () throws IOException
	{
		String f1 = "../tests/data/data1.bson";
		String f2 = "../tests/data/data3.json";
		File file1 = new File (f1.replace ('/', File.separatorChar));
		File file2 = new File (f2.replace ('/', File.separatorChar));

		HashMap<String, Object> bsonConfig = new HashMap<String, Object> ();
		bsonConfig.put (CookJsonProvider.FORMAT, CookJsonProvider.FORMAT_BSON);
		bsonConfig.put (CookJsonProvider.ROOT_AS_ARRAY, Boolean.TRUE);

		JsonStructure value = readModel (file1, bsonConfig);
		String str1 = writeModel (value);

		HashMap<String, Object> textConfig = new HashMap<String, Object> ();
		value = readModel2 (file2, textConfig);
		String str2 = writeModel (value);

		// Because JsonObject's name ordering is somewhat random,
		// we cannot do string comparison.  But we can compare the
		// string length which should be equal.
//		Assert.assertEquals (str1.length (), str2.length ());
		Assert.assertEquals (str1, str2);
	}
}
