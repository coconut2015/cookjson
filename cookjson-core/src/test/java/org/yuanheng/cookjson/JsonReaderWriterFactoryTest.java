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
import java.util.HashMap;

import javax.json.*;
import javax.json.spi.JsonProvider;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author	Heng Yuan
 */
public class JsonReaderWriterFactoryTest
{
	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder ();

	@Test
	public void readerWriterTest () throws IOException
	{
		File file = new File ("../tests/data/types.json".replace ('/', File.separatorChar));
		JsonProvider provider = new CookJsonProvider ();

		// create reader
		HashMap<String, Object> readConfig = new HashMap<String, Object> ();
		JsonReaderFactory rf = provider.createReaderFactory (readConfig);
		JsonReader reader = rf.createReader (new InputStreamReader (new FileInputStream (file), BOM.utf8));
		JsonStructure obj = reader.read ();
		reader.close ();

		// write it out
		File dstFile = testFolder.newFile ();
		HashMap<String, Object> writeConfig = new HashMap<String, Object> ();
		JsonWriterFactory wf = provider.createWriterFactory (writeConfig);
		JsonWriter writer = wf.createWriter (new OutputStreamWriter (new FileOutputStream (dstFile), BOM.utf8));
		writer.write (obj);
		writer.close ();

		Assert.assertEquals (file.length (), dstFile.length ());
	}

	@Test
	public void charsetTest () throws IOException
	{
		File file = new File ("../tests/data/types.json".replace ('/', File.separatorChar));
		JsonProvider provider = new CookJsonProvider ();

		// create reader
		HashMap<String, Object> readConfig = new HashMap<String, Object> ();
		JsonReaderFactory rf = provider.createReaderFactory (readConfig);
		JsonReader reader = rf.createReader (new FileInputStream (file), BOM.utf8);
		JsonStructure obj = reader.read ();
		reader.close ();

		// write it out
		File dstFile = testFolder.newFile ();
		HashMap<String, Object> writeConfig = new HashMap<String, Object> ();
		JsonWriterFactory wf = provider.createWriterFactory (writeConfig);
		JsonWriter writer = wf.createWriter (new FileOutputStream (dstFile), BOM.utf8);
		writer.write (obj);
		writer.close ();

		Assert.assertEquals (file.length (), dstFile.length ());
	}

	@Test
	public void streamTest () throws IOException
	{
		File file = new File ("../tests/data/types.json".replace ('/', File.separatorChar));
		JsonProvider provider = new CookJsonProvider ();

		// create reader
		HashMap<String, Object> readConfig = new HashMap<String, Object> ();
		JsonReaderFactory rf = provider.createReaderFactory (readConfig);
		JsonReader reader = rf.createReader (new FileInputStream (file));
		JsonStructure obj = reader.read ();
		reader.close ();

		// write it out
		File dstFile = testFolder.newFile ();
		HashMap<String, Object> writeConfig = new HashMap<String, Object> ();
		JsonWriterFactory wf = provider.createWriterFactory (writeConfig);
		JsonWriter writer = wf.createWriter (new FileOutputStream (dstFile));
		writer.write (obj);
		writer.close ();

		Assert.assertEquals (file.length (), dstFile.length ());
	}
}
