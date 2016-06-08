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

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * This class put a string in a double quote, escape characters as necessary.
 * <p>
 * See rfc4627 on quoted string:
 * <pre>
 *         string = quotation-mark *char quotation-mark
 *
 *         char = unescaped /
 *                escape (
 *                    %x22 /          ; "    quotation mark  U+0022
 *                    %x5C /          ; \    reverse solidus U+005C
 *                    %x2F /          ; /    solidus         U+002F
 *                    %x62 /          ; b    backspace       U+0008
 *                    %x66 /          ; f    form feed       U+000C
 *                    %x6E /          ; n    line feed       U+000A
 *                    %x72 /          ; r    carriage return U+000D
 *                    %x74 /          ; t    tab             U+0009
 *                    %x75 4HEXDIG )  ; uXXXX                U+XXXX
 *
 *         escape = %x5C              ; \
 *
 *         quotation-mark = %x22      ; "
 *
 *         unescaped = %x20-21 / %x23-5B / %x5D-10FFFF
 * </pre>
 *
 * @see <a href="http://tools.ietf.org/rfc/rfc4627.txt">rfc4627</a>
 * @author	Heng Yuan
 */
public class Quote
{
	final static char[] hex = "0123456789abcdef".toCharArray ();

	/**
	 * Quote a string
	 * @param	out
	 *			writer
	 * @param	str
	 *			string to be quoted and written
	 * @throws	IOException
	 *			in case of I/O error.
	 */
	public static void quote (Writer out, String str) throws IOException
	{
		int strLength = str.length ();
		char[] esc = null;
		int start = 0;
		int i;
		out.write ('"');
		for (i = 0; i < strLength; ++i)
		{
			char ch = str.charAt (i);
			// most frequent chars are lower case letters.
			if (ch > '\\')
				continue;
			// upper case letters and numbers are pretty frequent too
			else if (ch > '"')
			{
				if (ch < '\\')
					continue;
				// handle '\\' case
				int len = i - start + 1;
				out.write (str, start, len);
				start = i;
			}
			else if (ch >= ' ')
			{
				if (ch < '"')
					continue;
				// handle '"' case
				int len = i - start;
				if (len > 0)
				{
					out.write (str, start, len);
				}
				out.write ('\\');
				start = i;
			}
			else
			{
				int len = i - start;
				if (len > 0)
				{
					out.write (str, start, len);
				}
				start = i + 1;
				// handle the escape sequences
				if (esc == null)
				{
					esc = new char[6];
					esc[0] = '\\';
					esc[2] = '0';
					esc[3] = '0';
				}
				switch (ch)
				{
					case '\b':	// 0x08
					{
						esc[1] = 'b';
						out.write (esc, 0, 2);
						break;
					}
					case '\t':	// 0x09
					{
						esc[1] = 't';
						out.write (esc, 0, 2);
						break;
					}
					case '\n':	// 0x0a
					{
						esc[1] = 'n';
						out.write (esc, 0, 2);
						break;
					}
					case '\r':	// 0x0d
					{
						esc[1] = 'r';
						out.write (esc, 0, 2);
						break;
					}
					case '\f':	// 0x0c
					{
						esc[1] = 'f';
						out.write (esc, 0, 2);
						break;
					}
					default:
					{
						esc[1] = 'u';
						esc[4] = hex[(ch >> 4) & 0x0f];
						esc[5] = hex[ch & 0x0f];
						out.write (esc, 0, 6);
						break;
					}
				}
			}
		}
		int len = i - start;
		if (len > 0)
		{
			out.write (str, start, len);
		}
		out.write ('"');
	}

	public static String quote (String str)
	{
		try
		{
			StringWriter sw = new StringWriter (str.length () + 10);
			quote (sw, str);
			sw.close ();
			return sw.toString ();
		}
		catch (IOException ex)
		{
			ex.printStackTrace ();
			return null;
		}
	}
}
