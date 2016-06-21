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
public class CookJsonDouble implements JsonNumber
{
	private final double m_value;
	private BigDecimal m_decimal;

	public CookJsonDouble (double value)
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
		return false;
	}

	@Override
	public int intValue ()
	{
		return (int) m_value;
	}

	@Override
	public int intValueExact ()
	{
		return bigDecimalValue ().intValueExact ();
	}

	@Override
	public long longValue ()
	{
		return (long) m_value;
	}

	@Override
	public long longValueExact ()
	{
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
		return m_value;
	}

	@Override
	public BigDecimal bigDecimalValue ()
	{
		if (m_decimal == null)
			m_decimal = BigDecimal.valueOf (m_value);
		return m_decimal;
	}

	@Override
	public int hashCode ()
	{
		return bigDecimalValue ().hashCode ();
	}

	@Override
	public String toString ()
	{
		return Double.toString (m_value);
	}
}
