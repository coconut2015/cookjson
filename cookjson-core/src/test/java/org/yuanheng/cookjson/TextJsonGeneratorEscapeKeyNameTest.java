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

import java.io.*;
import java.util.NoSuchElementException;

import javax.json.JsonNumber;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser.Event;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author	Heng Yuan
 */
public class TextJsonGeneratorEscapeKeyNameTest
{
	private TextJsonParser getJsonParser (File file) throws IOException
	{
		return new TextJsonParser (new InputStreamReader (new FileInputStream (file), BOM.utf8));
	}

	private void convert (CookJsonParser p, TextJsonGenerator g)
	{
		g.setKeyNameEscaped (true);	// enable the setting
		String name = null;
		try
		{
			for (;;)
			{
				Event e = p.next ();
				switch (e)
				{
					case START_ARRAY:
						if (name == null)
							g.writeStartArray ();
						else
						{
							g.writeStartArray (name);
							name = null;
						}
						break;
					case START_OBJECT:
						if (name == null)
							g.writeStartObject ();
						else
						{
							g.writeStartObject (name);
							name = null;
						}
						break;
					case KEY_NAME:
						name = Quote.quote (p.getString ());	// escape the keyname manually
						break;
					case END_ARRAY:
					case END_OBJECT:
						g.writeEnd ();
						name = null;
						break;
					case VALUE_TRUE:
					{
						if (name == null)
						{
							g.write (true);
						}
						else
						{
							g.write (name, true);
							name = null;
						}
						break;
					}
					case VALUE_FALSE:
					{
						if (name == null)
						{
							g.write (false);
						}
						else
						{
							g.write (name, false);
							name = null;
						}
						break;
					}
					case VALUE_NULL:
					{
						if (name == null)
						{
							g.writeNull ();
						}
						else
						{
							g.writeNull (name);
							name = null;
						}
						break;
					}
					case VALUE_NUMBER:
					{
						JsonNumber number = (JsonNumber) p.getValue ();
						if (name == null)
						{
							g.write (number);
						}
						else
						{
							g.write (name, number);
							name = null;
						}
						break;
					}
					case VALUE_STRING:
					{
						if (name == null)
						{
							g.write (p.getString ());
						}
						else
						{
							g.write (name, p.getString ());
							name = null;
						}
						break;
					}
					default:
						break;
				}
			}
		}
		catch (NoSuchElementException ex)
		{
		}
	}

	void testFile (String f) throws IOException
	{
		File file = new File (f.replace ('/', File.separatorChar));

		StringWriter out1 = new StringWriter ();
		TextJsonParser p1 = getJsonParser (file);
		TextJsonGenerator g1 = new TextJsonGenerator (out1);
		// convert using keyname escape
		convert (p1, g1);
		p1.close ();
		g1.close ();

		StringWriter out2 = new StringWriter ();
		TextJsonParser p2 = getJsonParser (file);
		JsonGenerator g2 = new TextJsonGenerator (out2);
		Utils.convert (p2, g2);
		p2.close ();
		g2.close ();

		Assert.assertEquals (out2.toString (), out1.toString ());
	}

	@Test
	public void test () throws IOException
	{
		testFile ("../tests/data/complex1.json");
		testFile ("../tests/data/double.json");
		testFile ("../tests/data/empty.json");
		testFile ("../tests/data/long.json");
		testFile ("../tests/data/nested1.json");
		testFile ("../tests/data/nested2.json");
	}
}
