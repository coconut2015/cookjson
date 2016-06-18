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

/**
 * @author	Heng Yuan
 */
public class CookJsonBinary implements JsonString
{
	/** Use base64 to represent binary values. */
	public final static int BASE64 = 0;
	/** Use hexadecimal to represent binary values. */
	public final static int HEX = 1;

	private final byte[] m_bytes;
	private int m_textFormat;

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
		if (m_textFormat == HEX)
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
	 * @return	the textFormat
	 */
	public int getTextFormat ()
	{
		return m_textFormat;
	}

	/**
	 * Use BASE64 or HEX format for string.
	 *
	 * @param	textFormat
	 *			the text format to set
	 */
	public void setTextFormat (int textFormat)
	{
		m_textFormat = textFormat;
	}
}
