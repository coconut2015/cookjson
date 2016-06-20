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

import javax.json.JsonValue;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author	Heng Yuan
 */
public class TextJsonGeneratorTest
{
	@Test
	public void testWriteInt ()
	{
		StringWriter out = new StringWriter ();
		TextJsonGenerator g = new TextJsonGenerator (out);
		g.writeStartArray ();
		g.write (-1234);
		g.writeEnd ();
		g.close ();

		Assert.assertEquals ("[-1234]", out.toString ());
	}

	void testFile (String f) throws IOException
	{
		File file = new File (f.replace ('/', File.separatorChar));

		JsonProvider provider = new org.glassfish.json.JsonProviderImpl ();

		StringWriter out1 = new StringWriter ();
		JsonParser p1 = provider.createParser (new FileInputStream (file));
		TextJsonGenerator g1 = new TextJsonGenerator (out1);
		Utils.convert (p1, g1);
		p1.close ();
		g1.close ();

		StringWriter out2 = new StringWriter ();
		JsonParser p2 = provider.createParser (new FileInputStream (file));
		JsonGenerator g2 = provider.createGenerator (out2);
		Utils.convert (p2, g2);
		p2.close ();
		g2.close ();

		Assert.assertEquals (out1.toString (), out2.toString ());
	}

	@Test
	public void test () throws IOException
	{
		testFile ("../tests/data/complex1.json");
		testFile ("../tests/data/double.json");
		testFile ("../tests/data/empty.json");
		testFile ("../tests/data/long.json");
		testFile ("../tests/data/nested1.json");
		testFile ("../tests/data/nested2.json");
		testFile ("../tests/data/string.json");
		testFile ("../tests/data/string2.json");
		testFile ("../tests/data/string3.json");
		testFile ("../tests/data/number4.json");
		testFile ("../tests/data/types.json");
	}

	private void testJsonValueJson (String f) throws IOException
	{
		File file = new File (f.replace ('/', File.separatorChar));

		JsonValue value;

		TextJsonParser p = new TextJsonParser (new FileInputStream (file));
		p.next ();
		value = p.getValue ();
		p.close ();

		StringWriter out1 = new StringWriter ();
		JsonGenerator g1 = new TextJsonGenerator (out1);
		g1.writeStartArray ();
		g1.write (value);
		g1.writeEnd ();
		g1.close ();

		JsonProvider provider = new org.glassfish.json.JsonProviderImpl ();
		StringWriter out2 = new StringWriter ();
		JsonGenerator g2 = provider.createGenerator (out2);
		g2.writeStartArray ();
		g2.write (value);
		g2.writeEnd ();
		g2.close ();

		Assert.assertEquals (out1.toString (), out2.toString ());
	}

	private void testJsonValueBson (String f) throws IOException
	{
		File file = new File (f.replace ('/', File.separatorChar));

		JsonValue value;

		BsonParser p = new BsonParser (new FileInputStream (file));
		p.setRootAsArray (true);
		p.next ();
		value = p.getValue ();
		p.close ();

		StringWriter out1 = new StringWriter ();
		JsonGenerator g1 = new TextJsonGenerator (out1);
		g1.writeStartArray ();
		g1.write (value);
		g1.writeEnd ();
		g1.close ();

		JsonProvider provider = new org.glassfish.json.JsonProviderImpl ();
		StringWriter out2 = new StringWriter ();
		JsonGenerator g2 = provider.createGenerator (out2);
		g2.writeStartArray ();
		g2.write (value);
		g2.writeEnd ();
		g2.close ();

		Assert.assertEquals (out1.toString (), out2.toString ());
	}

	@Test
	public void testJsonValue () throws IOException
	{
		testJsonValueJson ("../tests/data/complex1.json");
		testJsonValueBson ("../tests/data/binary.bson");
	}
}
