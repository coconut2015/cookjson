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

/**
 * @author	Heng Yuan
 */
public class CookJsonBigDecimal implements JsonNumber
{
	private final BigDecimal m_value;

	public CookJsonBigDecimal (BigDecimal value)
	{
		m_value = value;
	}

	public CookJsonBigDecimal (BigInteger value)
	{
		m_value = new BigDecimal (value);
	}

	@Override
	public ValueType getValueType ()
	{
		return ValueType.NUMBER;
	}

	@Override
	public boolean isIntegral ()
	{
		return m_value.scale () == 0;
	}

	@Override
	public int intValue ()
	{
		return m_value.intValue ();
	}

	@Override
	public int intValueExact ()
	{
		return m_value.intValueExact ();
	}

	@Override
	public long longValue ()
	{
		return m_value.longValue ();
	}

	@Override
	public long longValueExact ()
	{
		return m_value.longValueExact ();
	}

	@Override
	public BigInteger bigIntegerValue ()
	{
		return m_value.toBigInteger ();
	}

	@Override
	public BigInteger bigIntegerValueExact ()
	{
		return m_value.toBigIntegerExact ();
	}

	@Override
	public double doubleValue ()
	{
		return m_value.doubleValue ();
	}

	@Override
	public BigDecimal bigDecimalValue ()
	{
		return m_value;
	}

	@Override
	public int hashCode ()
	{
		return bigDecimalValue ().hashCode ();
	}

	@Override
	public String toString ()
	{
		return m_value.toString ();
	}
}
