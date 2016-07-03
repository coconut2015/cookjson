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

import javax.json.spi.JsonProvider;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParsingException;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author	Heng Yuan
 */
public class CommentTest
{
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private JsonParser getJsonParser (Reader r, boolean smallBuf) throws IOException
	{
		if (smallBuf)
		{
			TextJsonParser p = new TextJsonParser (r, 2);
			Assert.assertFalse (p.isAllowComments ());
			p.setAllowComments (true);
			Assert.assertTrue (p.isAllowComments ());
			return p;
		}
		HashMap<String, Object> config = new HashMap<String, Object> ();
		config.put (CookJsonProvider.COMMENT, Boolean.TRUE);
		JsonProvider provider = new CookJsonProvider ();
		return provider.createParserFactory (config).createParser (r);
	}

	private JsonParser getJsonParser (File file, boolean smallBuf) throws IOException
	{
		Reader r = new InputStreamReader (new FileInputStream (file), BOM.utf8);
		return getJsonParser (r, smallBuf);
	}

	void testFile (String f1, String f2, boolean smallBuf) throws IOException
	{
		File file1 = new File (f1.replace ('/', File.separatorChar));
		File file2 = new File (f2.replace ('/', File.separatorChar));

		StringWriter out1 = new StringWriter ();
		JsonParser p1 = getJsonParser (file1, smallBuf);
		JsonGenerator g1 = new TextJsonGenerator (out1);
		Utils.convert (p1, g1);
		p1.close ();
		g1.close ();

		StringWriter out2 = new StringWriter ();
		JsonProvider provider = new org.glassfish.json.JsonProviderImpl ();
		JsonParser p2 = provider.createParser (new FileInputStream (file2));
		JsonGenerator g2 = new TextJsonGenerator (out2);
		Utils.convert (p2, g2);
		p2.close ();
		g2.close ();

		Assert.assertEquals (out1.toString (), out2.toString ());
	}

	@Test
	public void test () throws IOException
	{
		testFile ("../tests/data/comment.json", "../tests/data/complex1.json", false);
		testFile ("../tests/data/comment2.json", "../tests/data/complex1.json", false);
	}

	@Test
	public void testSmallBuf () throws IOException
	{
		testFile ("../tests/data/comment.json", "../tests/data/complex1.json", true);
		testFile ("../tests/data/comment2.json", "../tests/data/complex1.json", true);
	}

	@Test
	public void testError1 ()
	{
		String json = "{\"a\" :\n//abc\u0000\n 99e12 }";
		System.out.println (json);

	    thrown.expect (JsonParsingException.class);
	    thrown.expectMessage ("Parsing error at line 2, column 6, offset 12: unexpected character '\\u0000'");

	    TextJsonParser p = new TextJsonParser (new StringReader (json), 2);
	    p.setAllowComments (true);
		while (p.hasNext ())
		{
			p.next ();
		}
		p.close ();
	}

	@Test
	public void testError2 ()
	{
		String json = "{\"a\" :\n/*abc\u0000\n*/ 99e12 }";
		System.out.println (json);

	    thrown.expect (JsonParsingException.class);
	    thrown.expectMessage ("Parsing error at line 2, column 6, offset 12: unexpected character '\\u0000'");

	    TextJsonParser p = new TextJsonParser (new StringReader (json), 2);
	    p.setAllowComments (true);
		while (p.hasNext ())
		{
			p.next ();
		}
		p.close ();
	}
}
