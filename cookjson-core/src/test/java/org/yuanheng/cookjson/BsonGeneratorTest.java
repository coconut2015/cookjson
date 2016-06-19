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

import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import junitx.framework.FileAssert;

/**
 * @author	Heng Yuan
 */
public class BsonGeneratorTest
{
	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder ();

	private void testFile (String fileName1, String fileName2, boolean useDouble) throws IOException
	{
		// data1.bson has 0 in Document / Array length
		File file1 = new File (fileName1.replace ('/', File.separatorChar));
		File file2 = new File (fileName2.replace ('/', File.separatorChar));

		File testFile = testFolder.newFile ();
		TextJsonParser p = new TextJsonParser (new FileInputStream (file1));
		BsonGenerator g = new BsonGenerator (new FileOutputStream (testFile));
		g.setUseDouble (useDouble);
		Utils.convert (p, g);
		p.close ();
		g.close ();
		BsonFixLength.fix (testFile);

		FileAssert.assertBinaryEquals (file2, testFile);
	}

	@Test
	public void testConvert () throws IOException
	{
		testFile ("../tests/data/data3.json", "../tests/data/data1.bson", false);
		testFile ("../tests/data/types.json", "../tests/data/types.bson", true);
		testFile ("../tests/data/types.json", "../tests/data/types2.bson", false);
	}

	private void testBsonFile (String fileName, boolean useDouble) throws IOException
	{
		// data1.bson has 0 in Document / Array length
		File file = new File (fileName.replace ('/', File.separatorChar));

		File testFile = testFolder.newFile ();
		BsonParser p = new BsonParser (new FileInputStream (file));
		BsonGenerator g = new BsonGenerator (new FileOutputStream (testFile));
		g.setUseDouble (useDouble);
		Utils.convert (p, g);
		p.close ();
		g.close ();
		BsonFixLength.fix (testFile);

		FileAssert.assertBinaryEquals (file, testFile);
	}

	@Test
	public void testSelf () throws IOException
	{
		testBsonFile ("../tests/data/binary.bson", false);
	}

	@Test
	public void testComplex () throws IOException
	{
		File file1 = new File ("../tests/data/complex1.json".replace ('/', File.separatorChar));
		File file2 = new File ("../tests/data/complex1.bson".replace ('/', File.separatorChar));

		// first convert from Json to Bson
		File bsonFile = testFolder.newFile ();
		CookJsonParser p = new TextJsonParser (new FileInputStream (file1));
		JsonGenerator g = new BsonGenerator (new FileOutputStream (bsonFile));
		Utils.convert (p, g);
		p.close ();
		g.close ();

		// convert from expected BSON file to JSON
		StringWriter sw1 = new StringWriter ();
		p = new BsonParser (new FileInputStream (file2));
		g = new TextJsonGenerator (sw1);
		Utils.convert (p, g);
		p.close ();
		g.close ();

		// then convert from generated BSON file to JSON
		StringWriter sw2 = new StringWriter ();
		p = new BsonParser (new FileInputStream (bsonFile));
		g = new TextJsonGenerator (sw2);
		Utils.convert (p, g);
		p.close ();
		g.close ();

		Assert.assertEquals (sw1.toString (), sw2.toString ());
	}

	@Test
	public void testJsonValue () throws IOException
	{
		File file1 = new File ("../tests/data/complex1.json".replace ('/', File.separatorChar));

		// first convert from Json to Bson using stream API
		File bsonFile = testFolder.newFile ();
		CookJsonParser p = new TextJsonParser (new FileInputStream (file1));
		JsonGenerator g = new BsonGenerator (new FileOutputStream (bsonFile));
		Utils.convert (p, g);
		p.close ();
		g.close ();

		// convert from Json to Bson using tree api
		File bsonFile2 = testFolder.newFile ();
		p = new TextJsonParser (new FileInputStream (file1));
		p.next ();
		JsonValue value = p.getValue ();
		p.close ();
		g = new BsonGenerator (new FileOutputStream (bsonFile2));
		g.write (value);
		g.close ();

		// due to object ordering, we can only compare the length.
		Assert.assertEquals (bsonFile.length (), bsonFile2.length ());
	}
}
