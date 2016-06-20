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
import java.io.OutputStream;
import java.io.Writer;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;

import org.yuanheng.cookjson.value.CookJsonBinary;

/**
 * @author	Heng Yuan
 */
public class PrettyTextJsonGenerator extends TextJsonGenerator
{
	private String m_indent = "\t";

	public PrettyTextJsonGenerator (OutputStream os)
	{
		super (os);
	}

	public PrettyTextJsonGenerator (Writer out)
	{
		super (out);
	}

	/**
	 * Sets the indentation string.
	 * @param	indent
	 *			the indentation string
	 */
	public void setIndentation (String indent)
	{
		m_indent = indent;
	}

	@Override
	void writeName (String name) throws IOException
	{
		if (m_first)
			m_first = false;
		else
			w (',');

		// indent the value
		w ('\n');
		int indents = m_states.size ();
		String indent = m_indent;
		for (int i = 0; i < indents; ++i)
			w (indent);

		if (m_keyNameEscaped)
			w (name);
		else
			quote (name);
		w (" : ");
	}

	@Override
	void writeComma () throws IOException
	{
		if (m_first)
		{
			m_first = false;
			int indents = m_states.size ();
			if (indents == 0)
				return;
			w ('\n');
			String indent = m_indent;
			for (int i = 0; i < indents; ++i)
				w (indent);
		}
		else
		{
			w (',');

			// indent the value
			w ('\n');
			int indents = m_states.size ();
			String indent = m_indent;
			for (int i = 0; i < indents; ++i)
				w (indent);
		}
	}

	@Override
	public JsonGenerator writeEnd ()
	{
		if (!m_first)
		{
			try
			{
				// indent the value
				w ('\n');
				int indents = m_states.size ();
				String indent = m_indent;
				for (int i = 1; i < indents; ++i)
					w (indent);
			}
			catch (IOException ex)
			{
				throw new JsonException (ex.getMessage (), ex);
			}
		}
		return super.writeEnd ();
	}

	@Override
	JsonGenerator writeValue (JsonValue value) throws IOException
	{
		switch (value.getValueType ())
		{
			case ARRAY:
			{
				JsonArray array = (JsonArray) value;
				w ('[');
				pushState (true);
				m_first = true;
				for (JsonValue v : array)
				{
					writeComma ();
					writeValue (v);
				}
				if (!m_first)
				{
					// indent the value
					w ('\n');
					int indents = m_states.size ();
					String indent = m_indent;
					for (int i = 1; i < indents; ++i)
						w (indent);
				}
				w (']');
				popState ();
				m_first = false;
				break;
			}
			case OBJECT:
			{
				JsonObject obj = (JsonObject) value;
				w ('{');
				pushState (false);
				m_first = true;
				for (Map.Entry<String, JsonValue> entry : obj.entrySet ())
				{
					JsonValue v = entry.getValue ();
					writeName (entry.getKey ());
					writeValue (v);
				}
				if (!m_first)
				{
					// indent the value
					w ('\n');
					int indents = m_states.size ();
					String indent = m_indent;
					for (int i = 1; i < indents; ++i)
						w (indent);
				}
				w ('}');
				popState ();
				m_first = false;
				break;
			}
			case NULL:
			{
				w ("null");
				break;
			}
			case NUMBER:
			{
				w (value.toString ());
				break;
			}
			case STRING:
			{
				if (value instanceof CookJsonBinary)
				{
					byte[] bytes = ((CookJsonBinary) value).getBytes ();
					if (m_binaryFormat == BinaryFormat.BINARY_FORMAT_BASE64)
						base64Encode (bytes);
					else
						hexEncode (bytes);
				}
				else
					quote (value.toString ());
				break;
			}
			case TRUE:
			{
				w ("true");
				break;
			}
			case FALSE:
			{
				w ("false");
				break;
			}
		}
		return this;
	}
}
