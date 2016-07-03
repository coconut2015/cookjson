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
package org.yuanheng.cookjson;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import javax.json.JsonValue;
import javax.json.stream.JsonLocation;
import javax.json.stream.JsonParsingException;

import org.yuanheng.cookjson.value.CookJsonBigDecimal;
import org.yuanheng.cookjson.value.CookJsonString;

/**
 * This JsonParser adds the ability to allow parse JavaScript line and
 * block comments.
 *
 * @author	Heng Yuan
 */
public class TextJsonParser implements CookJsonParser
{
	private final static int START = 1;
	private final static int VALUE = 2;
	private final static int FIELD = 3;

	private final static int READ_SIZE = 8192;

	private boolean m_allowComments;

	private final Reader m_reader;
	/** append buffer for storing output string */
	private char[] m_appendBuf = new char[8193];
	/** position tracking for append buffer */
	private int m_appendPos;

	private long m_line;
	private long m_offset;
	private long m_column;
	private long savedLine;
	private long savedColumn;
	private long savedOffset;

	private final char[] m_readBuf;
	private int m_readPos = 0;
	private int m_readMax = 0;

	private final ArrayList<Boolean> m_states = new ArrayList<Boolean> ();
	private int m_state = ParserState.INITIAL;
	private int m_lastToken;
	private boolean m_int;

	private Event m_event;
	/**
	 * If this flag is true, the string data is located in the input buffer rather than
	 * m_appendBuf.
	 */
	private boolean m_simple;
	private int m_start;
	private int m_len;

	public TextJsonParser (Reader r)
	{
		this (r, READ_SIZE + 1);
	}

	TextJsonParser (Reader r, int bufferSize)
	{
		m_readBuf = new char[bufferSize];
		m_reader = r;
		m_line = 1;
		m_column = 1;
	}

	private void saveLocation ()
	{
		savedLine = m_line;
		savedOffset = m_offset;
		savedColumn = m_column;
	}

	private void append (char ch)
	{
		if (m_appendPos >= m_appendBuf.length)
		{
			int len = m_appendBuf.length;
			// we need to expand the buffer by 50%
			char[] newBuffer = new char[len + len / 2];
			System.arraycopy (m_appendBuf, 0, newBuffer, 0, len);
			m_appendBuf = newBuffer;
		}
		m_appendBuf[m_appendPos++] = ch;
	}

	private IllegalStateException stateError (String function)
	{
		return new IllegalStateException (function + " cannot be called at the current state: " + m_event + ".");
	}

	private JsonParsingException ioError (String msg)
	{
		JsonLocationImpl location = new JsonLocationImpl ();
		location.m_lineNumber = m_line;
		// -1 to back track the last read character.
		location.m_columnNumber = m_column -1;
		location.m_streamOffset = m_offset - 1;
		return new JsonParsingException ("Parsing error at " + location.toString () + ": " + msg, location);
	}

	private JsonParsingException eofError ()
	{
		JsonLocation location = getCurrentLocation ();
		return new JsonParsingException ("Parsing error at " + location.toString () + ": unexpected eof.", location);
	}

	private JsonParsingException unexpected (char ch)
	{
		String charStr;
		switch (ch)
		{
			case '\b':	// 0x08
				charStr = "\\b";
				break;
			case '\t':	// 0x09
				charStr = "\\t";
				break;
			case '\n':	// 0x0a
				charStr = "\\n";
				break;
			case '\r':	// 0x0d
				charStr = "\\r";
				break;
			case '\f':	// 0x0c
				charStr = "\\f";
				break;
			case '\\':
				charStr = "\\\\";
				break;
			case '\'':
				charStr = "\\'";
				break;
			default:
			{
				if (ch < ' ' || ch > 127)
				{
					String hex = Integer.toHexString (ch);
					charStr = "\\u" + ("0000".substring (hex.length ())) + hex;
				}
				else
				{
					charStr = Character.toString (ch);
				}
				break;
			}
		}
		return ioError ("unexpected character '" + charStr + "'");
	}

	private char read () throws IOException
	{
		if (m_readPos >= m_readMax)
			fill ();
		++m_offset;
		++m_column;
		return m_readBuf[m_readPos++];
	}

	private void unread ()
	{
		--m_readPos;
		--m_offset;
		--m_column;
	}

