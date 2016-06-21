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
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import javax.json.JsonValue;
import javax.json.stream.JsonLocation;
import javax.json.stream.JsonParsingException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.yuanheng.cookjson.value.*;

/**
 * This BSON parser has the option of treating the root document as array.
 *
 * @author	Heng Yuan
 */
public class BsonParser implements CookJsonParser
{
	private final BsonInputStream m_is;

	private int m_fieldType;
	private String m_fieldName;

	private Event m_event;
	private Object m_value;
	private ArrayList<Boolean> m_states = new ArrayList<Boolean> ();
	private int m_state = ParserState.INITIAL;
	private JsonLocationImpl m_location = new JsonLocationImpl ();
	private boolean m_inArray;

	private boolean m_rootAsArray;
	private int m_binaryFormat;

	public BsonParser (InputStream is)
	{
		m_is = new BsonInputStream (is);

		// neither column number and line number are meaningful
		// set them to unknown.
		m_location.m_columnNumber = -1;
		m_location.m_lineNumber = -1;
	}

	@Override
	public boolean hasNext ()
	{
		return m_state != ParserState.END;
	}

	@Override
	public Event getEvent ()
	{
		return m_event;
	}

	@Override
	public JsonValue getValue ()
	{
		if (m_event == null)
			throw new IllegalStateException ();
		switch (m_event)
		{
			case START_ARRAY:
			case START_OBJECT:
				return Utils.getStructure (this);
			case VALUE_STRING:
			{
				if (m_value instanceof byte[])
				{
					CookJsonBinary v = new CookJsonBinary ((byte[]) m_value);
					v.setBinaryFormat (m_binaryFormat);
					return v;
				}
				return new CookJsonString ((String) m_value);
			}
			case VALUE_NUMBER:
			{
				if (m_value instanceof Integer)
					return new CookJsonInt ((Integer)m_value);
				if (m_value instanceof Long)
					return new CookJsonLong ((Long)m_value);
				return new CookJsonDouble ((Double) m_value);
			}
			case VALUE_NULL:
				return JsonValue.NULL;
			case VALUE_TRUE:
				return JsonValue.TRUE;
			case VALUE_FALSE:
				return JsonValue.FALSE;
			default:
				throw new IllegalStateException ();
		}
	}

	private void getField () throws IOException
	{
		m_location.m_streamOffset= m_is.getLocation ();
		m_fieldType = m_is.read () & 0xff;
		if (m_fieldType == 0)
			m_fieldName = null;
		else
			m_fieldName = m_is.readCString ();
	}

	private Event getEventFromType (int type) throws IOException
	{
		switch (type)
		{
			case 0:
			{
				Boolean b = m_states.remove (m_states.size () - 1);
				m_value = null;
				if (m_states.isEmpty ())
					m_state = ParserState.END;
				else
				{
					m_inArray = m_states.get (m_states.size () - 1);
					m_state = m_inArray ? ParserState.IN_ARRAY : ParserState.IN_OBJECT;
				}
				if (b)
					return Event.END_ARRAY;
				// should not get here since this case is handled in next()
				return Event.END_OBJECT;
			}
			case BsonType.Array:
			{
				m_states.add (Boolean.TRUE);
				m_inArray = true;
				m_state = ParserState.IN_ARRAY;
				m_value = null;
				m_is.readInt ();	// skip size;
				// sets a temporary flag that indicates the object obtained
				// was internally marked as Array.
				return Event.START_ARRAY;
			}
			case BsonType.Document:
			{
				m_states.add (Boolean.FALSE);
				m_inArray = false;
				m_state = ParserState.IN_OBJECT;
				m_value = null;
				m_is.readInt ();	// skip size;
				// sets a temporary flag that indicates the object obtained
				// was internally marked as Array.
				return Event.START_OBJECT;
			}
			case BsonType.Null:
				m_state = m_inArray ? ParserState.IN_ARRAY : ParserState.IN_OBJECT;
				m_value = null;
				return Event.VALUE_NULL;
			case BsonType.Double:
				m_state = m_inArray ? ParserState.IN_ARRAY : ParserState.IN_OBJECT;
				m_value = m_is.readDouble ();
				return Event.VALUE_NUMBER;
			case BsonType.Integer:
				m_state = m_inArray ? ParserState.IN_ARRAY : ParserState.IN_OBJECT;
				m_value = m_is.readInt ();
				return Event.VALUE_NUMBER;
			case BsonType.DateTime:
			case BsonType.TimeStamp:
			case BsonType.Long:
				m_state = m_inArray ? ParserState.IN_ARRAY : ParserState.IN_OBJECT;
				m_value = m_is.readLong ();
				return Event.VALUE_NUMBER;
			case BsonType.JavaScript:
			case BsonType.Deprecated:
			case BsonType.String:
				m_state = m_inArray ? ParserState.IN_ARRAY : ParserState.IN_OBJECT;
				m_value = m_is.getStringValue ();
				return Event.VALUE_STRING;
			case BsonType.Boolean:
				m_state = m_inArray ? ParserState.IN_ARRAY : ParserState.IN_OBJECT;
				m_value = m_is.readBoolean ();
				return ((Boolean)m_value) ? Event.VALUE_TRUE : Event.VALUE_FALSE;
			case BsonType.ObjectId:
				m_state = m_inArray ? ParserState.IN_ARRAY : ParserState.IN_OBJECT;
				m_value = m_is.getObjectId ();
				return Event.VALUE_STRING;
			case BsonType.Binary:
				m_state = m_inArray ? ParserState.IN_ARRAY : ParserState.IN_OBJECT;
				m_value = m_is.getBinary ();
				return Event.VALUE_STRING;
			default:
				throw new JsonParsingException ("Unknown field: " + type, getLocation ());	// 
		}
	}

