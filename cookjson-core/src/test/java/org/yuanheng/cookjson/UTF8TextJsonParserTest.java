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
public class UTF8TextJsonParserTest
{
	private UTF8TextJsonParser getJsonParser (File file, int buffType) throws IOException
	{
		InputStream is = new FileInputStream (file);
		if (buffType > 0)
		{
			if (buffType == 1)
				return new UTF8TextJsonParser (is, 2);
			if (buffType == 2)
				return new UTF8TextJsonParser (is, 100000);
		}
		return new UTF8TextJsonParser (is);
	}

	@Test
	public void testGetInt () throws IOException
	{
		File file = new File ("../tests/data/types.json".replace ('/', File.separatorChar));
		JsonParser p = getJsonParser (file, 0);
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
		Assert.assertArrayEquals (new int[]
		{ 1234, 1942892530, -115429390, 12345, 1234, 1942892530, -115429390, 12345, 1 }, ints);
	}

	@Test
	public void testGetInt_2 () throws IOException
	{
		File file = new File ("../tests/data/types.json".replace ('/', File.separatorChar));
		JsonParser p = getJsonParser (file, 1);
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
		Assert.assertArrayEquals (new int[]
		{ 1234, 1942892530, -115429390, 12345, 1234, 1942892530, -115429390, 12345, 1 }, ints);
	}

	@Test
	public void testGetLong () throws IOException
	{
		File file = new File ("../tests/data/types.json".replace ('/', File.separatorChar));
		JsonParser p = getJsonParser (file, 0);
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
		Assert.assertArrayEquals (new long[]
		{ 1234, 12345678901234L, 7888426545362939890L, 12345, 1234, 12345678901234L, 7888426545362939890L, 12345, 1 },
				longs);
	}

	@Test
	public void testGetLong_2 () throws IOException
	{
		File file = new File ("../tests/data/types.json".replace ('/', File.separatorChar));
		JsonParser p = getJsonParser (file, 1);
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
		Assert.assertArrayEquals (new long[]
		{ 1234, 12345678901234L, 7888426545362939890L, 12345, 1234, 12345678901234L, 7888426545362939890L, 12345, 1 },
				longs);
	}

	@Test
	public void testGetDecimal () throws IOException
	{
		File file = new File ("../tests/data/types.json".replace ('/', File.separatorChar));
		JsonParser p = getJsonParser (file, 0);
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
		Assert.assertArrayEquals (new BigDecimal[]
		{ new BigDecimal (1234), new BigDecimal (12345678901234L), new BigDecimal ("1234567890123412345678901234"),
				new BigDecimal (12345.5), new BigDecimal (1234), new BigDecimal (12345678901234L),
				new BigDecimal ("1234567890123412345678901234"), new BigDecimal (12345.5), new BigDecimal (1) },
				decimals);
	}

	void testFile (String f, int buffType) throws IOException
	{
		File file = new File (f.replace ('/', File.separatorChar));

		StringWriter out1 = new StringWriter ();
		UTF8TextJsonParser p1 = getJsonParser (file, buffType);
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

		Assert.assertEquals (out2.toString (), out1.toString ());
	}

	@Test
	public void test () throws IOException
	{
		testFile ("../tests/data/complex1.json", 0);
		testFile ("../tests/data/double.json", 0);
		testFile ("../tests/data/empty.json", 0);
		testFile ("../tests/data/long.json", 0);
		testFile ("../tests/data/number.json", 0);
		testFile ("../tests/data/string.json", 0);
		testFile ("../tests/data/string2.json", 0);
		testFile ("../tests/data/string3.json", 0);
		testFile ("../tests/data/string4.json", 0);
		testFile ("../tests/data/types.json", 0);
	}

	@Test
	public void testSmallBuf () throws IOException
	{
		testFile ("../tests/data/complex1.json", 1);
		testFile ("../tests/data/double.json", 1);
		testFile ("../tests/data/empty.json", 1);
		testFile ("../tests/data/long.json", 1);
		testFile ("../tests/data/number.json", 1);
		testFile ("../tests/data/string.json", 1);
		testFile ("../tests/data/string2.json", 1);
		testFile ("../tests/data/string3.json", 1);
		testFile ("../tests/data/string4.json", 1);
		testFile ("../tests/data/types.json", 1);
	}

	@Test
	public void testBigBuf () throws IOException
	{
		testFile ("../tests/data/string4.json", 2);
	}

	@Test
	public void testLocation1 ()
	{
		String json = "{\"abc\" : -1234}";

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		JsonLocation location = null;
		while (p.hasNext ())
		{
			switch (p.next ())
			{
				case KEY_NAME:
					Assert.assertEquals ("abc", p.getString ());
					location = p.getLocation ();
					Assert.assertEquals ("line 1, column 2, offset 1", location.toString ());
					break;
				case VALUE_NUMBER:
					Assert.assertEquals ("-1234", p.getString ());
					location = p.getLocation ();
					Assert.assertEquals (1, location.getLineNumber ());
					Assert.assertEquals (10, location.getColumnNumber ());
					Assert.assertEquals (9, location.getStreamOffset ());
					Assert.assertEquals ("line 1, column 10, offset 9", location.toString ());
					break;
				default:
					break;
			}
		}
		p.close ();
	}

	@Test
	public void testLocation2 ()
	{
		String json = "{\"abc\" : 1234}";

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		JsonLocation location = null;
		while (p.hasNext ())
		{
			switch (p.next ())
			{
				case KEY_NAME:
					Assert.assertEquals ("abc", p.getString ());
					location = p.getLocation ();
					Assert.assertEquals ("line 1, column 2, offset 1", location.toString ());
					break;
				case VALUE_NUMBER:
					Assert.assertEquals ("1234", p.getString ());
					location = p.getLocation ();
					Assert.assertEquals ("line 1, column 10, offset 9", location.toString ());
					break;
				default:
					break;
			}
		}
		p.close ();
	}

