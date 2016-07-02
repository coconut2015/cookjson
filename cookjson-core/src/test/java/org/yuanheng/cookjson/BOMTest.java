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
import java.nio.charset.Charset;
import java.util.HashMap;

import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author	Heng Yuan
 */
public class BOMTest
{
	private void testFile (Charset charset) throws IOException
	{
		File file = new File ("../tests/data/types.json".replace ('/', File.separatorChar));

		CookJsonProvider provider = new CookJsonProvider ();
		ByteArrayOutputStream bos = new ByteArrayOutputStream ();
		JsonParser p = TextJsonConfigHandler.getJsonParser (new FileInputStream (file));
		JsonGenerator g = provider.createGeneratorFactory (new HashMap<String, Object> ()).createGenerator (bos, charset);
		Utils.convert (p, g);
		p.close ();
		g.close ();

		StringWriter sw = new StringWriter ();
		p = provider.createParser (new ByteArrayInputStream (bos.toByteArray ()));
		g = provider.createGenerator (sw);
		Utils.convert (p, g);
		p.close ();
		g.close ();

		Assert.assertEquals (Utils.getString (file), sw.toString ());
	}

	@Test
	public void test () throws IOException
	{
		testFile (BOM.utf8);
		testFile (BOM.utf16le);
		testFile (BOM.utf16be);
		testFile (BOM.utf32le);
		testFile (BOM.utf32be);
	}
}
