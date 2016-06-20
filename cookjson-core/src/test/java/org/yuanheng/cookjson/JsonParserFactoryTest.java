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

import java.io.*;
import java.util.HashMap;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author	Heng Yuan
 */
public class JsonParserFactoryTest
{
	void testReader (String fileName, HashMap<String, Object> config) throws IOException
	{
		File file = new File (fileName.replace ('/', File.separatorChar));

		JsonProvider provider1 = new CookJsonProvider ();

		JsonParserFactory f1 = provider1.createParserFactory (config);

		StringWriter out1 = new StringWriter ();
		JsonParser p1 = f1.createParser (new InputStreamReader (new FileInputStream (file), BOM.utf8));
		JsonGenerator g1 = new TextJsonGenerator (out1);
		Utils.convert (p1, g1);
		p1.close ();
		g1.close ();

		StringWriter out2 = new StringWriter ();
		JsonProvider provider2 = new org.glassfish.json.JsonProviderImpl ();
		JsonParserFactory f2 = provider2.createParserFactory (config);
		JsonParser p2 = f2.createParser (new InputStreamReader (new FileInputStream (file), BOM.utf8));
		JsonGenerator g2 = new TextJsonGenerator (out2);
		Utils.convert (p2, g2);
		p2.close ();
		g2.close ();

		Assert.assertEquals (out1.toString (), out2.toString ());
	}

	void testInputStream (String fileName, HashMap<String, Object> config) throws IOException
	{
		File file = new File (fileName.replace ('/', File.separatorChar));

		JsonProvider provider1 = new CookJsonProvider ();

		JsonParserFactory f1 = provider1.createParserFactory (config);

		StringWriter out1 = new StringWriter ();
		JsonParser p1 = f1.createParser (new FileInputStream (file));
		JsonGenerator g1 = new TextJsonGenerator (out1);
		Utils.convert (p1, g1);
		p1.close ();
		g1.close ();

		StringWriter out2 = new StringWriter ();
		JsonProvider provider2 = new org.glassfish.json.JsonProviderImpl ();
		JsonParserFactory f2 = provider2.createParserFactory (config);
		JsonParser p2 = f2.createParser (new FileInputStream (file));
		JsonGenerator g2 = new TextJsonGenerator (out2);
		Utils.convert (p2, g2);
		p2.close ();
		g2.close ();

		Assert.assertEquals (out1.toString (), out2.toString ());
	}

	void testCharset (String fileName, HashMap<String, Object> config) throws IOException
	{
		File file = new File (fileName.replace ('/', File.separatorChar));

		JsonProvider provider1 = new CookJsonProvider ();

		JsonParserFactory f1 = provider1.createParserFactory (config);

		StringWriter out1 = new StringWriter ();
		JsonParser p1 = f1.createParser (new FileInputStream (file), BOM.utf8);
		JsonGenerator g1 = new TextJsonGenerator (out1);
		Utils.convert (p1, g1);
		p1.close ();
		g1.close ();

		StringWriter out2 = new StringWriter ();
		JsonProvider provider2 = new org.glassfish.json.JsonProviderImpl ();
		JsonParserFactory f2 = provider2.createParserFactory (config);
		JsonParser p2 = f2.createParser (new FileInputStream (file), BOM.utf8);
		JsonGenerator g2 = new TextJsonGenerator (out2);
		Utils.convert (p2, g2);
		p2.close ();
		g2.close ();

		Assert.assertEquals (out1.toString (), out2.toString ());
	}

	private JsonParser createParser (JsonProvider provider, JsonValue value)
	{
		HashMap<String, Object> config = new HashMap<String, Object> ();
		JsonParserFactory f = provider.createParserFactory (config);
		if (value instanceof JsonArray)
			return f.createParser ((JsonArray)value);
		else if (value instanceof JsonObject)
			return f.createParser ((JsonObject)value);
		throw new IllegalArgumentException ("Invalid value type.");
	}

	void testStructure (String fileName) throws IOException
	{
		File file = new File (fileName.replace ('/', File.separatorChar));

		JsonValue value;
		TextJsonParser p = new TextJsonParser (new FileInputStream (file));
		p.next ();
		value = p.getValue ();
		p.close ();

		JsonProvider provider1 = new CookJsonProvider ();
		StringWriter out1 = new StringWriter ();
		JsonParser p1 = createParser (provider1, value);
		JsonGenerator g1 = new TextJsonGenerator (out1);
		Utils.convert (p1, g1);
		p1.close ();
		g1.close ();

		StringWriter out2 = new StringWriter ();
		JsonProvider provider = new org.glassfish.json.JsonProviderImpl ();
		JsonParser p2 = createParser (provider, value);
		JsonGenerator g2 = new TextJsonGenerator (out2);
		Utils.convert (p2, g2);
		p2.close ();
		g2.close ();

		Assert.assertEquals (out1.toString (), out2.toString ());
	}

	@Test
	public void test () throws IOException
	{
		HashMap<String, Object> config = new HashMap<String, Object> ();
		testReader ("../tests/data/complex1.json", config);
		testInputStream ("../tests/data/complex1.json", config);
		testCharset ("../tests/data/complex1.json", config);

		testStructure ("../tests/data/complex1.json");
		testStructure ("../tests/data/types.json");
	}
}