	private void fill () throws IOException
	{
		final char[] readBuf = m_readBuf;
		m_readPos = 0;
		m_readMax = m_reader.read (readBuf, 0, readBuf.length - 1);
		if (m_readMax <= 0)
			throw eofError ();
		readBuf[m_readMax] = 0;	// mark the end of buffer
	}

	private void readLineComment () throws IOException
	{
		final char[] readBuf = m_readBuf;

		int readPos = m_readPos;

		for (;;)
		{
			char ch = readBuf[readPos++];

			if (ch == '\n')
			{
				m_offset += readPos - m_readPos;
				m_column = 1;
				++m_line;
				m_readPos = readPos;
				return;
			}
			else if (ch == 0)
			{
				if (readPos <= m_readMax)
				{
					int l = readPos - m_readPos;
					m_offset += l;
					m_column += l;
					throw unexpected (ch);
				}
				int l = readPos - m_readPos - 1;
				m_offset += l;
				m_column += l;
				fill ();
				readPos = 0;
			}
		}
	}

	private void readBlockComment () throws IOException
	{
		final char[] readBuf = m_readBuf;

		int readPos = m_readPos;
		int len = 0;
		for (;;)
		{
			char ch = readBuf[readPos++];
			++len;

			// switch is useful for scanning characters that are
			// mostly handled as default.
			switch (ch)
			{
				case '\n':
				{
					m_offset += len;
					m_column = 1;
					++m_line;
					len = 0;
					break;
				}
				case '*':
				{
					char nextChar;
					if (readPos < m_readMax)
					{
						nextChar = readBuf[readPos];
					}
					else
					{
						fill ();
						readPos = 0;
						nextChar = readBuf[readPos];
					}
					if (nextChar == '/')
					{
						m_offset += len + 1;
						m_column += len + 1;
						m_readPos = readPos + 1;
						return;
					}
					break;
				}
				case 0:
				{
					if (readPos <= m_readMax)
					{
						int l = readPos - m_readPos;
						m_offset += l;
						m_column += l;
						throw unexpected (ch);
					}
					int l = readPos - m_readPos - 1;
					m_offset += l;
					m_column += l;
					fill ();
					readPos = 0;
					break;
				}
			}
		}
	}

	private void readComment () throws IOException
	{
		char ch;
		ch = read ();
		if (ch == '/')
			readLineComment ();
		else if (ch == '*')
			readBlockComment ();
		else
		{
			--m_offset;
			--m_column;
			throw unexpected ('/');
		}
	}

	/**
	 * We do not want to add cost for handling comments.  So we embed the
	 * comment handling in unexpected character handling.
	 *
	 * @param	ch
	 *			the unexpected character just read
	 * @throws	IOException
	 * 			in case of error.
	 */
	private void scanUnexpected (char ch) throws IOException
	{
		if (!m_allowComments || ch != '/')
			throw unexpected (ch);
		readComment ();
	}

	private void expect (String str) throws IOException
	{
		final int len = str.length ();
		if ((m_readPos + len) >= m_readMax)
		{
			// we do the slow matching
			for (int i = 0; i < len; ++i)
			{
				char ch = read ();
				if (ch != str.charAt (i))
					throw ioError ("expecting '" + ch + "'");
			}
		}
		else
		{
			final char[] readBuf = m_readBuf;
			int readPos = m_readPos;
			for (int i = 0; i < len; ++i)
			{
				char ch = readBuf[readPos++];
				if (ch != str.charAt (i))
					throw ioError ("expecting '" + ch + "'");
			}
			m_readPos = readPos;
			m_offset += len;
			m_column += len;
		}
	}

	private void readNegative () throws IOException
	{
		m_start = m_readPos - 1;

		// always prime the buffer with '-'
		m_appendBuf[0] = '-';
		m_appendPos = 1;

		char ch = m_readBuf[m_readPos++];
		++m_offset;
		++m_column;
		if (ch == 0)
		{
			if (m_readPos <= m_readMax)
			{
				throw unexpected (ch);
			}
			fill ();
			ch = m_readBuf[0];
			m_readPos = 1;
			if (!(ch >= '0' && ch <= '9'))
			{
				throw unexpected (ch);
			}
			// use the slow approach the parse the number
			readNumber (ch);
			return;
		}

		if (!(ch >= '0' && ch <= '9'))
		{
			throw unexpected (ch);
		}

		readFastNumber (ch);
	}

