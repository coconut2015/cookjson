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
import java.util.HashMap;

import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import junitx.framework.FileAssert;

/**
 * @author	Heng Yuan
 */
public class BinaryTest
{
	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder ();

	@Test
	public void testJson () throws IOException
	{
		File file1 = new File ("../tests/data/binary.bson".replace ('/', File.separatorChar));
		File file2 = new File ("../tests/data/binary.json".replace ('/', File.separatorChar));

		CookJsonProvider provider = new CookJsonProvider ();
		HashMap<String, Object> bsonConfig = new HashMap<String, Object> ();
		bsonConfig.put (CookJsonProvider.FORMAT, CookJsonProvider.FORMAT_BSON);
		bsonConfig.put (CookJsonProvider.ROOT_AS_ARRAY, Boolean.TRUE);

		// first convert from Json to Bson using stream API
		File jsonFile = testFolder.newFile ();
		JsonParser p = provider.createParserFactory (bsonConfig).createParser (new FileInputStream (file1));
		JsonGenerator g = new TextJsonGenerator (new FileOutputStream (jsonFile));
		Utils.convert (p, g);
		p.close ();
		g.close ();

		FileAssert.assertBinaryEquals (file2, jsonFile);
	}

	@Test
	public void testBson () throws IOException
	{
		File file1 = new File ("../tests/data/binary.bson".replace ('/', File.separatorChar));

		CookJsonProvider provider = new CookJsonProvider ();
		HashMap<String, Object> bsonConfig = new HashMap<String, Object> ();
		bsonConfig.put (CookJsonProvider.FORMAT, CookJsonProvider.FORMAT_BSON);
		bsonConfig.put (CookJsonProvider.ROOT_AS_ARRAY, Boolean.TRUE);

		// first convert from Json to Bson using stream API
		File bsonFile = testFolder.newFile ();
		JsonParser p = provider.createParserFactory (bsonConfig).createParser (new FileInputStream (file1));
		JsonGenerator g = provider.createGeneratorFactory (bsonConfig).createGenerator (new FileOutputStream (bsonFile));
		Utils.convert (p, g);
		p.close ();
		g.close ();

		BsonFixLength.fix (bsonFile);
		FileAssert.assertBinaryEquals (file1, bsonFile);
	}
}
