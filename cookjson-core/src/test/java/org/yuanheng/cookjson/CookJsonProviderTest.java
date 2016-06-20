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

import javax.json.JsonReader;
import javax.json.JsonWriter;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author	Heng Yuan
 */
public class CookJsonProviderTest
{
	@Test
	public void test1 () throws IOException
	{
		CookJsonProvider provider = new CookJsonProvider ();

		File srcFile = new File ("../tests/data/types.json".replace ('/', File.separatorChar));
		JsonParser p = provider.createParser (new FileInputStream (srcFile));
		StringWriter sw = new StringWriter ();
		JsonGenerator g = provider.createGenerator (sw);
		Utils.convert (p, g);
		p.close ();
		g.close ();

		Assert.assertEquals (Utils.getString (srcFile), sw.toString ());
	}

	@Test
	public void test2 () throws IOException
	{
		CookJsonProvider provider = new CookJsonProvider ();

		File srcFile = new File ("../tests/data/types.json".replace ('/', File.separatorChar));

		JsonReader r = provider.createReader (new InputStreamReader (new FileInputStream (srcFile), BOM.utf8));
		ByteArrayOutputStream bos = new ByteArrayOutputStream ();
		JsonWriter w = provider.createWriter (bos);

		w.write (r.read ());
		r.close ();
		w.close ();

		Assert.assertEquals (Utils.getString (srcFile).length (), new String (bos.toByteArray (), BOM.utf8).length ());
	}
}
