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

import javax.json.JsonString;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.yuanheng.cookjson.BinaryFormat;

/**
 * @author	Heng Yuan
 */
public class CookJsonBinary implements JsonString
{
	private final byte[] m_bytes;
	private int m_binaryFormat;

	public CookJsonBinary (byte[] bytes)
	{
		m_bytes = bytes;
	}

	@Override
	public ValueType getValueType ()
	{
		return ValueType.STRING;
	}

	@Override
	public String getString ()
	{
		if (m_binaryFormat == BinaryFormat.BINARY_FORMAT_HEX)
			return Hex.encodeHexString (m_bytes);
		return Base64.encodeBase64String (m_bytes);
	}

	@Override
	public CharSequence getChars ()
	{
		return getString ();
	}

	public byte[] getBytes ()
	{
		return m_bytes;
	}

	/**
	 * Gets the binary format for storing byte[].
	 * <p>
	 * It is one of {@link #BINARY_FORMAT_BASE64} and
	 * {@link #BINARY_FORMAT_HEX}.
	 *
	 * @return	the binaryFormat
	 */
	public int getBinaryFormat ()
	{
		return m_binaryFormat;
	}

	/**
	 * Sets the binary format for storing byte[].  The default is Base64.
	 * <p>
	 * It is one of {@link #BINARY_FORMAT_BASE64} and
	 * {@link #BINARY_FORMAT_HEX}.
	 *
	 * @param	binaryFormat
	 *			the binary format
	 */
	public void setBinaryFormat (int binaryFormat)
	{
		m_binaryFormat = binaryFormat;
	}
}
