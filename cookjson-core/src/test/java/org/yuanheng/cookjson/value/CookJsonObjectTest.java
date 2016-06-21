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
public class CookJsonObjectTest
{
	@Test
	public void testDefault ()
	{
		JsonProvider provider = new CookJsonProvider ();
		JsonObjectBuilder builder = provider.createObjectBuilder ();
		JsonObject obj = builder
			.add ("string", "test")
			.add ("int", 1234)
			.add ("true", true)
			.add ("false", false)
			.addNull ("null")
			.build ();
		Assert.assertEquals ("test", obj.getString ("string", "asdf"));
		Assert.assertEquals (1234, obj.getInt ("int", 890));
		Assert.assertEquals (true, obj.getBoolean ("true", false));
		Assert.assertEquals (false, obj.getBoolean ("false", true));

		Assert.assertEquals ("test", obj.getString ("no", "test"));
		Assert.assertEquals (1234, obj.getInt ("no", 1234));
		Assert.assertEquals (true, obj.getBoolean ("no", true));
		Assert.assertEquals (false, obj.getBoolean ("no", false));
	}

	@Test
	public void testGet ()
	{
		JsonProvider provider = new CookJsonProvider ();
		JsonObjectBuilder builder = provider.createObjectBuilder ();
		JsonObject obj = builder
			.add ("string", "test")
			.add ("int", 1234)
			.add ("true", true)
			.add ("false", false)
			.addNull ("null")
			.build ();
		Assert.assertEquals ("test", obj.getString ("string"));
		Assert.assertEquals (1234, obj.getInt ("int"));
		Assert.assertEquals (true, obj.getBoolean ("true"));
		Assert.assertEquals (false, obj.getBoolean ("false"));
		Assert.assertEquals (true, obj.isNull ("null"));
	}

	@Test
	public void testGetValue ()
	{
		JsonProvider provider = new CookJsonProvider ();
		JsonObjectBuilder builder = provider.createObjectBuilder ();
		JsonObject obj = builder
			.add ("string", "test")
			.add ("int", 1234)
			.add ("true", true)
			.add ("false", false)
			.addNull ("null")
			.add ("array", provider.createArrayBuilder ().build ())
			.add ("object", provider.createObjectBuilder ().build ())
			.build ();
		JsonValue v;
		v = obj.getJsonString ("string");
		Assert.assertEquals ("test", ((JsonString)v).getString ());
		v = obj.getJsonNumber ("int");
		Assert.assertEquals (1234, ((JsonNumber)v).intValue ());
		v = obj.get ("true");
		Assert.assertEquals (JsonValue.TRUE, v);
		v = obj.get ("false");
		Assert.assertEquals (JsonValue.FALSE, v);
		v = obj.get ("null");
		Assert.assertEquals (JsonValue.NULL, v);
		v = obj.getJsonArray ("array");
		Assert.assertEquals (ValueType.ARRAY, v.getValueType ());
		v = obj.getJsonObject ("object");
		Assert.assertEquals (ValueType.OBJECT, v.getValueType ());
	}
}
