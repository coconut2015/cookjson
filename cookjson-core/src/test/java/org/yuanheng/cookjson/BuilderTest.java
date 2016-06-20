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

import java.io.File;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;

import javax.json.JsonArray;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonWriter;

import org.junit.Assert;
import org.junit.Test;
import org.yuanheng.cookjson.value.CookJsonNumber;

/**
 * @author	Heng Yuan
 */
public class BuilderTest
{
	@Test
	public void test1 ()
	{
		CookJsonProvider provider = new CookJsonProvider ();
		JsonArray model = provider.createArrayBuilder ()
			.add (provider.createObjectBuilder ()
				.add ("int", 123)
				.add ("long", 12345678901234L)
				.add ("bigint", new BigInteger ("1234567890123412345678901234"))
				.add ("decimal", new BigDecimal ("12345.5"))
				.add ("string", "asdf")
				.addNull ("null")
				.add ("true", true)
				.add ("false", false)
				.build ())
			.add (true)
			.add (false)
			.addNull ()
			.add (provider.createArrayBuilder ()
				.add (1234)
				.add (new BigDecimal ("1234.5"))
				.add (true)
				.add (provider.createObjectBuilder ())
				.add (1)
				.build ())
			.build ();
		StringWriter sw = new StringWriter ();
		JsonWriter writer = provider.createWriter (sw);
		writer.write (model);
		writer.close ();

		Assert.assertEquals (Utils.getString (new File ("../tests/data/types.json".replace ('/', File.separatorChar))).length (), sw.toString ().length ());
	}

	@Test
	public void testObject ()
	{
		HashMap<String, Object> config = new HashMap<String, Object> ();
		CookJsonProvider provider = new CookJsonProvider ();
		JsonBuilderFactory f = provider.createBuilderFactory (config);

		JsonObject model = f.createObjectBuilder ()
			.add ("object", f.createObjectBuilder ())
			.add ("array", f.createArrayBuilder ())
			.add ("double", 1234.5)
			.add ("number", new CookJsonNumber (1234))
			.build ();

		StringWriter sw = new StringWriter ();
		JsonWriter writer = provider.createWriter (sw);
		writer.write (model);
		writer.close ();

		Assert.assertEquals ("{\"object\":{},\"array\":[],\"double\":1234.5,\"number\":1234}".length (), sw.toString ().length ());
	}

	@Test
	public void testArray ()
	{
		HashMap<String, Object> config = new HashMap<String, Object> ();
		CookJsonProvider provider = new CookJsonProvider ();
		JsonBuilderFactory f = provider.createBuilderFactory (config);

		JsonArray model = f.createArrayBuilder ()
			.add (12345678901234L)
			.add (1234.5)
			.add ("quick brown fox")
			.add (new BigInteger ("123456789012345678901234567890"))
			.add (f.createArrayBuilder ())
			.build ();

		StringWriter sw = new StringWriter ();
		JsonWriter writer = provider.createWriter (sw);
		writer.write (model);
		writer.close ();

		Assert.assertEquals ("[12345678901234,1234.5,\"quick brown fox\",123456789012345678901234567890,[]]", sw.toString ());
	}
}
