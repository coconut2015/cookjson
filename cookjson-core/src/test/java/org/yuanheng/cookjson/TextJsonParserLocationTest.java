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
import java.util.NoSuchElementException;

import javax.json.stream.JsonParser.Event;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author	Heng Yuan
 */
public class TextJsonParserLocationTest
{
	void testFile (String f, String str) throws IOException
	{
		File file = new File (f.replace ('/', File.separatorChar));

		StringWriter out = new StringWriter ();
		TextJsonParser p = new TextJsonParser (new FileInputStream (file));
		try
		{
			for (;;)
			{
				Event e = p.next ();
				out.write (e.toString ());
				out.write (":");
				out.write (p.getLocation ().toString ());
				out.write ('\n');
			}
		}
		catch (NoSuchElementException ex)
		{
		}
		p.close ();
		Assert.assertEquals (str, out.toString ());
	}

	@Test
	public void test () throws IOException
	{
		testFile ("../tests/data/complex1.json", "START_OBJECT:Line (1) Column (1) Offset (0)\n"
				+ "KEY_NAME:Line (2) Column (2) Offset (4)\n" + "VALUE_NUMBER:Line (2) Column (9) Offset (11)\n"
				+ "KEY_NAME:Line (3) Column (2) Offset (24)\n" + "VALUE_NUMBER:Line (3) Column (9) Offset (31)\n"
				+ "KEY_NAME:Line (4) Column (2) Offset (39)\n" + "VALUE_NUMBER:Line (4) Column (11) Offset (48)\n"
				+ "KEY_NAME:Line (5) Column (2) Offset (61)\n" + "VALUE_NUMBER:Line (5) Column (12) Offset (71)\n"
				+ "KEY_NAME:Line (6) Column (2) Offset (90)\n" + "VALUE_TRUE:Line (6) Column (14) Offset (102)\n"
				+ "KEY_NAME:Line (7) Column (2) Offset (110)\n" + "VALUE_FALSE:Line (7) Column (15) Offset (123)\n"
				+ "KEY_NAME:Line (8) Column (2) Offset (132)\n" + "VALUE_NULL:Line (8) Column (11) Offset (141)\n"
				+ "KEY_NAME:Line (9) Column (2) Offset (149)\n" + "VALUE_STRING:Line (9) Column (11) Offset (158)\n"
				+ "KEY_NAME:Line (10) Column (2) Offset (207)\n" + "VALUE_NUMBER:Line (10) Column (11) Offset (216)\n"
				+ "KEY_NAME:Line (11) Column (2) Offset (221)\n" + "START_OBJECT:Line (11) Column (14) Offset (233)\n"
				+ "KEY_NAME:Line (12) Column (3) Offset (238)\n" + "VALUE_NUMBER:Line (12) Column (11) Offset (246)\n"
				+ "KEY_NAME:Line (13) Column (3) Offset (255)\n" + "VALUE_STRING:Line (13) Column (16) Offset (268)\n"
				+ "KEY_NAME:Line (14) Column (3) Offset (282)\n" + "START_ARRAY:Line (14) Column (13) Offset (292)\n"
				+ "VALUE_NUMBER:Line (14) Column (16) Offset (295)\n"
				+ "VALUE_NUMBER:Line (14) Column (20) Offset (299)\n"
				+ "VALUE_NUMBER:Line (14) Column (24) Offset (303)\n" + "END_ARRAY:Line (14) Column (26) Offset (305)\n"
				+ "END_OBJECT:Line (15) Column (2) Offset (309)\n" + "KEY_NAME:Line (16) Column (2) Offset (314)\n"
				+ "START_OBJECT:Line (16) Column (13) Offset (325)\n"
				+ "END_OBJECT:Line (16) Column (14) Offset (326)\n" + "KEY_NAME:Line (17) Column (2) Offset (331)\n"
				+ "START_ARRAY:Line (17) Column (16) Offset (345)\n"
				+ "VALUE_NUMBER:Line (17) Column (19) Offset (348)\n"
				+ "VALUE_STRING:Line (17) Column (22) Offset (351)\n"
				+ "VALUE_NUMBER:Line (17) Column (30) Offset (359)\n" + "END_ARRAY:Line (17) Column (37) Offset (366)\n"
				+ "KEY_NAME:Line (18) Column (2) Offset (371)\n" + "START_ARRAY:Line (18) Column (13) Offset (382)\n"
				+ "END_ARRAY:Line (18) Column (14) Offset (383)\n" + "KEY_NAME:Line (19) Column (2) Offset (388)\n"
				+ "VALUE_STRING:Line (19) Column (16) Offset (402)\n"
				+ "END_OBJECT:Line (20) Column (1) Offset (425)\n");
	}
}
