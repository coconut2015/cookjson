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

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.json.JsonNumber;
import javax.json.JsonValue.ValueType;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author	Heng Yuan
 */
public class NumberTest
{
	@Test
	public void testInt ()
	{
		JsonNumber v = new CookJsonInt (1234);

		Assert.assertEquals (ValueType.NUMBER, v.getValueType ());

		Assert.assertEquals (true, v.isIntegral ());
		Assert.assertEquals (1234, v.intValue ());
		Assert.assertEquals (1234, v.intValueExact ());
		Assert.assertEquals (1234, v.longValue ());
		Assert.assertEquals (1234, v.longValueExact ());
		Assert.assertEquals (new BigInteger ("1234"), v.bigIntegerValue ());
		Assert.assertEquals (new BigInteger ("1234"), v.bigIntegerValueExact ());
		Assert.assertEquals (new BigDecimal (1234), v.bigDecimalValue ());
		Assert.assertEquals (1234, v.doubleValue (), 0);
		Assert.assertEquals (new BigDecimal (1234).hashCode (), v.hashCode ());
		Assert.assertEquals ("1234", v.toString ());
	}

	@Test
	public void testLong ()
	{
		long d = 1234;
		JsonNumber v = new CookJsonLong (d);

		Assert.assertEquals (ValueType.NUMBER, v.getValueType ());

		Assert.assertEquals (true, v.isIntegral ());
		Assert.assertEquals (1234, v.intValue ());
		Assert.assertEquals (1234, v.intValueExact ());
		Assert.assertEquals (d, v.longValue ());
		Assert.assertEquals (d, v.longValueExact ());
		Assert.assertEquals (new BigInteger ("1234"), v.bigIntegerValue ());
		Assert.assertEquals (new BigInteger ("1234"), v.bigIntegerValueExact ());
		Assert.assertEquals (new BigDecimal (d), v.bigDecimalValue ());
		Assert.assertEquals (d, v.doubleValue (), 0);
		Assert.assertEquals (new BigDecimal (d).hashCode (), v.hashCode ());
		Assert.assertEquals ("1234", v.toString ());
	}

	@Test
	public void testDouble ()
	{
		double d = 1234;
		JsonNumber v = new CookJsonDouble (d);

		Assert.assertEquals (ValueType.NUMBER, v.getValueType ());

		Assert.assertEquals (false, v.isIntegral ());
		Assert.assertEquals (1234, v.intValue ());
		Assert.assertEquals (1234, v.intValueExact ());
		Assert.assertEquals (1234, v.longValue ());
		Assert.assertEquals (1234, v.longValueExact ());
		Assert.assertEquals (new BigInteger ("1234"), v.bigIntegerValue ());
		Assert.assertEquals (new BigInteger ("1234"), v.bigIntegerValueExact ());
		Assert.assertEquals (BigDecimal.valueOf (d), v.bigDecimalValue ());
		Assert.assertEquals (d, v.doubleValue (), 0);
		Assert.assertEquals (BigDecimal.valueOf (d).hashCode (), v.hashCode ());
		Assert.assertEquals (Double.toString (d), v.toString ());
	}

	@Test
	public void testBigInteger ()
	{
		BigInteger d = new BigInteger ("1234");
		JsonNumber v = new CookJsonBigDecimal (d);

		Assert.assertEquals (ValueType.NUMBER, v.getValueType ());

		Assert.assertEquals (true, v.isIntegral ());
		Assert.assertEquals (d.intValue (), v.intValue ());
		Assert.assertEquals (d.intValue (), v.intValueExact ());
		Assert.assertEquals (d.longValue (), v.longValue ());
		Assert.assertEquals (d.longValue (), v.longValueExact ());
		Assert.assertEquals (d, v.bigIntegerValue ());
		Assert.assertEquals (d, v.bigIntegerValueExact ());
		Assert.assertEquals (new BigDecimal (d), v.bigDecimalValue ());
		Assert.assertEquals (d.doubleValue (), v.doubleValue (), 0);
		Assert.assertEquals (new BigDecimal (d).hashCode (), v.hashCode ());
		Assert.assertEquals (d.toString (), v.toString ());
	}

	@Test
	public void testBigDecimal ()
	{
		BigDecimal d = new BigDecimal ("1234.1234");
		JsonNumber v = new CookJsonBigDecimal (d);

		Assert.assertEquals (ValueType.NUMBER, v.getValueType ());

		Assert.assertEquals (false, v.isIntegral ());
		Assert.assertEquals (d.intValue (), v.intValue ());
//		Assert.assertEquals (d.intValue (), v.intValueExact ());
		Assert.assertEquals (d.longValue (), v.longValue ());
//		Assert.assertEquals (d.longValue (), v.longValueExact ());
		Assert.assertEquals (d.toBigInteger (), v.bigIntegerValue ());
//		Assert.assertEquals (d.toBigInteger (), v.bigIntegerValueExact ());
		Assert.assertEquals (d, v.bigDecimalValue ());
		Assert.assertEquals (d.doubleValue (), v.doubleValue (), 0);
		Assert.assertEquals (d.hashCode (), v.hashCode ());
		Assert.assertEquals (d.toString (), v.toString ());
	}
}