	private void readFastNumber (char firstChar) throws IOException
	{
		m_simple = true;
		m_int = true;

		final char[] readBuf = m_readBuf;
		int readPos = m_readPos;
		char ch;

		if (firstChar == '0')
		{
			ch = readBuf[readPos++];
			if (ch == '.')
			{
			}
			else if (ch == 0)
			{
				// use slow approach
				readNumber (firstChar);
				return;
			}
			else
			{
				if (m_appendPos > 0)	// buffer initiated with '-'
				{
					++m_offset;
					++m_column;
					throw unexpected (ch);
				}
				m_len = 1;
				return;
			}
		}
		else
		{
			for (;;)
			{
				ch = readBuf[readPos++];
				if (ch >= '0')
				{
					if (ch <= '9')
						continue;
				}
				else if (ch == 0)
				{
					readNumber (firstChar);
					return;
				}
				break;
			}
		}

		if (ch == '.')
		{
			m_int = false;
			// read fraction
			ch = readBuf[readPos++];
			if (ch >= '0' && ch <= '9')
			{
				for (;;)
				{
					ch = readBuf[readPos++];
					if (ch >= '0')
					{
						if (ch <= '9')
							continue;
					}
					else if (ch == 0)
					{
						readNumber (firstChar);
						return;
					}
					break;
				}
			}
			else if (ch == 0)
			{
				readNumber (firstChar);
				return;
			}
			else
			{
				int l = readPos - m_readPos;
				m_offset += l;
				m_column += l;
				throw unexpected (ch);
			}
		}

		if (ch == 'E' || ch == 'e')
		{
			m_int = false;
			ch = readBuf[readPos++];
			if (ch == '+' || ch == '-')
				ch = readBuf[readPos++];

			if (ch >= '0' && ch <= '9')
			{
				// intentionally empty
			}
			else if (ch == 0)
			{
				readNumber (firstChar);
				return;
			}
			else
			{
				int l = readPos - m_readPos;
				m_offset += l;
				m_column += l;
				throw unexpected (ch);
			}

			for (;;)
			{
				ch = readBuf[readPos++];
				if (ch >= '0')
				{
					if (ch <= '9')
						continue;
					break;
				}
				else if (ch == 0)
				{
					if (readPos <= m_readMax)
					{
						int l = readPos - m_readPos;
						m_offset += l;
						m_column += l;
						throw unexpected (ch);
					}
					readNumber (firstChar);
					return;
				}
				break;
			}
		}

		int l = readPos - m_readPos - 1;
		m_offset += l;
		m_column += l;
		m_readPos += l;
		m_len = m_readPos - m_start;
	}

	private void readNumber (char firstChar) throws IOException
	{
		m_simple = false;
		m_int = true;

		append (firstChar);

		char ch;

		if (firstChar == '0')
		{
			ch = read ();
			if (ch == '.')
			{
			}
			else
			{
				if (m_appendBuf[0] == '-')
				{
					throw unexpected (ch);
				}
				unread ();
				return;
			}
		}
		else
		{
			for (;;)
			{
				ch = read ();
				if (ch >= '0')
				{
					if (ch <= '9')
					{
						append (ch);
						continue;
					}
				}
				break;
			}
		}

		if (ch == '.')
		{
			append (ch);
			m_int = false;
			// read fraction
			ch = read ();
			if (ch >= '0' && ch <= '9')
			{
				append (ch);
				for (;;)
				{
					ch = read ();
					if (ch >= '0')
					{
						if (ch <= '9')
						{
							append (ch);
							continue;
						}
					}
					break;
				}
			}
			else
			{
				throw unexpected (ch);
			}
		}

		if (ch == 'E' || ch == 'e')
		{
			append (ch);
			m_int = false;
			ch = read ();
			if (ch == '+' || ch == '-')
			{
				append (ch);
				ch = read ();
			}

			if (ch >= '0' && ch <= '9')
			{
				append (ch);
			}
			else
			{
				throw unexpected (ch);
			}

			for (;;)
			{
				ch = read ();
				if (ch >= '0' && ch <= '9')
				{
					append (ch);
					continue;
				}
				break;
			}
		}

		unread ();
	}

