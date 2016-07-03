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

import java.io.ByteArrayInputStream;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParsingException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author	Heng Yuan
 */
public class UTF8TextJsonParserErrorTest
{
	@Rule
	public ExpectedException thrown = ExpectedException.none ();

	@Test
	public void testBOM ()
	{
		byte[] bytes = new byte[]
		{ '{' };

		thrown.expect (JsonParsingException.class);
		thrown.expectMessage ("JSON minimum size is 2");

		CookJsonParser p = TextJsonConfigHandler.getJsonParser (new ByteArrayInputStream (bytes));
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void test1 ()
	{
		String json = "{[]}";

		thrown.expect (JsonParsingException.class);
		thrown.expectMessage ("Parsing error at line 1, column 2, offset 1: unexpected character '['");

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void test2 ()
	{
		String json = "{/}";

		thrown.expect (JsonParsingException.class);
		thrown.expectMessage ("Parsing error at line 1, column 2, offset 1: unexpected character '/'");

		UTF8TextJsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		p.setAllowComments (true);
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void test3 ()
	{
		String json = "{{}}";

		thrown.expect (JsonParsingException.class);
		thrown.expectMessage ("Parsing error at line 1, column 2, offset 1: unexpected character '{'");

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		for (;;)
		{
			p.next ();
		}
	}

	@Test (expected = IllegalStateException.class)
	@SuppressWarnings ("resource")
	public void testStateError ()
	{
		String json = "{\"abc\":1234}";

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		for (;;)
		{
			switch (p.next ())
			{
				case START_OBJECT:
					p.getString ();
					break;
				default:
					break;
			}
		}
	}

	@Test (expected = JsonParsingException.class)
	@SuppressWarnings ("resource")
	public void testTrue1 ()
	{
		String json = "[ t ]";

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		for (;;)
		{
			p.next ();
		}
	}

	@Test (expected = JsonParsingException.class)
	@SuppressWarnings ("resource")
	public void testTrue2 ()
	{
		String json = "[ tr ]";

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		for (;;)
		{
			p.next ();
		}
	}

	@Test (expected = JsonParsingException.class)
	@SuppressWarnings ("resource")
	public void testTrue3 ()
	{
		String json = "[ tru ]";

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		for (;;)
		{
			p.next ();
		}
	}

	@Test (expected = JsonParsingException.class)
	@SuppressWarnings ("resource")
	public void testTrue4 ()
	{
		String json = "[ trua ]";

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		for (;;)
		{
			p.next ();
		}
	}

	@Test (expected = JsonParsingException.class)
	@SuppressWarnings ("resource")
	public void testFalse1 ()
	{
		String json = "[ f ]";

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		for (;;)
		{
			p.next ();
		}
	}

	@Test (expected = JsonParsingException.class)
	@SuppressWarnings ("resource")
	public void testFalse2 ()
	{
		String json = "[ fa ]";

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		for (;;)
		{
			p.next ();
		}
	}

	@Test (expected = JsonParsingException.class)
	@SuppressWarnings ("resource")
	public void testFalse3 ()
	{
		String json = "[ fal ]";

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		for (;;)
		{
			p.next ();
		}
	}

	@Test (expected = JsonParsingException.class)
	@SuppressWarnings ("resource")
	public void testFalse4 ()
	{
		String json = "[ fals ]";

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		for (;;)
		{
			p.next ();
		}
	}

	@Test (expected = JsonParsingException.class)
	@SuppressWarnings ("resource")
	public void testFalse5 ()
	{
		String json = "[ falsa ]";

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		for (;;)
		{
			p.next ();
		}
	}

	@Test (expected = JsonParsingException.class)
	@SuppressWarnings ("resource")
	public void testNull1 ()
	{
		String json = "[ n ]";

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		for (;;)
		{
			p.next ();
		}
	}

	@Test (expected = JsonParsingException.class)
	@SuppressWarnings ("resource")
	public void testNull2 ()
	{
		String json = "[ nu ]";

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		for (;;)
		{
			p.next ();
		}
	}

	@Test (expected = JsonParsingException.class)
	@SuppressWarnings ("resource")
	public void testNull3 ()
	{
		String json = "[ nul ]";

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void testNull4 ()
	{
		String json = "[ nula ]";

		thrown.expect (JsonParsingException.class);

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void testNumber1 ()
	{
		String json = "[ 01 ]";

		thrown.expect (JsonParsingException.class);
		thrown.expectMessage ("Parsing error at line 1, column 4, offset 3: unexpected character '1'");

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void testNumber2 ()
	{
		String json = "[ -1e ]";

		thrown.expect (JsonParsingException.class);
		thrown.expectMessage ("Parsing error at line 1, column 6, offset 5: unexpected character ' '");

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void testNumber2_2 ()
	{
		String json = "[ -1e ]";

		thrown.expect (JsonParsingException.class);
		thrown.expectMessage ("Parsing error at line 1, column 6, offset 5: unexpected character ' '");

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)), 2);
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void testNumber3 ()
	{
		String json = "[ -e ]";

		thrown.expect (JsonParsingException.class);
		thrown.expectMessage ("Parsing error at line 1, column 4, offset 3: unexpected character 'e'");

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void testNumber4 ()
	{
		String json = "[ -1e- ]";

		thrown.expect (JsonParsingException.class);
		thrown.expectMessage ("Parsing error at line 1, column 7, offset 6: unexpected character ' '");

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void testNumber5 ()
	{
		String json = "[ -0 ]";

		thrown.expect (JsonParsingException.class);
		thrown.expectMessage ("Parsing error at line 1, column 5, offset 4: unexpected character ' '");

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void testNumber5_2 ()
	{
		String json = "[ -0 ]";

		thrown.expect (JsonParsingException.class);
		thrown.expectMessage ("Parsing error at line 1, column 5, offset 4: unexpected character ' '");

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)), 2);
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void testNumber6 ()
	{
		String json = "[ - ]";

		thrown.expect (JsonParsingException.class);
		thrown.expectMessage ("Parsing error at line 1, column 4, offset 3: unexpected character ' '");

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void testNumber6_2 ()
	{
		String json = "[ - ]";

		thrown.expect (JsonParsingException.class);
		thrown.expectMessage ("Parsing error at line 1, column 4, offset 3: unexpected character ' '");

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)), 2);
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void testNumber7 ()
	{
		String json = "[ -\u0000 ]";

		thrown.expect (JsonParsingException.class);
		thrown.expectMessage ("Parsing error at line 1, column 4, offset 3: unexpected character '\\u0000'");

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void testNumber7_2 ()
	{
		String json = "[ -\u0000 ]";

		thrown.expect (JsonParsingException.class);
		thrown.expectMessage ("Parsing error at line 1, column 4, offset 3: unexpected character '\\u0000'");

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)), 2);
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void testNumber8 ()
	{
		String json = "[ 0. ]";

		thrown.expect (JsonParsingException.class);
		thrown.expectMessage ("Parsing error at line 1, column 5, offset 4: unexpected character ' '");

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void testNumber8_2 ()
	{
		String json = "[ 0. ]";

		thrown.expect (JsonParsingException.class);
		thrown.expectMessage ("Parsing error at line 1, column 5, offset 4: unexpected character ' '");

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)), 2);
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void testNumber9 ()
	{
		String json = "[ 0.5e5\u0000 ]";

		thrown.expect (JsonParsingException.class);
		thrown.expectMessage ("Parsing error at line 1, column 8, offset 7: unexpected character '\\u0000'");

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void testNumber9_2 ()
	{
		String json = "[ 0.5e5\u0000 ]";

		thrown.expect (JsonParsingException.class);
		thrown.expectMessage ("Parsing error at line 1, column 8, offset 7: unexpected character '\\u0000'");

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)), 2);
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void testString1 ()
	{
		String json = "[ \"\\s\" ]";

		thrown.expect (JsonParsingException.class);
		thrown.expectMessage ("Parsing error at line 1, column 5, offset 4: unknown escape sequence '\\s'");

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void testString2 ()
	{
		String json = "[ \"\\u05c\" ]";

		thrown.expect (JsonParsingException.class);
		thrown.expectMessage ("Parsing error at line 1, column 9, offset 8: unexpected character '\"'");

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void testString3 ()
	{
		String json = "[ \"\t\" ]";

		thrown.expect (JsonParsingException.class);
		thrown.expectMessage ("Parsing error at line 1, column 4, offset 3: unexpected character '\\t'");

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void testString4 ()
	{
		String json = "[ \"\n\" ]";

		thrown.expect (JsonParsingException.class);
		thrown.expectMessage ("Parsing error at line 1, column 4, offset 3: unexpected character '\\n'");

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void testString5 ()
	{
		String json = "[ \"\b\" ]";

		thrown.expect (JsonParsingException.class);
		thrown.expectMessage ("Parsing error at line 1, column 4, offset 3: unexpected character '\\b'");

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void testString6 ()
	{
		String json = "[ \"\r\" ]";

		thrown.expect (JsonParsingException.class);
		thrown.expectMessage ("Parsing error at line 1, column 4, offset 3: unexpected character '\\r'");

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void testString7 ()
	{
		String json = "[ \"\f\" ]";

		thrown.expect (JsonParsingException.class);
		thrown.expectMessage ("Parsing error at line 1, column 4, offset 3: unexpected character '\\f'");

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void testString8 ()
	{
		String json = "[ \"\\b\u0000\" ]";

		thrown.expect (JsonParsingException.class);
		thrown.expectMessage ("Parsing error at line 1, column 6, offset 5: unexpected character '\\u0000'");

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void testString9 ()
	{
		String json = "[ \"\\b\u001f\" ]";

		thrown.expect (JsonParsingException.class);
		thrown.expectMessage ("Parsing error at line 1, column 6, offset 5: unexpected character '\\u001f'");

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void testError1 ()
	{
		String json = "[ -\\ ]";

		thrown.expect (JsonParsingException.class);
		thrown.expectMessage ("Parsing error at line 1, column 4, offset 3: unexpected character '\\\\'");

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void testError2 ()
	{
		String json = "[ -' ]";

		thrown.expect (JsonParsingException.class);
		thrown.expectMessage ("Parsing error at line 1, column 4, offset 3: unexpected character '\\''");

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void testError3 ()
	{
		String json = "[ \u0000 ]";

		thrown.expect (JsonParsingException.class);
		thrown.expectMessage ("Parsing error at line 1, column 3, offset 2: unexpected character '\\u0000'");

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void testError4 ()
	{
		String json = "{ \"a\" \u0000: 1 ]";

		thrown.expect (JsonParsingException.class);
		thrown.expectMessage ("Parsing error at line 1, column 7, offset 6: unexpected character '\\u0000'");

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void testError5 ()
	{
		String json = "{ \u0000\"a\" : 1 ]";

		thrown.expect (JsonParsingException.class);
		thrown.expectMessage ("Parsing error at line 1, column 3, offset 2: unexpected character '\\u0000'");

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void testError6 ()
	{
		String json = "\u0000{ \"a\" : 1 ]";

		thrown.expect (JsonParsingException.class);
		thrown.expectMessage ("Parsing error at line 1, column 1, offset 0: unexpected character '\\u0000'");

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void testError7 ()
	{
		String json = "{ \"a\" : 1 \u0000]";

		thrown.expect (JsonParsingException.class);
		thrown.expectMessage ("Parsing error at line 1, column 11, offset 10: unexpected character '\\u0000'");

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void testIOError ()
	{
		String json = "[{";

		thrown.expect (JsonParsingException.class);
		thrown.expectMessage ("Parsing error at line 1, column 3, offset 2: unexpected eof");

		JsonParser p = new UTF8TextJsonParser (new ByteArrayInputStream (json.getBytes (BOM.utf8)));
		for (;;)
		{
			p.next ();
		}
	}
}
