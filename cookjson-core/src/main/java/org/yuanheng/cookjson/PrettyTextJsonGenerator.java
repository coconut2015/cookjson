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
	JsonGenerator writeValue (String value)
	{
		try
		{
			Writer out = m_out;
			if (m_first)
				m_first = false;
			else
				out.write (',');

			if (m_state != GeneratorState.INITIAL)
			{
				// indent the value
				out.write ('\n');
				int indents = m_states.size () + 1;
				String indent = m_indent;
				for (int i = 0; i < indents; ++i)
					out.write (indent);
			}

			if (m_name != null)
			{
				if (m_keyNameEscaped)
				{
					out.write (m_name);
				}
				else
				{
					Quote.quote (out, m_name);
				}
				out.write (" : ");
			}
			out.write (value);
		}
		catch (IOException ex)
		{
			throw new JsonException (ex.getMessage (), ex);
		}
		return this;
	}

	JsonGenerator quoteValue (String value)
	{
		try
		{	
			Writer out = m_out;
			if (m_first)
				m_first = false;
			else
				out.write (',');
			if (m_name != null)
			{
				if (m_keyNameEscaped)
				{
					out.write (m_name);
				}
				else
				{
					Quote.quote (out, m_name);
				}
				out.write (":");
			}
			Quote.quote (out, value);
		}
		catch (IOException ex)
		{
			throw new JsonException (ex.getMessage (), ex);
		}
		return this;
	}

	@Override
	public JsonGenerator writeEnd ()
	{
		if (!m_first)
		{
			try
			{
				Writer out = m_out;
				// indent the value
				out.write ('\n');
				int indents = m_states.size ();
				String indent = m_indent;
				for (int i = 0; i < indents; ++i)
					out.write (indent);
			}
			catch (IOException ex)
			{
				throw new JsonException (ex.getMessage (), ex);
			}
		}
		return super.writeEnd ();
	}
}
