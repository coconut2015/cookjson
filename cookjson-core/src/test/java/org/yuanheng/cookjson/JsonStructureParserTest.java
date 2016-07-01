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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;

import javax.json.*;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;
import javax.json.stream.JsonParserFactory;

import org.apache.commons.codec.binary.Hex;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author	Heng Yuan
 */
public class JsonStructureParserTest
{
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

	void testFile (String f) throws IOException
	{
		File file = new File (f.replace ('/', File.separatorChar));

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
		testFile ("../tests/data/complex1.json");
		testFile ("../tests/data/double.json");
		testFile ("../tests/data/empty.json");
		testFile ("../tests/data/long.json");
	}

	private void eventCount (String f, int expect)  throws IOException
	{
		File file = new File (f.replace ('/', File.separatorChar));

		JsonValue value;
		TextJsonParser p = new TextJsonParser (new FileInputStream (file));
		p.next ();
		value = p.getValue ();
		p.close ();

		JsonProvider provider = new CookJsonProvider ();
		JsonParser p2 = createParser (provider, value);

		int count;
		for (count = 0; p2.hasNext (); ++count)
			p2.next ();
		p2.close ();

		Assert.assertEquals (expect, count);;
	}

	@Test
	public void testCount () throws IOException
	{
		eventCount ("../tests/data/complex1.json", 47);
		eventCount ("../tests/data/double.json", 16);
		eventCount ("../tests/data/empty.json", 2);
		eventCount ("../tests/data/long.json", 10);
	}

	@Test
	public void testBinary () throws IOException
	{
		File file = new File ("../tests/data/binary.bson".replace ('/', File.separatorChar));
		CookJsonProvider provider = new CookJsonProvider ();
		HashMap<String, Object> config = new HashMap<String, Object> ();
		config.put (CookJsonProvider.FORMAT, CookJsonProvider.FORMAT_BSON);
		config.put (CookJsonProvider.BINARY_FORMAT, CookJsonProvider.BINARY_FORMAT_HEX);
		JsonReader r = provider.createReaderFactory (config).createReader (new FileInputStream (file));
		JsonStructure v = r.read ();
		r.close ();

		CookJsonParser p = new JsonStructureParser (v);
		while (p.hasNext ())
		{
			if (p.next () == Event.VALUE_STRING)
			{
				if (p.isBinary ())
				{
					Assert.assertEquals (p.getString (), Hex.encodeHexString (p.getBytes ()));
				}
			}
		}
		p.close ();
	}

	private int sum (int[] array)
	{
		int total = 0;
		for (int i : array)
			total += i;
		return total;
	}

	private long sum (long[] array)
	{
		long total = 0;
		for (long i : array)
			total += i;
		return total;
	}

	private BigInteger sum (BigInteger[] array)
	{
		BigInteger total = BigInteger.ZERO;
		for (BigInteger i : array)
			total.add (i);
		return total;
	}

	private BigDecimal sum (BigDecimal[] array)
	{
		BigDecimal total = BigDecimal.ZERO;
		for (BigDecimal i : array)
			total.add (i);
		return total;
	}

	@Test
	public void testGetInt () throws IOException
	{
		File file = new File ("../tests/data/types.json".replace ('/', File.separatorChar));
		CookJsonProvider provider = new CookJsonProvider ();
		JsonReader r = provider.createReader (new FileInputStream (file));
		JsonStructure v = r.read ();
		r.close ();

		CookJsonParser p = new JsonStructureParser (v);
		int[] ints = new int[9];
		int count = 0;
		while (p.hasNext ())
		{
			if (p.next () == Event.VALUE_NUMBER)
			{
				Assert.assertEquals (Event.VALUE_NUMBER, p.getEvent ());
				Assert.assertTrue (p.getValue () instanceof JsonNumber);
				Assert.assertEquals (p.getString (), ((JsonNumber)p.getValue ()).toString ());
				ints[count++] = p.getInt ();
				Assert.assertEquals (JsonLocationImpl.Unknown, p.getLocation ());
			}
		}
		p.close ();

		Assert.assertEquals (sum (new int[]{ 1234, 1942892530, -115429390, 12345, 1234, 1942892530, -115429390, 12345, 1 }), sum (ints));
	}

	@Test
	public void testGetLong () throws IOException
	{
		File file = new File ("../tests/data/types.json".replace ('/', File.separatorChar));
		CookJsonProvider provider = new CookJsonProvider ();
		JsonReader r = provider.createReader (new FileInputStream (file));
		JsonStructure v = r.read ();
		r.close ();

		CookJsonParser p = new JsonStructureParser (v);
		long[] longs = new long[9];
		int count = 0;
		while (p.hasNext ())
		{
			if (p.next () == Event.VALUE_NUMBER)
			{
				Assert.assertTrue (p.getValue () instanceof JsonNumber);
				longs[count++] = p.getLong ();
			}
		}
		p.close ();
		Assert.assertEquals (sum (new long[]{ 1234, 12345678901234L, 7888426545362939890L, 12345, 1234, 12345678901234L, 7888426545362939890L, 12345, 1 }), sum (longs));
	}

	@Test
	public void testGetBigInteger () throws IOException
	{
		File file = new File ("../tests/data/types.json".replace ('/', File.separatorChar));
		CookJsonParser p = new TextJsonParser (new FileInputStream (file));
		BigInteger[] bigints = new BigInteger[9];
		int count = 0;
		while (p.hasNext ())
		{
			if (p.next () == Event.VALUE_NUMBER)
			{
				Assert.assertTrue (p.getValue () instanceof JsonNumber);
				bigints[count++] = p.getBigDecimal ().toBigInteger ();
			}
		}
		p.close ();
		Assert.assertEquals (sum (new BigInteger[]{ new BigInteger ("1234"), new BigInteger ("12345678901234"), new BigInteger ("1234567890123412345678901234"), new BigInteger ("12345"), new BigInteger ("1234"), new BigInteger ("12345678901234"), new BigInteger ("1234567890123412345678901234"), new BigInteger ("12345"), new BigInteger ("1") }), sum (bigints));
	}

	@Test
	public void testGetDecimal () throws IOException
	{
		File file = new File ("../tests/data/types.json".replace ('/', File.separatorChar));
		CookJsonProvider provider = new CookJsonProvider ();
		JsonReader r = provider.createReader (new FileInputStream (file));
		JsonStructure v = r.read ();
		r.close ();

		CookJsonParser p = new JsonStructureParser (v);
		BigDecimal[] decimals = new BigDecimal[9];
		int count = 0;
		while (p.hasNext ())
		{
			if (p.next () == Event.VALUE_NUMBER)
			{
				Assert.assertTrue (p.getValue () instanceof JsonNumber);
				decimals[count++] = p.getBigDecimal ();
			}
		}
		p.close ();
		Assert.assertEquals (sum (new BigDecimal[]{ new BigDecimal (1234), new BigDecimal (12345678901234L), new BigDecimal ("1234567890123412345678901234"), new BigDecimal (12345.5), new BigDecimal (1234), new BigDecimal (12345678901234L), new BigDecimal ("1234567890123412345678901234"), new BigDecimal (12345.5), new BigDecimal (1) }), sum (decimals));
	}

}
