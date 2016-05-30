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
package org.yuanheng.cookjson.value;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.json.JsonNumber;

/**
 * @author	Heng Yuan
 */
public class CookJsonNumber implements JsonNumber
{
	private Number m_value;

	public CookJsonNumber (Number value)
	{
		m_value = value;
	}

	@Override
	public ValueType getValueType ()
	{
		return ValueType.NUMBER;
	}

	@Override
	public boolean isIntegral ()
	{
		if (m_value instanceof Integer ||
			m_value instanceof Long)
			return true;
		return false;
	}

	@Override
	public int intValue ()
	{
		return m_value.intValue ();
	}

	@Override
	public int intValueExact ()
	{
		if (m_value instanceof Integer)
			return m_value.intValue ();
		else if (m_value instanceof Long)
		{
			long val = m_value.longValue ();
			if (val > (long)Integer.MAX_VALUE ||
				val < (long)Integer.MIN_VALUE)
				throw new ArithmeticException ();
			return (int) val;
		}
		return bigDecimalValue ().intValueExact ();
	}

	@Override
	public long longValue ()
	{
		return m_value.longValue ();
	}

	@Override
	public long longValueExact ()
	{
		if (m_value instanceof Integer ||
			m_value instanceof Long)
			return m_value.longValue ();
		return bigDecimalValue ().longValueExact ();
	}

	@Override
	public BigInteger bigIntegerValue ()
	{
		return bigDecimalValue ().toBigInteger ();
	}

	@Override
	public BigInteger bigIntegerValueExact ()
	{
		return bigDecimalValue ().toBigIntegerExact ();
	}

	@Override
	public double doubleValue ()
	{
		return m_value.doubleValue ();
	}

	@Override
	public BigDecimal bigDecimalValue ()
	{
		if (m_value instanceof Integer)
		{
			return new BigDecimal (m_value.intValue ());
		}
		if (m_value instanceof Long)
		{
			return new BigDecimal (m_value.longValue ());
		}
		if (m_value instanceof Float)
		{
			return new BigDecimal (m_value.floatValue ());
		}
		return new BigDecimal (m_value.doubleValue ());
	}

	@Override
	public int hashCode ()
	{
		return m_value.hashCode ();
	}

	@Override
	public String toString ()
	{
		return m_value.toString ();
	}
}
