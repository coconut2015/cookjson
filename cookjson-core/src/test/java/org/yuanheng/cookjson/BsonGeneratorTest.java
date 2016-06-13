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

import javax.json.stream.JsonParser;

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

	void testFile (String f1, String f2) throws IOException
	{
		File file1 = new File (f1.replace ('/', File.separatorChar));
		File file2 = new File (f2.replace ('/', File.separatorChar));

//		File testFile = testFolder.newFile ();
		File testFile = new File ("..\\tests\\t1.bson");
		JsonParser p = new TextJsonParser (new FileInputStream (file1));
		BsonGenerator g = new BsonGenerator (new FileOutputStream (testFile));
		Utils.convert (p, g);
		p.close ();
		g.close ();

		FileAssert.assertBinaryEquals (file2, testFile);
	}

	@Test
	public void test () throws IOException
	{
		// data1.bson has 0 in Document / Array length
		testFile ("../tests/data/data3.json", "../tests/data/data2.bson");
	}
}
