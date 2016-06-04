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
import java.io.StringReader;

import org.yuanheng.cookcc.CookCCOption;
import org.yuanheng.cookcc.Lex;

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
@CookCCOption (unicode = true)
public class QuoteString extends QuoteStringParser
{
	private boolean m_quote;
	private final StringBuffer m_buffer;

	private QuoteString (boolean quote, int size)
	{
		m_quote = quote;
		m_buffer = new StringBuffer (size);
		if (quote)
			m_buffer.append ('"');
	}

	@Lex (pattern="[\\x20-\\x21\\x23-\\x5b\\x5d-\\uffff]+")
	void scanText ()
	{
		m_buffer.append (yyText ());
	}

	@Lex (pattern=".|\\n")
	void scanSpecialChar ()
	{
		char c = yyText ().charAt (0);
		switch (c)
		{
			case '\\':
				m_buffer.append ("\\\\");
				break;
			case '"':
				m_buffer.append ("\\\"");
				break;
			case '\r':
				m_buffer.append ("\\\r");
				break;
			case '\n':
				m_buffer.append ("\\\n");
				break;
			case '\f':
				m_buffer.append ("\\\f");
				break;
			case '\b':
				m_buffer.append ("\\\b");
				break;
			case '\t':
				m_buffer.append ("\\\t");
				break;
			default:
			{
				m_buffer.append ("\\u");
				String str = "000" + Integer.toHexString (c);
				m_buffer.append (str.substring (str.length () - 4));
			}
		}
	}

	@Lex (pattern="<<EOF>>")
	int scanEof ()
	{
		return 0;
	}

	public String getString ()
	{
		if (m_quote)
			m_buffer.append ('"');
		return m_buffer.toString ();
	}

	/**
	 * Put a string in double quote and escape the string if necessary.
	 *
	 * @param	str
	 * 			the string to be quoted and escaped.
	 * @return	the generated string.
	 */
	public static String quote (String str)
	{
		QuoteString qs = new QuoteString (true, str.length () + 10);
		qs.setInput (new StringReader (str));
		try
		{
			qs.yyLex ();
		}
		catch (IOException ex)
		{
		}
		return qs.getString ();
	}

	/**
	 * Escape the string according to JSON specification.  It does not
	 * have double quotes around it.
	 *
	 * @param	str
	 * 			the string to be escaped.
	 * @return	the generated string.
	 */
	public static String escape (String str)
	{
		QuoteString qs = new QuoteString (false, str.length () + 10);
		qs.setInput (new StringReader (str));
		try
		{
			qs.yyLex ();
		}
		catch (IOException ex)
		{
		}
		return qs.getString ();
	}
}
