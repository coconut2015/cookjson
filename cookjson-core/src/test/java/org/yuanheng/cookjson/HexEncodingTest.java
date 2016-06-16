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

import java.io.StringWriter;
import java.util.HashMap;

import javax.json.spi.JsonProvider;
import javax.json.stream.JsonGeneratorFactory;

import org.apache.commons.codec.binary.Hex;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author	Heng Yuan
 */
public class HexEncodingTest
{
	private void testEncoding (byte[] value)
	{
		JsonProvider provider = new CookJsonProvider ();
		HashMap<String, Object> config = new HashMap<String, Object> ();
		config.put (CookJsonProvider.BINARY_FORMAT, CookJsonProvider.BINARY_FORMAT_HEX);
		JsonGeneratorFactory f = provider.createGeneratorFactory (config);

		StringWriter sw = new StringWriter ();
		CookJsonGenerator g = (CookJsonGenerator) f.createGenerator (sw);
		g.writeStartArray ();
		g.write (value);
		g.writeEnd ();
		g.close ();

		String expected = "[\"" + Hex.encodeHexString (value) + "\"]";
		Assert.assertEquals (expected, sw.toString ());

		sw = new StringWriter ();
		g = (CookJsonGenerator) f.createGenerator (sw);
		g.writeStartObject ();
		g.write ("test", value);
		g.writeEnd ();
		g.close ();

		expected = "{\"test\":\"" + Hex.encodeHexString (value) + "\"}";
		Assert.assertEquals (expected, sw.toString ());
	}

	@Test
	public void test ()
	{
		testEncoding (new byte[0]);
		testEncoding (new byte[1]);
		testEncoding (new byte[2]);
		testEncoding (new byte[]{ (byte) 0xde, (byte) 0xad, (byte) 0xbe, (byte) 0xef });
		testEncoding ("any carnal pleas".getBytes (BOM.utf8));
		testEncoding ("any carnal pleasu".getBytes (BOM.utf8));
		testEncoding ("any carnal pleasur".getBytes (BOM.utf8));
		testEncoding ("any carnal pleasure".getBytes (BOM.utf8));
		testEncoding ("any carnal pleasure.".getBytes (BOM.utf8));
		testEncoding (("Man is distinguished, not only by his reason, but by this singular passion from\n"
				+ "other animals, which is a lust of the mind, that by a perseverance of delight\n"
				+ "in the continued and indefatigable generation of knowledge, exceeds the short\n"
				+ "vehemence of any carnal pleasure.").getBytes (BOM.utf8));

		// test buffer overflow logic
		byte[] bytes;
		bytes = new byte[90000];
		for (int i = 0; i < bytes.length; ++i)
			bytes[i] = (byte)i;
		testEncoding (bytes);
	}
}