	@Override
	public Event next ()
	{
		try
		{
//			Debug.debug ("-- STATE: " + m_state);
			switch (m_state)
			{
				case ParserState.END:
					throw new NoSuchElementException ();
				case ParserState.INITIAL:
				{
					// we need to setup as a nameless document
					// by default, BSON's root is a Document.
					m_state = m_rootAsArray ? ParserState.IN_ARRAY : ParserState.IN_OBJECT;
					m_fieldType = m_rootAsArray ? BsonType.Array : BsonType.Document;
					m_fieldName = null;
					m_event = getEventFromType (m_fieldType);
					break;
				}
				case ParserState.IN_FIELD:
				{
					// get the value
					m_event = getEventFromType (m_fieldType);
					break;
				}
				case ParserState.IN_ARRAY:
				{
					// get the field
					getField ();
					// skip the name for array.
					m_event = getEventFromType (m_fieldType);
					break;
				}
				case ParserState.IN_OBJECT:
				{
					getField ();
					if (m_fieldType == 0)
					{
						Boolean b = m_states.remove (m_states.size () - 1);
						if (b)
							throw new IllegalStateException ();
						m_value = null;
						if (m_states.isEmpty ())
							m_state = ParserState.END;
						else
						{
							m_inArray = m_states.get (m_states.size () - 1);
							m_state = m_inArray ? ParserState.IN_ARRAY : ParserState.IN_OBJECT;
						}
						return Event.END_OBJECT;
					}
//					Debug.debug ("FIELD: " + m_field);
					m_event = Event.KEY_NAME;
					m_value = m_fieldName;
					m_state = ParserState.IN_FIELD;
					break;
				}
				default:
				{
					throw new IllegalStateException ();
				}
			}
//			Debug.debug ("-- EVENT: " + m_event + ", value = " + m_value);
			return m_event;
		}
		catch (IOException ex)
		{
			throw new JsonParsingException (ex.getMessage (), ex, m_location);
		}
	}

	@Override
	public String getString ()
	{
		switch (m_event)
		{
			case KEY_NAME:
				return (String) m_value;
			case VALUE_STRING:
			{
				if (m_value instanceof byte[])
				{
					if (m_binaryFormat == BinaryFormat.BINARY_FORMAT_HEX)
						return Hex.encodeHexString ((byte[]) m_value);
					return Base64.encodeBase64String ((byte[]) m_value);
				}
				return (String) m_value;
			}
			case VALUE_NUMBER:
				return m_value.toString ();
			default:
				throw new IllegalStateException ();
		}
	}

	@Override
	public boolean isBinary ()
	{
		if (m_event != Event.VALUE_STRING)
			throw new IllegalStateException ();
		return m_value instanceof byte[];
	}

	@Override
	public byte[] getBytes ()
	{
		if (m_event != Event.VALUE_STRING)
			throw new IllegalStateException ();
		if (m_value instanceof byte[])
			return (byte[]) m_value;
		throw new IllegalStateException ();
	}

	@Override
	public boolean isIntegralNumber ()
	{
		if (m_event != Event.VALUE_NUMBER)
			throw new IllegalStateException ();
		return !(m_value instanceof Double);
	}

	@Override
	public int getInt ()
	{
		if (m_event != Event.VALUE_NUMBER)
			throw new IllegalStateException ();
		return ((Number)m_value).intValue ();
	}

	@Override
	public long getLong ()
	{
		if (m_event != Event.VALUE_NUMBER)
			throw new IllegalStateException ();
		return ((Number)m_value).longValue ();
	}

	@Override
	public BigDecimal getBigDecimal ()
	{
		if (m_event != Event.VALUE_NUMBER)
			throw new IllegalStateException ();
		if (m_value instanceof Integer)
			return new BigDecimal ((Integer)m_value);
		if (m_value instanceof Long)
			return new BigDecimal ((Long)m_value);
		return new BigDecimal ((Double)m_value);
	}

	@Override
	public JsonLocation getLocation ()
	{
		return m_location;
	}

	@Override
	public void close ()
	{
		try
		{
			m_states.clear ();
			m_is.close ();
		}
		catch (IOException ex)
		{
			throw new JsonParsingException (ex.getMessage (), ex, m_location);
		}
	}

	/**
	 * Gets the status of treating root as array.
	 * @return	the status of treating root as array.
	 */
	public boolean isRootAsArray ()
	{
		return m_rootAsArray;
	}

	/**
	 * Treats the root as Array rather than Document.
	 * <p>
	 * By default, the root is treated as Document (Object in JSON).  With
	 * this flag set, the root is treated as array, and field names are
	 * ignored as the result.
	 *
	 * @param	b
	 *			true or false.
	 */
	public void setRootAsArray (boolean b)
	{
		m_rootAsArray = b;
	}

	/**
	 * Gets the binary format for storing byte[].
	 * <p>
	 * It is one of {@link #BINARY_FORMAT_BASE64} and
	 * {@link #BINARY_FORMAT_HEX}.
	 *
	 * @return	the binaryFormat
	 */
	public int getBinaryFormat ()
	{
		return m_binaryFormat;
	}

	/**
	 * Sets the binary format for storing byte[].  The default is Base64.
	 * <p>
	 * It is one of {@link #BINARY_FORMAT_BASE64} and
	 * {@link #BINARY_FORMAT_HEX}.
	 *
	 * @param	binaryFormat
	 *			the binary format
	 */
	public void setBinaryFormat (int binaryFormat)
	{
		m_binaryFormat = binaryFormat;
	}
}
