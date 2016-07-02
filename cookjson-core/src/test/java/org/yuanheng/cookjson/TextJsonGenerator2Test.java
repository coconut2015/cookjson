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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;

import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author	Heng Yuan
 */
public class TextJsonGenerator2Test
{
	void testFile (String f) throws IOException
	{
		File file = new File (f.replace ('/', File.separatorChar));

		StringWriter out1 = new StringWriter ();
		CookJsonParser p1 = TextJsonConfigHandler.getJsonParser (new FileInputStream (file));
		p1.next ();
		JsonValue v = p1.getValue ();
		TextJsonGenerator g1 = new TextJsonGenerator (out1);
		g1.write (v);
		p1.close ();
		g1.close ();

		StringWriter out2 = new StringWriter ();
		CookJsonParser p2 = TextJsonConfigHandler.getJsonParser (new FileInputStream (file));
		JsonGenerator g2 = new TextJsonGenerator (out2);
		Utils.convert (p2, g2);
		p2.close ();
		g2.close ();

		// because JsonObject ordering is based on hash, we cannot directly
		// compare the output.  Instead, we compare the length.
		Assert.assertEquals (out1.toString ().length (), out2.toString ().length ());
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
	}
}
