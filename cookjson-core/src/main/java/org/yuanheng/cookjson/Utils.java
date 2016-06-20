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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.yuanheng.cookjson.value.*;

/**
 * @author	Heng Yuan
 */
public class Utils
{
	public static int THRESHOLD = 10000;

	static void setInt (byte[] bytes, int value)
	{
		bytes[0] = (byte) (value & 0xff);
		bytes[1] = (byte) ((value >> 8) & 0xff);
		bytes[2] = (byte) ((value >> 16) & 0xff);
		bytes[3] = (byte) ((value >> 24) & 0xff);
	}

	static void setLong (byte[] bytes, long value)
	{
		bytes[0] = (byte) (value & 0xff);
		bytes[1] = (byte) ((value >> 8) & 0xff);
		bytes[2] = (byte) ((value >> 16) & 0xff);
		bytes[3] = (byte) ((value >> 24) & 0xff);
		bytes[4] = (byte) ((value >> 32) & 0xff);
		bytes[5] = (byte) ((value >> 40) & 0xff);
		bytes[6] = (byte) ((value >> 48) & 0xff);
		bytes[7] = (byte) ((value >> 56) & 0xff);
	}

	private static void addStructure (CookJsonParser p, JsonStructure struct)
	{
		ArrayList<JsonStructure> structStack = new ArrayList<JsonStructure> ();

		structStack.add (struct);
//		assert Debug.debug ("push " + struct.getClass ());
		String name = null;
		for (;;)
		{
			Event e = p.next ();
			JsonValue value = null;
//			assert Debug.debug ("READ: " + e);
			switch (e)
			{
				case START_ARRAY:
				{
					JsonStructure newStruct = new CookJsonArray ();
					if (struct instanceof JsonArray)
					{
						((JsonArray)struct).add (newStruct);
					}
					else
					{
						((JsonObject)struct).put (name, newStruct);
					}
					struct = newStruct;
					structStack.add (newStruct);
//					assert Debug.debug ("push array");
					continue;
				}
				case START_OBJECT:
				{
					JsonStructure newStruct = new CookJsonObject ();
					if (struct instanceof JsonArray)
					{
						((JsonArray)struct).add (newStruct);
					}
					else
					{
						((JsonObject)struct).put (name, newStruct);
					}
					struct = newStruct;
					structStack.add (newStruct);
//					assert Debug.debug ("push object");
					continue;
				}
				case KEY_NAME:
				{
					name = p.getString ();
					continue;
				}
				case END_ARRAY:
				{
//					assert Debug.debug ("end array");
					value = structStack.remove (structStack.size () - 1);
					if (!(value instanceof JsonArray))
						throw new IllegalStateException ();
					if (structStack.isEmpty ())
						return;	// done
					struct = structStack.get (structStack.size () - 1);
					continue;
				}
				case END_OBJECT:
				{
//					assert Debug.debug ("end object");
					value = structStack.remove (structStack.size () - 1);
					if (!(value instanceof JsonObject))
					{
//						assert Debug.debug ("invalid value: " + value.getClass ());
						throw new IllegalStateException ();
					}
					if (structStack.isEmpty ())
						return;	// done
					struct = structStack.get (structStack.size () - 1);
					continue;
				}
				case VALUE_TRUE:
				case VALUE_FALSE:
				case VALUE_NULL:
				case VALUE_NUMBER:
				case VALUE_STRING:
				{
					value = p.getValue ();
					break;
				}
			}
			if (struct instanceof JsonArray)
			{
				((JsonArray)struct).add (value);
			}
			else
			{
				((JsonObject)struct).put (name, value);
				name = null;
			}
		}
	}

	public static JsonValue getValue (CookJsonParser p)
	{
		Event e = p.getEvent ();
		switch (e)
		{
			case START_ARRAY:
			{
				CookJsonArray v = new CookJsonArray ();
				addStructure (p, v);
				return v;
			}
			case START_OBJECT:
			{
				CookJsonObject v = new CookJsonObject ();
				addStructure (p, v);
				return v;
			}
			case KEY_NAME:
			case END_ARRAY:
			case END_OBJECT:
				throw new IllegalStateException ();
			case VALUE_TRUE:
				return CookJsonBoolean.TRUE;
			case VALUE_FALSE:
				return CookJsonBoolean.FALSE;
			case VALUE_NULL:
				return CookJsonNull.NULL;
			case VALUE_NUMBER:
			case VALUE_STRING:
				return p.getValue ();
			default:
				throw new IllegalStateException ();
		}
	}

	public static void convert (JsonParser p, JsonGenerator g)
	{
		CookJsonParser p2 = null;
		CookJsonGenerator g2 = null;
		if (p instanceof CookJsonParser)
			p2 = (CookJsonParser) p;
		if (g instanceof CookJsonGenerator)
			g2 = (CookJsonGenerator) g;
		String name = null;
		try
		{
			for (;;)
			{
				Event e = p.next ();
				switch (e)
				{
					case START_ARRAY:
//						assert Debug.debug ("READ: " + e);
						if (name == null)
							g.writeStartArray ();
						else
						{
							g.writeStartArray (name);
							name = null;
						}
						break;
					case START_OBJECT:
//						assert Debug.debug ("READ: " + e);
						if (name == null)
							g.writeStartObject ();
						else
						{
							g.writeStartObject (name);
							name = null;
						}
						break;
					case KEY_NAME:
//						assert Debug.debug ("READ: " + e + " = " + p.getString ());
						name = p.getString ();
						break;
					case END_ARRAY:
					case END_OBJECT:
//						assert Debug.debug ("READ: " + e);
						g.writeEnd ();
						name = null;
						break;
					case VALUE_TRUE:
					{
//						assert Debug.debug ("READ: " + e);
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
//						assert Debug.debug ("READ: " + e);
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
//						assert Debug.debug ("READ: " + e);
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
						BigDecimal value = p.getBigDecimal ();
//						assert Debug.debug ("READ: " + e + " = " + value);
						if (p.isIntegralNumber ())
						{
							try
							{
								if (name == null)
								{
									g.write (value.intValueExact ());
								}
								else
								{
									g.write (name, value.intValueExact ());
									name = null;
								}
							}
							catch (ArithmeticException ex)
							{
								try
								{
									if (name == null)
									{
										g.write (value.longValueExact ());
									}
									else
									{
										g.write (name, value.longValueExact ());
										name = null;
									}
								}
								catch (ArithmeticException ex2)
								{
									if (name == null)
									{
										g.write (value.toBigInteger ());
									}
									else
									{
										g.write (name, value.toBigInteger ());
										name = null;
									}
								}
							}
						}
						else
						{
							if (name == null)
							{
								g.write (value);
							}
							else
							{
								g.write (name, value);
								name = null;
							}
						}
						break;
					}
					case VALUE_STRING:
					{
//						assert Debug.debug ("READ: " + e + " = " + p.getString ());
						if (p2 != null && g2 != null)
						{
							if (p2.isBinary ())
							{
								if (name == null)
									g2.write (p2.getBytes ());
								else
								{
									g2.write (name, p2.getBytes ());
									name = null;
								}
								break;
							}
						}
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

	public static String getString (File file)
	{
		try
		{
			StringBuilder buf = new StringBuilder ();
			char[] chars = new char[8192];
			InputStreamReader r = new InputStreamReader (new FileInputStream (file), BOM.utf8);
			int len;
			while ((len = r.read (chars)) > 0)
			{
				buf.append (chars, 0, len);
			}
			r.close ();
			return buf.toString ();
		}
		catch (Exception ex)
		{
			return null;
		}
	}
}
