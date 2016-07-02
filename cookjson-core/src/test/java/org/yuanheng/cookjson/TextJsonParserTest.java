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
import java.math.BigDecimal;

import javax.json.spi.JsonProvider;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonLocation;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author	Heng Yuan
 */
public class TextJsonParserTest
{
	private TextJsonParser getJsonParser (File file) throws IOException
	{
		return new TextJsonParser (new InputStreamReader (new FileInputStream (file), BOM.utf8));
	}

	@Test
	public void testGetInt () throws IOException
	{
		File file = new File ("../tests/data/types.json".replace ('/', File.separatorChar));
		JsonParser p = getJsonParser (file);
		int[] ints = new int[9];
		int count = 0;
		while (p.hasNext ())
		{
			if (p.next () == Event.VALUE_NUMBER)
			{
				ints[count++] = p.getInt ();
			}
		}
		p.close ();
		Assert.assertArrayEquals (new int[]{ 1234, 1942892530, -115429390, 12345, 1234, 1942892530, -115429390, 12345, 1 }, ints);
	}

	@Test
	public void testGetLong () throws IOException
	{
		File file = new File ("../tests/data/types.json".replace ('/', File.separatorChar));
		JsonParser p = getJsonParser (file);
		long[] longs = new long[9];
		int count = 0;
		while (p.hasNext ())
		{
			if (p.next () == Event.VALUE_NUMBER)
			{
				longs[count++] = p.getLong ();
			}
		}
		p.close ();
		Assert.assertArrayEquals (new long[]{ 1234, 12345678901234L, 7888426545362939890L, 12345, 1234, 12345678901234L, 7888426545362939890L, 12345, 1 }, longs);
	}

	@Test
	public void testGetDecimal () throws IOException
	{
		File file = new File ("../tests/data/types.json".replace ('/', File.separatorChar));
		JsonParser p = getJsonParser (file);
		BigDecimal[] decimals = new BigDecimal[9];
		int count = 0;
		while (p.hasNext ())
		{
			if (p.next () == Event.VALUE_NUMBER)
			{
				decimals[count++] = p.getBigDecimal ();
			}
		}
		p.close ();
		Assert.assertArrayEquals (new BigDecimal[]{ new BigDecimal (1234), new BigDecimal (12345678901234L), new BigDecimal ("1234567890123412345678901234"), new BigDecimal (12345.5), new BigDecimal (1234), new BigDecimal (12345678901234L), new BigDecimal ("1234567890123412345678901234"), new BigDecimal (12345.5), new BigDecimal (1) }, decimals);
	}

	void testFile (String f) throws IOException
	{
		File file = new File (f.replace ('/', File.separatorChar));

		StringWriter out1 = new StringWriter ();
		TextJsonParser p1 = getJsonParser (file);
		TextJsonGenerator g1 = new TextJsonGenerator (out1);
		Utils.convert (p1, g1);
		p1.close ();
		g1.close ();

		StringWriter out2 = new StringWriter ();
		JsonProvider provider = new org.glassfish.json.JsonProviderImpl ();
		JsonParser p2 = provider.createParser (new FileInputStream (file));
		JsonGenerator g2 = new TextJsonGenerator (out2);
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
		testFile ("../tests/data/number.json");
		testFile ("../tests/data/string.json");
		testFile ("../tests/data/string2.json");
		testFile ("../tests/data/string3.json");
		testFile ("../tests/data/types.json");
	}

	@Test
	public void testLocation1 ()
	{
		String json = "{\"abc\" : -1234}";

	    TextJsonParser p = new TextJsonParser (new StringReader (json));
	    JsonLocation location = null;
		while (p.hasNext ())
		{
			switch (p.next ())
			{
				case VALUE_NUMBER:
					location = p.getLocation ();
					break;
				default:
					break;
			}
		}
		p.close ();
		Assert.assertEquals (1, location.getLineNumber ());
		Assert.assertEquals (10, location.getColumnNumber ());
		Assert.assertEquals (9, location.getStreamOffset ());
		Assert.assertEquals ("line 1, column 10, offset 9", location.toString ());
	}

	@Test
	public void testLocation2 ()
	{
		String json = "{\"abc\" : -1234}";

	    TextJsonParser p = new TextJsonParser (new StringReader (json));
	    JsonLocation location = null;
		while (p.hasNext ())
		{
			switch (p.next ())
			{
				case KEY_NAME:
					location = p.getLocation ();
					break;
				default:
					break;
			}
		}
		p.close ();
		Assert.assertEquals (1, location.getLineNumber ());
		Assert.assertEquals (2, location.getColumnNumber ());
		Assert.assertEquals (1, location.getStreamOffset ());
	}

	@Test
	public void testLocation3 ()
	{
		String json = "{\"abc\\t\" : \"def\"}";

	    TextJsonParser p = new TextJsonParser (new StringReader (json));
	    JsonLocation location = null;
		while (p.hasNext ())
		{
			switch (p.next ())
			{
				case VALUE_STRING:
					location = p.getLocation ();
					break;
				default:
					break;
			}
		}
		p.close ();
		Assert.assertEquals (1, location.getLineNumber ());
		Assert.assertEquals (12, location.getColumnNumber ());
		Assert.assertEquals (11, location.getStreamOffset ());
	}

	@Test
	public void testLocation4 ()
	{
		String json = "{\"abc\\t\" : []}";

	    TextJsonParser p = new TextJsonParser (new StringReader (json));
	    JsonLocation location = null;
		while (p.hasNext ())
		{
			switch (p.next ())
			{
				case START_ARRAY:
					location = p.getLocation ();
					break;
				default:
					break;
			}
		}
		p.close ();
		Assert.assertEquals (1, location.getLineNumber ());
		Assert.assertEquals (12, location.getColumnNumber ());
		Assert.assertEquals (11, location.getStreamOffset ());
	}

	@Test
	public void testLocation5 ()
	{
		String json = "[ true, false, null ]";

	    TextJsonParser p = new TextJsonParser (new StringReader (json));
	    JsonLocation location = null;
		while (p.hasNext ())
		{
			switch (p.next ())
			{
				case VALUE_TRUE:
					location = p.getLocation ();
					break;
				default:
					break;
			}
		}
		p.close ();
		Assert.assertEquals (1, location.getLineNumber ());
		Assert.assertEquals (3, location.getColumnNumber ());
		Assert.assertEquals (2, location.getStreamOffset ());
	}

	@Test
	public void testLocation6 ()
	{
		String json = "[ true, false, null ]";

	    TextJsonParser p = new TextJsonParser (new StringReader (json));
	    JsonLocation location = null;
		while (p.hasNext ())
		{
			switch (p.next ())
			{
				case VALUE_FALSE:
					location = p.getLocation ();
					break;
				default:
					break;
			}
		}
		p.close ();
		Assert.assertEquals (1, location.getLineNumber ());
		Assert.assertEquals (9, location.getColumnNumber ());
		Assert.assertEquals (8, location.getStreamOffset ());
	}

	@Test
	public void testLocation7 ()
	{
		String json = "[ true, false, null ]";

	    TextJsonParser p = new TextJsonParser (new StringReader (json));
	    JsonLocation location = null;
		while (p.hasNext ())
		{
			switch (p.next ())
			{
				case VALUE_NULL:
					location = p.getLocation ();
					break;
				default:
					break;
			}
		}
		p.close ();
		Assert.assertEquals (1, location.getLineNumber ());
		Assert.assertEquals (16, location.getColumnNumber ());
		Assert.assertEquals (15, location.getStreamOffset ());
	}
}
