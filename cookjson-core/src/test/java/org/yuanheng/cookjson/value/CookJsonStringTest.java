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

import javax.json.JsonString;
import javax.json.JsonValue.ValueType;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author	Heng Yuan
 */
public class CookJsonStringTest
{
	@Test
	public void test ()
	{
		JsonString v = new CookJsonString ("test");
		Assert.assertEquals ("test", v.getString ());
		Assert.assertEquals ("test", v.getChars ());
		Assert.assertEquals ("\"test\"", v.toString ());
		Assert.assertEquals ("test".hashCode (), v.hashCode ());
		Assert.assertEquals (ValueType.STRING, v.getValueType ());
	}
}
