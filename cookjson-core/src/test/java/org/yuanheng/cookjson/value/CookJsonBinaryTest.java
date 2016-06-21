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

import java.util.Arrays;

import javax.json.JsonValue.ValueType;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.junit.Assert;
import org.junit.Test;
import org.yuanheng.cookjson.BOM;
import org.yuanheng.cookjson.BinaryFormat;

/**
 * @author	Heng Yuan
 */
public class CookJsonBinaryTest
{
	@Test
	public void test ()
	{
		byte[] bytes = "test".getBytes (BOM.utf8);
		CookJsonBinary v = new CookJsonBinary (bytes.clone ());
		Assert.assertEquals (Arrays.hashCode (bytes), v.hashCode ());
		Assert.assertEquals (ValueType.STRING, v.getValueType ());
		Assert.assertArrayEquals (bytes, v.getBytes ());

		String base64 = Base64.encodeBase64String (bytes);
		Assert.assertEquals (base64, v.getString ());
		Assert.assertEquals (base64, v.getChars ());
		Assert.assertEquals ('"' + base64 + '"', v.toString ());
		Assert.assertEquals (BinaryFormat.BINARY_FORMAT_BASE64, v.getBinaryFormat ());

		v.setBinaryFormat (BinaryFormat.BINARY_FORMAT_HEX);
		String hex = Hex.encodeHexString (bytes);
		Assert.assertEquals (hex, v.getString ());
		Assert.assertEquals (hex, v.getChars ());
		Assert.assertEquals ('"' + hex + '"', v.toString ());
		Assert.assertEquals (BinaryFormat.BINARY_FORMAT_HEX, v.getBinaryFormat ());
	}
}
