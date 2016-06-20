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

import java.io.StringReader;

import javax.json.JsonException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author	Heng Yuan
 */
public class TextJsonParserErrorTest
{
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	@SuppressWarnings ("resource")
	public void test1 ()
	{
		String json = "{[]}";

	    thrown.expect (JsonException.class);
	    thrown.expectMessage ("Line 1, column 2, offset 1: unexpected character '['");

	    TextJsonParser p = new TextJsonParser (new StringReader (json));
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

	    thrown.expect (JsonException.class);
	    thrown.expectMessage ("Line 1, column 2, offset 1: unexpected character '/'");

	    TextJsonParser p = new TextJsonParser (new StringReader (json));
	    p.setAllowComments (true);
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void testTrue1 ()
	{
		String json = "[ t ]";

	    thrown.expect (JsonException.class);

	    TextJsonParser p = new TextJsonParser (new StringReader (json));
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void testTrue2 ()
	{
		String json = "[ tr ]";

	    thrown.expect (JsonException.class);

	    TextJsonParser p = new TextJsonParser (new StringReader (json));
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void testTrue3 ()
	{
		String json = "[ tru ]";

	    thrown.expect (JsonException.class);

	    TextJsonParser p = new TextJsonParser (new StringReader (json));
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void testTrue4 ()
	{
		String json = "[ trua ]";

	    thrown.expect (JsonException.class);

	    TextJsonParser p = new TextJsonParser (new StringReader (json));
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void testFalse1 ()
	{
		String json = "[ f ]";

	    thrown.expect (JsonException.class);

	    TextJsonParser p = new TextJsonParser (new StringReader (json));
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void testFalse2 ()
	{
		String json = "[ fa ]";

	    thrown.expect (JsonException.class);

	    TextJsonParser p = new TextJsonParser (new StringReader (json));
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void testFalse3 ()
	{
		String json = "[ fal ]";

	    thrown.expect (JsonException.class);

	    TextJsonParser p = new TextJsonParser (new StringReader (json));
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void testFalse4 ()
	{
		String json = "[ fals ]";

	    thrown.expect (JsonException.class);

	    TextJsonParser p = new TextJsonParser (new StringReader (json));
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void testFalse5 ()
	{
		String json = "[ falsa ]";

	    thrown.expect (JsonException.class);

	    TextJsonParser p = new TextJsonParser (new StringReader (json));
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void testNull1 ()
	{
		String json = "[ n ]";

	    thrown.expect (JsonException.class);

	    TextJsonParser p = new TextJsonParser (new StringReader (json));
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void testNull2 ()
	{
		String json = "[ nu ]";

	    thrown.expect (JsonException.class);

	    TextJsonParser p = new TextJsonParser (new StringReader (json));
		for (;;)
		{
			p.next ();
		}
	}

	@Test
	@SuppressWarnings ("resource")
	public void testNull3 ()
	{
		String json = "[ nul ]";

	    thrown.expect (JsonException.class);

	    TextJsonParser p = new TextJsonParser (new StringReader (json));
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

	    thrown.expect (JsonException.class);

	    TextJsonParser p = new TextJsonParser (new StringReader (json));
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

	    thrown.expect (JsonException.class);
	    thrown.expectMessage ("Line 1, column 4, offset 3: unexpected character '1'");

	    TextJsonParser p = new TextJsonParser (new StringReader (json));
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

	    thrown.expect (JsonException.class);
	    thrown.expectMessage ("Line 1, column 6, offset 5: unexpected character ' '");

	    TextJsonParser p = new TextJsonParser (new StringReader (json));
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

	    thrown.expect (JsonException.class);
	    thrown.expectMessage ("Line 1, column 4, offset 3: unexpected character 'e'");

	    TextJsonParser p = new TextJsonParser (new StringReader (json));
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

	    thrown.expect (JsonException.class);
	    thrown.expectMessage ("Line 1, column 5, offset 4: unknown escape sequence '\\s'");

	    TextJsonParser p = new TextJsonParser (new StringReader (json));
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

	    thrown.expect (JsonException.class);
	    thrown.expectMessage ("Line 1, column 9, offset 8: unexpected character '\"'");

	    TextJsonParser p = new TextJsonParser (new StringReader (json));
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

	    thrown.expect (JsonException.class);
	    thrown.expectMessage ("Line 1, column 4, offset 3: unexpected character '\t'");

	    TextJsonParser p = new TextJsonParser (new StringReader (json));
		for (;;)
		{
			p.next ();
		}
	}
}
