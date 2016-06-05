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

import javax.json.stream.JsonLocation;

/**
 * @author	Heng Yuan
 */
class JsonLocationImpl implements JsonLocation, Cloneable
{
	public final static JsonLocationImpl Unknown = new JsonLocationImpl ();

	static
	{
		Unknown.m_columnNumber = -1;
		Unknown.m_lineNumber = -1;
		Unknown.m_streamOffset = -1;
	}

	private long m_lineNumber;
	private long m_columnNumber;
	private long m_streamOffset;

	@Override
	public long getLineNumber ()
	{
		return m_lineNumber;
	}

	@Override
	public long getColumnNumber ()
	{
		return m_columnNumber;
	}

	@Override
	public long getStreamOffset ()
	{
		return m_streamOffset;
	}

	public void setLineNumber (long lineNumber)
	{
		m_lineNumber = lineNumber;
	}

	public void setColumnNumber (long columnNumber)
	{
		m_columnNumber = columnNumber;
	}

	public void setStreamOffset (long streamOffset)
	{
		m_streamOffset = streamOffset;
	}

	@Override
	public String toString ()
	{
		return "Line (" + m_lineNumber + ") Column (" + m_columnNumber + ") Offset (" + m_streamOffset + ")";
	}

	public Object clone ()
	{
		try
		{
			return super.clone ();
		}
		catch (CloneNotSupportedException ex)
		{
			return null;
		}
	}
}