	private void readEscape () throws IOException
	{
		char ch = read ();
		switch (ch)
		{
			case 'b':
				append ('\b');
				break;
			case 'f':
				append ('\f');
				break;
			case 'n':
				append ('\n');
				break;
			case 'r':
				append ('\r');
				break;
			case 't':
				append ('\t');
				break;
			case '\\':
			case '/':
			case '"':
				append (ch);
				break;
			case 'u':
			{
				int val = 0;
				for (int i = 0; i < 4; ++i)
				{
					ch = read ();
					if (ch >= '0')
					{
						if (ch <= '9')
						{
							int hex = ch - '0';
							val = (val << 4) | hex;
							continue;
						}
						else if (ch >= 'A')
						{
							if (ch <= 'F')
							{
								int hex = ch - 'A' + 10;
								val = (val << 4) | hex;
								continue;
							}
							else if (ch >= 'a')
							{
								if (ch <= 'f')
								{
									int hex = ch - 'a' + 10;
									val = (val << 4) | hex;
									continue;
								}
							}
						}
					}
					throw unexpected (ch);
				}
				append ((char) val);
				break;
			}
			default:
				throw ioError ("unknown escape sequence '\\" + ch + "'");
		}
	}

	/**
	 * Check if we have a case where we do not need to refresh the buffer, and no escape sequences etc
	 * were encountered.  In this case, we do not need to copy the data to m_appendBuf and thus saves
	 * quite a bit performance.
	 */
	private void readString () throws IOException
	{
		final char[] readBuf = m_readBuf;
		int readPos = m_readPos;
		m_simple = true;
		m_start = readPos;

		for (;;)
		{
			char ch = readBuf[readPos++];
			// JSON does not allow 0x00 - 0x1f in string.
			// And '"' and '\\' must be escaped.
			if (ch > '\\')
				continue;
			else if (ch > '"')
			{
				if (ch < '\\')
					continue;
				else
				{
					// Encountered escape sequence.  We have to deal with the slower way.
					readComplexString (readPos - 1);
					return;
				}
			}
			else if (ch >= ' ')
			{
				if (ch < '"')
				{
					continue;
				}
				else
				{
					// handle '"' case, which closes this scan
					int l = readPos - m_readPos;
					m_offset += l;
					m_column += l;
					m_readPos = readPos;
					m_len = l - 1;
					return;
				}
			}
			else if (ch == 0)
			{
				// Encountered end of buffer.
				readComplexString (readPos - 1);
				return;
			}
			else
			{
				int l = readPos - m_readPos;
				m_offset += l;
				m_column += l;
				throw unexpected (ch);
			}
		}
	}

	private void readComplexString (int readPos) throws IOException
	{
		m_simple = false;
		final char[] readBuf = m_readBuf;
		int len = readPos - m_start;
		// first, copy the string
		if (m_appendBuf.length < len)
		{
			int appendBufLen = m_appendBuf.length;
			while (appendBufLen < len)
			{
				appendBufLen += appendBufLen;
			}
			m_appendBuf = new char[appendBufLen];
		}
		System.arraycopy (readBuf, m_start, m_appendBuf, 0, len);
		m_appendPos = len;

		for (;;)
		{
			char ch = readBuf[readPos++];
			// JSON does not allow 0x00 - 0x1f in string.
			// And '"' and '\\' must be escaped.
			if (ch > '\\')
			{
				append (ch);
			}
			else if (ch > '"')
			{
				if (ch < '\\')
				{
					append (ch);
				}
				else
				{
					// handle '\\' case
					int l = readPos - m_readPos;
					m_offset += l;
					m_column += l;
					m_readPos = readPos;
					readEscape ();
					readPos = m_readPos;
				}
			}
			else if (ch >= ' ')
			{
				if (ch < '"')
				{
					append (ch);
				}
				else
				{
					// handle '"' case
					int l = readPos - m_readPos;
					m_offset += l;
					m_column += l;
					m_readPos = readPos;
					return;
				}
			}
			else if (ch == 0)
			{
				if (readPos <= m_readMax)
				{
					int l = readPos - m_readPos;
					m_offset += l;
					m_column += l;
					throw unexpected (ch);
				}
				int l = readPos - m_readPos - 1;
				m_offset += l;
				m_column += l;
				fill ();
				readPos = 0;
			}
			else
			{
				int l = readPos - m_readPos;
				m_offset += l;
				m_column += l;
				throw unexpected (ch);
			}
		}
	}

