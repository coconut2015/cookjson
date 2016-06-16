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

import javax.json.JsonException;

import org.apache.commons.codec.binary.Base64;

/**
 * @author	Heng Yuan
 */
public class BinaryUtils
{
	public static String encodeBase64 (byte[] bytes)
	{
		return Base64.encodeBase64String (bytes);
	}

	public static byte[] decodeBase64 (String str)
	{
		return Base64.decodeBase64 (str);
	}

	public static String encodeHex (byte[] bytes)
	{
		char[] chars = new char[bytes.length * 2];
		char[] hex = Quote.hex;
		int pos = 0;
		for (int i = 0; i < bytes.length; ++i)
		{
			byte b = bytes[i];
			chars[pos++] = hex[(b & 0xff) >> 4];
			chars[pos++] = hex[(b & 0x0f)];
		}
		return new String (chars);
	}

	private static int getInt (char c)
	{
		if (c >= '0' && c <= '9')
			return c - '0';
		if (c >= 'a' && c <= 'f')
			return 10 + c - 'a';
		if (c >= 'A' && c <= 'F')
			return 10 + c - 'A';
		throw new JsonException ("Invalid hexadecimal string.");
	}

	public static byte[] decodeHex (String str)
	{
		char[] chars = str.toCharArray ();
		if ((chars.length & 1) != 0)
			throw new JsonException ("Invalid hexadecimal string.");

		byte[] bytes = new byte[chars.length / 2];

		int pos = 0;
		for (int i = 0; i < bytes.length; ++i)
		{
			int p1 = getInt (chars[pos++]);
			int p2 = getInt (chars[pos++]);
			bytes[i] = (byte) (p1 << 4 | p2);
		}
		return bytes;
	}
}
