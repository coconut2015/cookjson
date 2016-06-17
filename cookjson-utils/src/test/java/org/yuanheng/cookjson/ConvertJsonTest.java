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
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import junitx.framework.FileAssert;

/**
 * @author	Heng Yuan
 */
public class ConvertJsonTest
{
	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder ();

	@Test
	public void testJson () throws IOException
	{
		// test Json input / output with pretty option

		File dstFile;
		File srcFile;

		srcFile = new File ("../tests/data/complex1_pretty.json".replace ('/', File.separatorChar));
		dstFile = testFolder.newFile ("test1.json");
		ConvertJson.main (new String[]{ "-f", srcFile.getPath (), "-t", dstFile.getPath (), "-p" });
		FileAssert.assertBinaryEquals (srcFile, dstFile);
	}

	@Test
	public void testBson () throws Exception
	{
		// test Bson input / output

		File dstFile;
		File srcFile;

		srcFile = new File ("../tests/data/complex1.bson".replace ('/', File.separatorChar));
		dstFile = testFolder.newFile ("test2.bson");
		ConvertJson.main (new String[]{ "-f", srcFile.getPath (), "-t", dstFile.getPath () });
		FixBson.main (new String[]{ dstFile.getPath () });
		FileAssert.assertBinaryEquals (srcFile, dstFile);
	}

	@Test
	public void testRootAsArray () throws Exception
	{
		// test Bson input with rootAsArray option

		File dstFile;
		File srcFile;
		File dstFile2;

		srcFile = new File ("../tests/data/data1.bson".replace ('/', File.separatorChar));
		dstFile = testFolder.newFile ("test3_1.json");
		ConvertJson.main (new String[]{ "-f", srcFile.getPath (), "-t", dstFile.getPath (), "-a" });

		srcFile = new File ("../tests/data/data3.json".replace ('/', File.separatorChar));
		dstFile2 = testFolder.newFile ("test3_2.json");
		ConvertJson.main (new String[]{ "-f", srcFile.getPath (), "-t", dstFile2.getPath () });
		FileAssert.assertBinaryEquals (dstFile, dstFile2);
	}
}