	private String getBufferString ()
	{
		if (m_simple)
			return new String (m_readBuf, m_start, m_len);
		return new String (m_appendBuf, 0, m_appendPos);
	}

	@Override
	public Event getEvent ()
	{
		return m_event;
	}

	@Override
	public JsonValue getValue ()
	{
		switch (m_event)
		{
			case START_ARRAY:
			case START_OBJECT:
				return Utils.getStructure (this);
			case END_ARRAY:
			case END_OBJECT:
			case KEY_NAME:
				throw stateError ("getValue()");
			case VALUE_TRUE:
				return JsonValue.TRUE;
			case VALUE_FALSE:
				return JsonValue.FALSE;
			case VALUE_NULL:
				return JsonValue.NULL;
			case VALUE_NUMBER:
				return new CookJsonBigDecimal (getBigDecimal ());
			case VALUE_STRING:
				return new CookJsonString (getString ());
		}
		throw stateError ("getValue()");
	}

	private void expectArrayObject () throws IOException
	{
		for (;;)
		{
			char ch = m_readBuf[m_readPos++];
			++m_offset;
			++m_column;
			// since we are only call at initiation.  We are mostly expecting
			// '[' and '{', not anything else.
			if (ch == '[')
			{
				pushState (true);
				m_event = Event.START_ARRAY;
				m_lastToken = START;
				return;
			}
			if (ch == '{')
			{
				pushState (false);
				m_event = Event.START_OBJECT;
				m_lastToken = START;
				return;
			}
			if (ch == ' ' || ch == '\t' || ch == '\r')
				continue;
			if (ch == '\n')
			{
				m_column = 1;
				++m_line;
				continue;
			}
			if (ch == 0)
			{
				if (m_readPos <= m_readMax)
					throw unexpected (ch);
				--m_offset;
				--m_column;
				fill ();
				continue;
			}
			scanUnexpected (ch);
		}
	}

	private boolean expectCommaObject () throws IOException
	{
		for (;;)
		{
			char ch = m_readBuf[m_readPos++];
			++m_offset;
			++m_column;
			if (ch == ',')
				return false;
			if (ch == '}')
			{
				popState (false);
				m_event = Event.END_OBJECT;
				m_lastToken = VALUE;
				return true;
			}
			if (ch == ' ' || ch == '\t' || ch == '\r')
				continue;
			if (ch == '\n')
			{
				m_column = 1;
				++m_line;
				continue;
			}
			if (ch == 0)
			{
				if (m_readPos <= m_readMax)
					throw unexpected (ch);
				--m_offset;
				--m_column;
				fill ();
				continue;
			}
			scanUnexpected (ch);
		}
	}

	private boolean expectCommaArray () throws IOException
	{
		for (;;)
		{
			char ch = m_readBuf[m_readPos++];
			++m_offset;
			++m_column;
			if (ch == ',')
				return false;
			if (ch == ']')
			{
				popState (true);
				m_event = Event.END_ARRAY;
				m_lastToken = VALUE;
				return true;
			}
			if (ch == ' ' || ch == '\t' || ch == '\r')
				continue;
			if (ch == '\n')
			{
				m_column = 1;
				++m_line;
				continue;
			}
			if (ch == 0)
			{
				if (m_readPos <= m_readMax)
					throw unexpected (ch);
				--m_offset;
				--m_column;
				fill ();
				continue;
			}
			scanUnexpected (ch);
		}
	}

	private void expectColon () throws IOException
	{
		for (;;)
		{
			char ch = m_readBuf[m_readPos++];
			++m_offset;
			++m_column;
			if (ch == ':')
				return;
			if (ch == ' ' || ch == '\t' || ch == '\r')
				continue;
			if (ch == '\n')
			{
				m_column = 1;
				++m_line;
				continue;
			}
			if (ch == 0)
			{
				if (m_readPos <= m_readMax)
					throw unexpected (ch);
				--m_offset;
				--m_column;
				fill ();
				continue;
			}
			scanUnexpected (ch);
		}
	}