	@Test
	public void testLocation2_2 ()
	{
		String json = "{\"abc\" : 1234}";

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)), 2);
		JsonLocation location = null;
		while (p.hasNext ())
		{
			switch (p.next ())
			{
				case KEY_NAME:
					Assert.assertEquals ("abc", p.getString ());
					location = p.getLocation ();
					Assert.assertEquals ("line 1, column 2, offset 1", location.toString ());
					break;
				case VALUE_NUMBER:
					Assert.assertEquals ("1234", p.getString ());
					location = p.getLocation ();
					Assert.assertEquals ("line 1, column 10, offset 9", location.toString ());
					break;
				default:
					break;
			}
		}
		p.close ();
	}

	@Test
	public void testLocation3 ()
	{
		String json = "{\"abc\\t\" : \"def\\t\\\"ghi\"}";

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		JsonLocation location = null;
		while (p.hasNext ())
		{
			switch (p.next ())
			{
				case KEY_NAME:
					Assert.assertEquals ("abc\t", p.getString ());
					location = p.getLocation ();
					Assert.assertEquals ("line 1, column 2, offset 1", location.toString ());
					break;
				case VALUE_STRING:
					Assert.assertEquals ("def\t\"ghi", p.getString ());
					location = p.getLocation ();
					Assert.assertEquals ("line 1, column 12, offset 11", location.toString ());
					break;
				default:
					break;
			}
		}
		p.close ();
	}

	@Test
	public void testLocation4 ()
	{
		String json = "{\"abc\\t\" : []}";

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
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

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
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

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
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

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
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

	@Test
	public void testNumber1 ()
	{
		String json = "{\"abc\" : 0 }";

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		while (p.hasNext ())
		{
			switch (p.next ())
			{
				case VALUE_NUMBER:
					Assert.assertEquals ("0", p.getString ());
					break;
				default:
					break;
			}
		}
		p.close ();
	}

	@Test
	public void testNumber2 ()
	{
		String json = "{\"abc\" : 0.1 }";

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		while (p.hasNext ())
		{
			switch (p.next ())
			{
				case VALUE_NUMBER:
					Assert.assertEquals ("0.1", p.getString ());
					break;
				default:
					break;
			}
		}
		p.close ();
	}

	@Test
	public void testNumber3 ()
	{
		String json = "{\"abc\" : -0.1 }";

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		while (p.hasNext ())
		{
			switch (p.next ())
			{
				case VALUE_NUMBER:
					Assert.assertEquals ("-0.1", p.getString ());
					break;
				default:
					break;
			}
		}
		p.close ();
	}

	@Test
	public void testNumber4 ()
	{
		String json = "{\"abc\" : 0.1e2 }";

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		while (p.hasNext ())
		{
			switch (p.next ())
			{
				case VALUE_NUMBER:
					Assert.assertEquals ("0.1e2", p.getString ());
					break;
				default:
					break;
			}
		}
		p.close ();
	}

	@Test
	public void testNumber5 ()
	{
		String json = "{\"abc\" : -0.1e2 }";

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		while (p.hasNext ())
		{
			switch (p.next ())
			{
				case VALUE_NUMBER:
					Assert.assertEquals ("-0.1e2", p.getString ());
					break;
				default:
					break;
			}
		}
		p.close ();
	}

	@Test
	public void testNumber6 ()
	{
		String json = "{\"abc\" : -0.1234567890 }";

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		while (p.hasNext ())
		{
			switch (p.next ())
			{
				case VALUE_NUMBER:
					Assert.assertEquals ("-0.1234567890", p.getString ());
					break;
				default:
					break;
			}
		}
		p.close ();
	}

	@Test
	public void testNumber7 ()
	{
		String json = "{\"abc\" : 99e12 }";

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		while (p.hasNext ())
		{
			switch (p.next ())
			{
				case VALUE_NUMBER:
					Assert.assertEquals ("99e12", p.getString ());
					break;
				default:
					break;
			}
		}
		p.close ();
	}

	@Test
	public void testZero1 ()
	{
		String json = "[ -0 ]";

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		while (p.hasNext ())
		{
			switch (p.next ())
			{
				case VALUE_NUMBER:
					Assert.assertEquals ("-0", p.getString ());
					break;
				default:
					break;
			}
		}
		p.close ();
	}

	@Test
	public void testZero2 ()
	{
		String json = "[ 0e0 ]";

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		while (p.hasNext ())
		{
			switch (p.next ())
			{
				case VALUE_NUMBER:
					Assert.assertEquals ("0e0", p.getString ());
					break;
				default:
					break;
			}
		}
		p.close ();
	}

	@Test
	public void testZero3 ()
	{
		String json = "[ -0E0 ]";

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		while (p.hasNext ())
		{
			switch (p.next ())
			{
				case VALUE_NUMBER:
					Assert.assertEquals ("-0E0", p.getString ());
					break;
				default:
					break;
			}
		}
		p.close ();
	}

	@Test
	public void testZero4 ()
	{
		String json = "[ -0E40 ]";

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		while (p.hasNext ())
		{
			switch (p.next ())
			{
				case VALUE_NUMBER:
					Assert.assertEquals ("-0E40", p.getString ());
					break;
				default:
					break;
			}
		}
		p.close ();
	}
}
