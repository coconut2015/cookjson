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

import javax.json.spi.JsonProvider;
import javax.json.stream.JsonParser;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author	Heng Yuan
 */
public class PrettyTextJsonGeneratorTest
{
	void testFile (String f) throws IOException
	{
		File file = new File (f.replace ('/', File.separatorChar));

		JsonProvider provider = new org.glassfish.json.JsonProviderImpl ();

		StringWriter out1 = new StringWriter ();
		JsonParser p1 = provider.createParser (new FileInputStream (file));
		PrettyTextJsonGenerator g1 = new PrettyTextJsonGenerator (out1);
		Utils.convert (p1, g1);
		p1.close ();
		g1.close ();

		String original = Utils.getString (file);

		Assert.assertEquals (out1.toString (), original);
	}

	@Test
	public void test () throws IOException
	{
		testFile ("../tests/data/complex1_pretty.json");
	}
}