	private void expectKeyName () throws IOException
	{
		for (;;)
		{
			char ch = m_readBuf[m_readPos++];
			++m_offset;
			++m_column;
			if (ch == '"')
			{
				saveLocation ();
				readString ();
				m_event = Event.KEY_NAME;
				m_lastToken = FIELD;
				m_event = Event.KEY_NAME;
				return;
			}
			if (ch == '}')
			{
				popState (false);
				m_event = Event.END_OBJECT;
				m_lastToken = VALUE;
				return;
			}
			if (ch == ' ' || ch == '\t' || ch == '\r')
				continue;
			if (ch == '\n')
			{
				m_column = 1;
				++m_line;
				continue;
			}
			if (ch == 0)
			{
				if (m_readPos <= m_readMax)
					throw unexpected (ch);
				--m_offset;
				--m_column;
				fill ();
				continue;
			}
			scanUnexpected (ch);
		}
	}

	private void expectValue () throws IOException
	{
		for (;;)
		{
			char ch = m_readBuf[m_readPos++];
			++m_offset;
			++m_column;
			switch (ch)
			{
				case '\t':
				case '\r':
					break;
				case '\n':
					m_column = 1;
					++m_line;
					break;
				case ' ':
					break;
				case '"':
				{
					saveLocation ();
					readString ();
					m_lastToken = VALUE;
					m_event = Event.VALUE_STRING;
					return;
				}
				case '-':
				{
					readNegative ();
					m_event = Event.VALUE_NUMBER;
					m_lastToken = VALUE;
					return;
				}
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
				{
					m_appendPos = 0;
					m_start = m_readPos - 1;
					readFastNumber (ch);
					m_event = Event.VALUE_NUMBER;
					m_lastToken = VALUE;
					return;
				}
				case '[':
				{
					pushState (true);
					m_event = Event.START_ARRAY;
					m_lastToken = START;
					return;
				}
				case ']':
				{
					popState (true);
					m_event = Event.END_ARRAY;
					m_lastToken = VALUE;
					return;
				}
				case 'f':
				{
					expect ("alse");
					m_event = Event.VALUE_FALSE;
					m_lastToken = VALUE;
					return;
				}
				case 'n':
				{
					expect ("ull");
					m_event = Event.VALUE_NULL;
					m_lastToken = VALUE;
					return;
				}
				case 't':
				{
					expect ("rue");
					m_event = Event.VALUE_TRUE;
					m_lastToken = VALUE;
					return;
				}
				case '{':
				{
					pushState (false);
					m_event = Event.START_OBJECT;
					m_lastToken = START;
					return;
				}
				case 0:
				{
					if (m_readPos <= m_readMax)
						throw unexpected (ch);
					--m_offset;
					--m_column;
					fill ();
					break;
				}
				default:
				{
					scanUnexpected (ch);
				}
			}
		}
	}

	@Override
	public Event next ()
	{
		try
		{
			int state = m_state;
			if (state == ParserState.IN_OBJECT)
			{
				int lastToken = m_lastToken;
				if (lastToken == FIELD)
				{
					expectColon ();
					expectValue ();
					return m_event;
				}

				if (lastToken == VALUE)
				{
					// we now expect either ',' or '}'
					if (expectCommaObject ())
					{
						return m_event;
					}
				}

				expectKeyName ();
				return m_event;
			}
			else if (state == ParserState.IN_ARRAY)
			{
				if (m_lastToken == VALUE)
				{
					// we now expect either ',' or ']'
					if (expectCommaArray ())
					{
						return m_event;
					}
				}
				expectValue ();
				return m_event;
			}
			else if (state == ParserState.INITIAL)
			{
				expectArrayObject ();
				return m_event;
			}
			else if (state == ParserState.END)
				throw new NoSuchElementException ();
			throw new IllegalStateException ();
		}
		catch (IOException ex)
		{
			throw new JsonParsingException (ex.getMessage (), ex, getCurrentLocation ());
		}
	}

	@Override
	public boolean hasNext ()
	{
		return m_state != ParserState.END;
	}

	@Override
	public boolean isIntegralNumber ()
	{
		if (m_event != Event.VALUE_NUMBER)
			throw stateError ("isIntegralNumber()");
		return m_int || getBigDecimal ().scale () == 0;
	}

	@Override
	public int getInt ()
	{
		if (m_event != Event.VALUE_NUMBER)
			throw stateError ("getInt()");
		if (m_int)
		{
			int len = m_simple ? m_len : m_appendPos;
			if (len < 10 ||
				(len < 11 && m_appendBuf[0] == '-'))
			{
				return Integer.valueOf (getBufferString ());
			}
		}
		BigDecimal bd = m_simple ? new BigDecimal (m_readBuf, m_start, m_len) : new BigDecimal (getBufferString ());
		return bd.intValue ();
	}

