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
	public void testHelp () throws Exception
	{
		ConvertJson.main (new String[]{ "-h" });
	}

	@Test
	public void testJson () throws IOException
	{
		// test Json input / output with pretty option

		File srcFile;
		File dstFile;

		srcFile = new File ("../tests/data/complex1_pretty.json".replace ('/', File.separatorChar));
		dstFile = testFolder.newFile ("complex1_pretty.json");
		ConvertJson.main (new String[]{ "-f", srcFile.getPath (), "-t", dstFile.getPath (), "-p" });
		FileAssert.assertBinaryEquals (srcFile, dstFile);
	}

	@Test
	public void testBsonComplex () throws Exception
	{
		// test Bson input / output

		File srcFile;
		File dstFile;

		srcFile = new File ("../tests/data/complex1.bson".replace ('/', File.separatorChar));
		dstFile = testFolder.newFile ("complex1.bson");
		ConvertJson.main (new String[]{ "-f", srcFile.getPath (), "-t", dstFile.getPath () });
		FileAssert.assertBinaryEquals (srcFile, dstFile);
	}

	@Test
	public void testBsonNumber () throws Exception
	{
		// test Bson input / output

		File srcFile;
		File expectFile;
		File dstFile;

		srcFile = new File ("../tests/data/number3.json".replace ('/', File.separatorChar));
		expectFile = new File ("../tests/data/number3.bson".replace ('/', File.separatorChar));
		dstFile = testFolder.newFile ("number3.bson");
		ConvertJson.main (new String[]{ "-f", srcFile.getPath (), "-t", dstFile.getPath (), "-d" });
		FileAssert.assertBinaryEquals (expectFile, dstFile);
	}

	@Test
	public void testRootAsArray () throws Exception
	{
		// test Bson input with rootAsArray option

		File srcFile;
		File dstFile;
		File dstFile2;

		srcFile = new File ("../tests/data/data1.bson".replace ('/', File.separatorChar));
		dstFile = testFolder.newFile ("testroot_1.json");
		ConvertJson.main (new String[]{ "-f", srcFile.getPath (), "-t", dstFile.getPath (), "-a" });

		srcFile = new File ("../tests/data/data3.json".replace ('/', File.separatorChar));
		dstFile2 = testFolder.newFile ("testroot_2.json");
		ConvertJson.main (new String[]{ "-f", srcFile.getPath (), "-t", dstFile2.getPath () });
		FileAssert.assertBinaryEquals (dstFile, dstFile2);
	}

	@Test
	public void testHexadecimal () throws Exception
	{
		// test Bson input with rootAsArray option

		File srcFile;
		File expectFile;
		File dstFile;

		srcFile = new File ("../tests/data/binary.bson".replace ('/', File.separatorChar));
		dstFile = testFolder.newFile ("testbinary.json");
		ConvertJson.main (new String[]{ "-f", srcFile.getPath (), "-t", dstFile.getPath (), "-a", "-x" });

		expectFile = new File ("../tests/data/binary2.json".replace ('/', File.separatorChar));
		FileAssert.assertBinaryEquals (expectFile, dstFile);
	}
}
