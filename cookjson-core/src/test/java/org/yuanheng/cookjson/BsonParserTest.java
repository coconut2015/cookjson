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
import java.math.BigInteger;
import java.util.HashMap;
import java.util.NoSuchElementException;

import javax.json.JsonNumber;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author	Heng Yuan
 */
public class BsonParserTest
{
	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder ();

	void testFile (String f1, String f2) throws IOException
	{
		File file1 = new File (f1.replace ('/', File.separatorChar));
		File file2 = new File (f2.replace ('/', File.separatorChar));

		StringWriter out1 = new StringWriter ();
		BsonParser p1 = new BsonParser (new FileInputStream (file1));
		p1.setRootAsArray (true);
		Assert.assertEquals (true, p1.isRootAsArray ());
		TextJsonGenerator g1 = new TextJsonGenerator (out1);
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
		// data1.bson has 0 in Document / Array length
		testFile ("../tests/data/data1.bson", "../tests/data/data3.json");
		// data2.bson has the correct length
		testFile ("../tests/data/data2.bson", "../tests/data/data3.json");
	}

	private void eventCheck (String fileName, int expectedCount, boolean rootAsArray) throws IOException
	{
		File file = new File (fileName.replace ('/', File.separatorChar));
		BsonParser p = new BsonParser (new FileInputStream (file));
		p.setRootAsArray (rootAsArray);

		int count = 0;
		try
		{
			for (;;)
			{
				Event e = p.next ();
//				Debug.debug ("READ: " + e);
				switch (e)
				{
					case KEY_NAME:
					{
						++count;
						break;
					}
					default:
						++count;
				}
			}
		}
		catch (NoSuchElementException ex)
		{
			// expected end.
		}
		p.close ();
		Assert.assertEquals (expectedCount, count);
	}

	@Test
	public void testEvent () throws IOException
	{
		// data1.bson has 0 in Document / Array length
		eventCheck ("../tests/data/complex1.bson", 47, false);
		eventCheck ("../tests/data/binary.bson", 32, true);
	}

	@Test
	public void testLargeCstring () throws IOException
	{
		int length = 0;
		BsonParser p = new BsonParser (new FileInputStream ("../tests/data/largecstring.bson".replace ('/', File.separatorChar)));
		while (p.hasNext ())
		{
			Event e = p.next ();
			if (e == Event.KEY_NAME)
				length = p.getString ().length ();
		}
		p.close ();
		Assert.assertEquals (8554, length);
	}

	@Test
	public void testBinaryFormatBase64 () throws IOException
	{
		File bsonFile = testFolder.newFile ();
		BsonGenerator g = new BsonGenerator (new FileOutputStream (bsonFile));
		g.writeStartArray ();
		g.write (new byte[] { (byte) 0xde, (byte) 0xad, (byte) 0xbe, (byte) 0xef });
		g.writeEnd ();
		g.close ();

		CookJsonProvider provider = new CookJsonProvider ();
		HashMap<String, Object> config = new HashMap<String, Object> ();
		config.put (CookJsonProvider.FORMAT, CookJsonProvider.FORMAT_BSON);
		config.put (CookJsonProvider.BINARY_FORMAT, CookJsonProvider.BINARY_FORMAT_BASE64);
		BsonParser p = (BsonParser) provider.createParserFactory (config).createParser (new FileInputStream (bsonFile));
		Assert.assertEquals (BinaryFormat.BINARY_FORMAT_BASE64, p.getBinaryFormat ());
		String str = null;
		while (p.hasNext ())
		{
			if (p.next () == Event.VALUE_STRING)
			{
				str = p.getString ();
			}
		}
		p.close ();

		// now verify that our original data written was correct.
		Assert.assertEquals ("3q2+7w==", str);
	}

	@Test
	public void testBinaryFormatHex () throws IOException
	{
		File bsonFile = testFolder.newFile ();
		BsonGenerator g = new BsonGenerator (new FileOutputStream (bsonFile));
		g.writeStartArray ();
		g.write (new byte[] { (byte) 0xde, (byte) 0xad, (byte) 0xbe, (byte) 0xef });
		g.writeEnd ();
		g.close ();

		CookJsonProvider provider = new CookJsonProvider ();
		HashMap<String, Object> config = new HashMap<String, Object> ();
		config.put (CookJsonProvider.FORMAT, CookJsonProvider.FORMAT_BSON);
		config.put (CookJsonProvider.BINARY_FORMAT, CookJsonProvider.BINARY_FORMAT_HEX);
		BsonParser p = (BsonParser) provider.createParserFactory (config).createParser (new FileInputStream (bsonFile));
		Assert.assertEquals (BinaryFormat.BINARY_FORMAT_HEX, p.getBinaryFormat ());
		String str = null;
		while (p.hasNext ())
		{
			if (p.next () == Event.VALUE_STRING)
			{
				str = p.getString ();
			}
		}
		p.close ();

		// now verify that our original data written was correct.
		Assert.assertEquals ("deadbeef", str);
	}

	@Test
	public void testGetInt () throws IOException
	{
		File file = new File ("../tests/data/types.bson".replace ('/', File.separatorChar));
		JsonParser p = new BsonParser (new FileInputStream (file));
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
		Assert.assertArrayEquals (new int[]{ 1234, 1942892530, 2147483647, 12345, 1234, 1942892530, 2147483647, 12345, 1 }, ints);
	}

	@Test
	public void testGetLong () throws IOException
	{
		File file = new File ("../tests/data/types.bson".replace ('/', File.separatorChar));
		JsonParser p = new BsonParser (new FileInputStream (file));
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
		Assert.assertArrayEquals (new long[]{ 1234, 12345678901234L, 9223372036854775807L, 12345, 1234, 12345678901234L, 9223372036854775807L, 12345, 1 }, longs);
	}

	@Test
	public void testGetDecimal () throws IOException
	{
		File file = new File ("../tests/data/types.bson".replace ('/', File.separatorChar));
		JsonParser p = new BsonParser (new FileInputStream (file));
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
		BigDecimal d = new BigDecimal (new BigInteger ("1234567890123412345678901234").doubleValue ());
		Assert.assertArrayEquals (new BigDecimal[]{ new BigDecimal (1234), new BigDecimal (12345678901234L), d, new BigDecimal (12345.5), new BigDecimal (1234), new BigDecimal (12345678901234L), d, new BigDecimal (12345.5), new BigDecimal (1) }, decimals);
	}

	@Test
	public void testGetJsonValue () throws IOException
	{
		File file = new File ("../tests/data/types.bson".replace ('/', File.separatorChar));
		CookJsonParser p = new BsonParser (new FileInputStream (file));
		while (p.hasNext ())
		{
			switch (p.next ())
			{
				case END_ARRAY:
				case END_OBJECT:
				case KEY_NAME:
				case START_ARRAY:
				case START_OBJECT:
					break;
				case VALUE_FALSE:
					Assert.assertEquals (JsonValue.FALSE, p.getValue ());
					break;
				case VALUE_NULL:
					Assert.assertEquals (JsonValue.NULL, p.getValue ());
					break;
				case VALUE_NUMBER:
				{
					JsonNumber v = (JsonNumber) p.getValue ();
					Assert.assertEquals (p.getString (), v.toString ());
					break;
				}
				case VALUE_STRING:
				{
					JsonString v = (JsonString) p.getValue ();
					Assert.assertEquals (p.getString (), v.getString ());
					break;
				}
				case VALUE_TRUE:
					Assert.assertEquals (JsonValue.TRUE, p.getValue ());
					break;
			}
		}
		p.close ();
	}
}