	@Override
	public long getLong ()
	{
		if (m_event != Event.VALUE_NUMBER)
			throw stateError ("getLong()");
		if (m_int)
		{
			int len = m_simple ? m_len : m_appendPos;
			if (len < 19 ||
				(len < 20 && m_appendBuf[0] == '-'))
			{
				return Long.valueOf (getBufferString ());
			}
		}
		BigDecimal bd = m_simple ? new BigDecimal (m_readBuf, m_start, m_len) : new BigDecimal (getBufferString ());
		return bd.longValue ();
	}

	@Override
	public BigDecimal getBigDecimal ()
	{
		if (m_event != Event.VALUE_NUMBER)
			throw stateError ("getBigDecimal()");
		if (m_simple)
			return new BigDecimal (m_readBuf, m_start, m_len);
		return new BigDecimal (getBufferString ());
	}

	private JsonLocation getCurrentLocation ()
	{
		JsonLocationImpl location = new JsonLocationImpl ();
		location.m_columnNumber = m_column;
		location.m_streamOffset = m_offset;
		location.m_lineNumber = m_line;
		return location;
	}

	@Override
	public JsonLocation getLocation ()
	{
		JsonLocationImpl location = new JsonLocationImpl ();
		long diff = 0;
		switch (m_event)
		{
			case START_OBJECT:
			case START_ARRAY:
			case END_OBJECT:
			case END_ARRAY:
				diff = 1;
				break;
			case VALUE_NUMBER:
				if (m_simple)
					diff = m_len;
				else
					diff = m_appendPos;
				break;
			case KEY_NAME:
			case VALUE_STRING:
			{
				location.m_columnNumber = savedColumn - 1;	// minus 1 to account for '"'
				location.m_streamOffset = savedOffset - 1;	// minus 1 to account for '"'
				location.m_lineNumber = savedLine;
				return location;
			}
			case VALUE_FALSE:
				diff = 5;
				break;
			case VALUE_NULL:
			case VALUE_TRUE:
				diff = 4;
				break;
		}
		location.m_columnNumber = m_column - diff;
		location.m_streamOffset = m_offset - diff;
		location.m_lineNumber = m_line;
		return location;
	}

	@Override
	public String getString ()
	{
		if (m_event != Event.VALUE_STRING &&
			m_event != Event.VALUE_NUMBER &&
			m_event != Event.KEY_NAME)
			throw stateError ("getString()");
		return getBufferString ();
	}

	@Override
	public boolean isBinary ()
	{
		if (m_event != Event.VALUE_STRING)
			throw stateError ("isBinary()");
		// For Json, it is up to the caller to interpret the string
		// value as the binary encoded string.
		return false;
	}

	@Override
	public byte[] getBytes ()
	{
		if (m_event != Event.VALUE_STRING)
			throw stateError ("getBytes()");
		throw new IllegalStateException ("The current string value is not binary.");
	}

	@Override
	public void close ()
	{
		try
		{
			m_reader.close ();
		}
		catch (IOException ex)
		{
			throw new JsonParsingException (ex.getMessage (), ex, getCurrentLocation ());
		}
	}

	private void pushState (boolean isArray)
	{
		if (m_state != ParserState.INITIAL)
			m_states.add (Boolean.valueOf (m_state == ParserState.IN_ARRAY));
		m_state = isArray ? ParserState.IN_ARRAY : ParserState.IN_OBJECT;
	}

	private void popState (boolean isArrayEnd)
	{
		boolean isArray = m_state == ParserState.IN_ARRAY;
		if (isArrayEnd != isArray)
			throw new IllegalStateException ();
		if (m_states.isEmpty ())
			m_state = ParserState.END;
		else
		{
			boolean b = m_states.remove (m_states.size () - 1);
			m_state = b ? ParserState.IN_ARRAY : ParserState.IN_OBJECT;
		}
	}

	/**
	 * @return	the allowComments
	 */
	public boolean isAllowComments ()
	{
		return m_allowComments;
	}

	/**
	 * @param	allowComments
	 *			the allowComments to set
	 */
	public void setAllowComments (boolean allowComments)
	{
		m_allowComments = allowComments;
	}
}
