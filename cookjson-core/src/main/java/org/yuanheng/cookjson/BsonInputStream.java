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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import javax.json.JsonException;

/**
 * @author Heng Yuan
 */
class BsonInputStream
{
	final static int m_max = 8192;
	private final byte[] m_buffer = new byte[m_max];
	private int m_readPos;
	private int m_readMax;
	private InputStream m_is;
	private byte[] m_strBuffer;

	private long m_location;

	public BsonInputStream (InputStream is)
	{
		m_is = is;
	}

	private void fill () throws IOException
	{
		m_readPos = 0;
		m_readMax = m_is.read (m_buffer);
		if (m_readMax <= 0)
			ioError ("unexpected eof.");
	}

	private void ioError (String msg)
	{
		throw new JsonException ("Offset " + m_location + ": " + msg);
	}

	public void readFully (byte[] b) throws IOException
	{
		int off = 0;
		int len = b.length;

		m_location += len;

		byte[] buf = m_buffer;
		int readPos = m_readPos;

		// see if we have the chars in the buffer
		if (readPos + len < m_readMax)
		{
			for (int i = 0; i < len; ++i)
				b[i] = buf[readPos++];
			m_readPos = readPos;
			return;
		}

		// okay we don't, copy what we have to b
		int readMax = m_readMax;
		while (readPos < readMax)
		{
			b[off++] = buf[readPos++];
		}

		// now read the rest directly into b;
		int readSize;
		InputStream is = m_is;
		len -= off;
		do
		{
			readSize = is.read (b, off, len);
			if (readSize < 0)
				throw new EOFException ();
			off += readSize;
			len -= readSize;
		}
		while (len > 0);

		// indicates that we do not have anything buffered;
		m_readPos = 0;
		m_readMax = 0;
	}

	public boolean readBoolean () throws IOException
	{
		if (m_readPos >= m_readMax)
			fill ();
		++m_location;
		return m_buffer[m_readPos++] != 0;
	}

	public byte read () throws IOException
	{
		if (m_readPos >= m_readMax)
			fill ();
		++m_location;
		return m_buffer[m_readPos++];
	}

	public int readInt () throws IOException
	{
		return (read () & 0xff) |
			   ((read () & 0xff) << 8) |
			   ((read () & 0xff) << 16) |
			   ((read () & 0xff) << 24);
	}

	public long readLong () throws IOException
	{
		return ((long)readInt () & 0xffffffffL) | ((long)readInt () << 32);
	}

	public double readDouble () throws IOException
	{
		return Double.longBitsToDouble (readLong ());
	}

	public String readCString () throws IOException
	{
		byte[] strBuf = m_strBuffer;
		if (strBuf == null)
		{
			strBuf = new byte[200];
			m_strBuffer = strBuf;
		}
		int bufferLen = strBuf.length;

		int pos = 0;
		byte[] buf = m_buffer;

loop:
		for (;;)
		{
			int readPos = m_readPos;
			int readMax = m_readMax;
			while (readPos < readMax)
			{
				byte b = buf[readPos++];
				if (b == 0)
				{
					m_readPos = readPos;
					break loop;
				}
				if (pos >= bufferLen)
				{
					bufferLen += bufferLen / 2;
					byte[] newBuffer = new byte[bufferLen];
					System.arraycopy (strBuf, 0, newBuffer, 0, pos);
					strBuf = newBuffer;
					m_strBuffer = strBuf;
				}
				strBuf[pos++] = b;
			}
			fill ();
		}
		m_location += pos + 1;		// +1 for terminating null.
		if (pos == 0)
			return "";
		return new String (strBuf, 0, pos, BOM.utf8);
	}

	public String getStringValue () throws IOException
	{
		int size = readInt ();
		byte[] bytes = new byte[size];
		readFully (bytes);
		return new String (bytes, 0, size - 1, BOM.utf8);
	}

	public byte[] getBinary () throws IOException
	{
		int size = readInt ();
		read ();	// binary subtype
		byte[] bytes = new byte[size];
		readFully (bytes);
		return bytes;
	}

	public byte[] getObjectId () throws IOException
	{
		byte[] bytes = new byte[12];
		readFully (bytes);
		return bytes;
	}

	public void close () throws IOException
	{
		m_is.close ();
	}

	public long getLocation ()
	{
		return m_location;
	}
}
