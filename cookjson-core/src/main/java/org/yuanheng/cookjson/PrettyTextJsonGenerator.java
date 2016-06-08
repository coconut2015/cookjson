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
import java.io.OutputStream;
import java.io.Writer;

import javax.json.JsonException;
import javax.json.stream.JsonGenerator;

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
}
