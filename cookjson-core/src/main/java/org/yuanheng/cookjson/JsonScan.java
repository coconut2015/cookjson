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

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;

import org.yuanheng.cookcc.CookCCOption;
import org.yuanheng.cookcc.Lex;
import org.yuanheng.cookcc.Lexs;

/**
 * @author	Heng Yuan
 */
@CookCCOption (unicode = true)
class JsonScan extends JsonScanParser implements Closeable
{
	final static int START_ARRAY = 1;
	final static int START_OBJECT = 2;
	final static int END_ARRAY = 3;
	final static int END_OBJECT = 4;
	final static int VALUE_STRING = 5;
	final static int VALUE_NUMBER = 6;
	final static int VALUE_TRUE = 7;
	final static int VALUE_FALSE = 8;
	final static int VALUE_NULL = 9;
	final static int COMMA = 10;
	final static int COLON = 11;

	/**
	 * The default StringBuffer size for holding parsed string data.
	 * <p>
	 * The right size is related to actual quoted string sizes, rather than
	 * the size of the file.
	 */
	public static int DEFAULT_BUFFER_SIZE = 8192;

	private final Reader m_reader;
	private final StringBuffer m_buffer = new StringBuffer (DEFAULT_BUFFER_SIZE);

	private boolean m_allowComments;

	private int m_line;
	private int m_column;
	private long m_offset;

	int m_savedLine;
	int m_savedColumn;
	long m_savedOffset;
	String m_value;

	public JsonScan (Reader r)
	{
		m_reader = r;
		setInput (r);
	}

	private void saveLocation ()
	{
		m_savedLine = m_line;
		m_savedColumn = m_column;
		m_savedOffset = m_offset;
	}

	private void updateLocation ()
	{
		int len = yyLength ();
		m_column += len;
		m_offset += len;
	}

	@Lex (pattern = "[\\[\\]{}:,]", state = "INITIAL")
	int scanToken ()
	{
		saveLocation ();
		updateLocation ();

		m_value = null;
		switch (yyText ().charAt (0))
		{
			case '[':
				return START_ARRAY;
			case ']':
				return END_ARRAY;
			case '{':
				return START_OBJECT;
			case '}':
				return END_OBJECT;
			case ':':
				return COLON;
			case ',':
				return COMMA;
		}
		// should not get here.
		return 0;
	}

	@Lex (pattern = "[a-zA-Z_][a-zA-Z_0-9]*", state = "INITIAL")
	int scanIdentifier () throws IOException
	{
		saveLocation ();
		updateLocation ();

		String text = yyText ();
		if ("null".equals (text))
			return VALUE_NULL;
		if ("true".equals (text))
			return VALUE_TRUE;
		if ("false".equals (text))
			return VALUE_FALSE;
		ioError ("unknown identifier: " + text);
		// should not get here
		return 0;
	}

	@Lex (pattern = "'-'?([1-9][0-9]*|0)([.][0-9]+)?([eE][+-]?[0-9]+)?", state = "INITIAL")
	int scanNumber () throws IOException
	{
		saveLocation ();
		updateLocation ();

		m_value = yyText ();
		return VALUE_NUMBER;
	}

	@Lex (pattern = "'//'.*", state = "INITIAL")
	void scanLineComment () throws IOException
	{
		updateLocation ();

		if (!m_allowComments)
			ioError ("Line comment not allowed.");
	}

	@Lex (pattern = "'/*'[^*/\\n]+", state = "INITIAL")
	void scanBlockCommentStart () throws IOException
	{
		updateLocation ();

		if (!m_allowComments)
			ioError ("block comment not allowed.");

		begin ("BLOCKCOMMENT");
	}

	@Lexs (patterns = {
		@Lex (pattern = "[*/]", state = "BLOCKCOMMENT"),
		@Lex (pattern = "[^*/\\n]+", state = "BLOCKCOMMENT")
	})
	void scanCommentText ()
	{
		updateLocation ();
	}

	@Lex (pattern = "[*]+'/'", state = "BLOCKCOMMENT")
	void scanBlockCommentEnd ()
	{
		updateLocation ();

		begin ("INITIAL");
	}

	@Lex (pattern = "[ \\t\\r]+", state = "INITIAL")
	void scanSpace ()
	{
		updateLocation ();
	}

	@Lex (pattern = "\\n", state = "INITIAL, BLOCKCOMMENT")
	void scanEOL ()
	{
		updateLocation ();
		++m_line;
		m_column = 0;
	}

	@Lex (pattern = "'\"'", state = "INITIAL")
	void scanQuote ()
	{
		saveLocation ();
		updateLocation ();

		m_buffer.setLength (0);

		begin ("QUOTE");
	}

	@Lex (pattern = "[^\\\\\"\\n]+", state = "QUOTE")
	void scanQuoteText ()
	{
		updateLocation ();

		m_buffer.append (yyText ());
	}

	@Lex (pattern = "\\n", state = "QUOTE")
	void scanQuoteEOL ()
	{
		updateLocation ();
		++m_line;
		m_column = 0;
		m_buffer.append (yyText ());
	}

	@Lex (pattern = "'\\\\'u[0-9a-zA-Z]{4}", state = "QUOTE")
	void scanQuoteUnicodeEscape ()
	{
		updateLocation ();
		String literal = yyText ().substring (2);
		char ch = (char) Integer.parseInt (literal, 16);
		m_buffer.append (ch);
	}

	@Lex (pattern = "\\\\(.|\\n)", state = "QUOTE")
	void scanEscape () throws IOException
	{
		updateLocation ();
		char ch = yyText ().charAt (1);
		switch (ch)
		{
			case 'n':
				ch = '\n';
				break;
			case 'r':
				ch = '\r';
				break;
			case 'b':
				ch = '\b';
				break;
			case 'f':
				ch = '\f';
				break;
			case 't':
				ch = '\t';
				break;
			case '\\':
			case '/':
			case '"':
				break;
			default:
				ioError ("unknown escape sequence: \\" + QuoteString.escape ("" + ch));
		}
		m_buffer.append (ch);
	}

	@Lex (pattern = "'\"'", state = "QUOTE")
	int scanEndQuote ()
	{
		updateLocation ();
		begin ("INITIAL");
		m_value = m_buffer.toString ();
		m_buffer.setLength (0);
		return VALUE_STRING;
	}

	@Lex (pattern = ".", state = "QUOTE")
	void scanQuoteText2 ()
	{
		updateLocation ();
		m_buffer.append (yyText ());
	}

	@Lex (pattern = ".", state = "INITIAL")
	void scanUnexpectedChar () throws IOException
	{
		ioError ("unexpected char: " + QuoteString.escape (yyText ()));
	}

	@Lex (pattern = "<<EOF>>", state = "INITIAL, BLOCKCOMMENT, QUOTE")
	void scanEOF () throws IOException
	{
		ioError ("EOF encountered.");
	}

	final void ioError (String msg) throws IOException
	{
		throw new IOException ("Line " + m_line + ", column " + m_column + ": " + msg);
	}

	/**
	 * Check if comments are allowed.
	 *
	 * @return	the allowComments
	 */
	public boolean isAllowComments ()
	{
		return m_allowComments;
	}

	/**
	 * Set if comments are allowed.
	 * @param	allowComments
	 *			allow comment or not.
	 */
	public void setAllowComments (boolean allowComments)
	{
		m_allowComments = allowComments;
	}

	@Override
	public void close () throws IOException
	{
		m_reader.close ();
	}
}
