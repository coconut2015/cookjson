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
package org.yuanheng.cookjson.value;

import javax.json.*;
import javax.json.JsonValue.ValueType;
import javax.json.spi.JsonProvider;

import org.junit.Assert;
import org.junit.Test;
import org.yuanheng.cookjson.CookJsonProvider;

/**
 * @author	Heng Yuan
 */
public class CookJsonArrayTest
{
	@Test
	public void testDefault ()
	{
		JsonProvider provider = new CookJsonProvider ();
		JsonArrayBuilder builder = provider.createArrayBuilder ();
		JsonArray array = builder
			.add ("test")
			.add (1234)
			.add (true)
			.add (false)
			.addNull ()
			.build ();
		Assert.assertEquals ("test", array.getString (0, "asdf"));
		Assert.assertEquals (1234, array.getInt (1, 890));
		Assert.assertEquals (true, array.getBoolean (2, false));
		Assert.assertEquals (false, array.getBoolean (3, true));

		Assert.assertEquals ("test", array.getString (11, "test"));
		Assert.assertEquals (1234, array.getInt (11, 1234));
		Assert.assertEquals (true, array.getBoolean (11, true));
		Assert.assertEquals (false, array.getBoolean (11, false));
	}

	@Test
	public void testGet ()
	{
		JsonProvider provider = new CookJsonProvider ();
		JsonArrayBuilder builder = provider.createArrayBuilder ();
		JsonArray array = builder
			.add ("test")
			.add (1234)
			.add (true)
			.add (false)
			.addNull ()
			.build ();
		Assert.assertEquals ("test", array.getString (0));
		Assert.assertEquals (1234, array.getInt (1));
		Assert.assertEquals (true, array.getBoolean (2));
		Assert.assertEquals (false, array.getBoolean (3));
		Assert.assertEquals (true, array.isNull (4));
	}

	@Test
	public void testGetValue ()
	{
		JsonProvider provider = new CookJsonProvider ();
		JsonArrayBuilder builder = provider.createArrayBuilder ();
		JsonArray array = builder
			.add ("test")
			.add (1234)
			.add (true)
			.add (false)
			.addNull ()
			.add (provider.createArrayBuilder ().build ())
			.add (provider.createObjectBuilder ().build ())
			.build ();
		JsonValue v;
		v = array.getJsonString (0);
		Assert.assertEquals ("test", ((JsonString)v).getString ());
		v = array.getJsonNumber (1);
		Assert.assertEquals (1234, ((JsonNumber)v).intValue ());
		v = array.get (2);
		Assert.assertEquals (JsonValue.TRUE, v);
		v = array.get (3);
		Assert.assertEquals (JsonValue.FALSE, v);
		v = array.get (4);
		Assert.assertEquals (JsonValue.NULL, v);
		v = array.getJsonArray (5);
		Assert.assertEquals (ValueType.ARRAY, v.getValueType ());
		v = array.getJsonObject (6);
		Assert.assertEquals (ValueType.OBJECT, v.getValueType ());
	}

	@Test
	public void testGetValuesAs ()
	{
		JsonProvider provider = new CookJsonProvider ();
		JsonArrayBuilder builder = provider.createArrayBuilder ();
		JsonArray array = builder.build ();
		Assert.assertEquals (array, array.getValuesAs (JsonValue.class));
	}
}
