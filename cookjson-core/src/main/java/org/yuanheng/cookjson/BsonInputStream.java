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

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Heng Yuan
 */
class BsonInputStream
{
	private final byte[] m_bytes = new byte[8];
	private InputStream m_is;
	private byte[] m_strBuffer;

	private long m_location;

	public BsonInputStream (InputStream is)
	{
		if (is instanceof BufferedInputStream)
			m_is = is;
		else
			m_is = new BufferedInputStream (is);
	}

	public void readFully (byte[] b) throws IOException
	{
		readFully (b, 0, b.length);
	}

	public void readFully (byte[] b, int off, int len) throws IOException
	{
		m_location += len;
		int readSize;
		InputStream is = m_is;
		do
		{
			readSize = is.read (b, off, len);
			if (readSize < 0)
				throw new EOFException ();
			off += readSize;
			len -= readSize;
		}
		while (len > 0);
	}

	public boolean readBoolean () throws IOException
	{
		int b;
		if ((b = m_is.read ()) < 0)
			throw new EOFException ();
		++m_location;
		return b != 0;
	}

	public byte readByte () throws IOException
	{
		int b;
		if ((b = m_is.read ()) < 0)
			throw new EOFException ();
		++m_location;
		return (byte) b;
	}

	public int readInt () throws IOException
	{
		byte[] bytes = m_bytes;
		readFully (bytes, 0, 4);
		return (bytes[0] & 0xff) |
			   ((bytes[1] & 0xff) << 8) |
			   ((bytes[2] & 0xff) << 16) |
			   ((bytes[3] & 0xff) << 24);
	}

	public long readLong () throws IOException
	{
		byte[] bytes = m_bytes;
		readFully (bytes, 0, 8);
		return (long)(bytes[0] & 0xff) |
			   (((long)(bytes[1] & 0xff)) << 8) |
			   (((long)(bytes[2] & 0xff)) << 16) |
			   (((long)(bytes[3] & 0xff)) << 24) |
			   (((long)(bytes[4] & 0xff)) << 32) |
			   (((long)(bytes[5] & 0xff)) << 40) |
			   (((long)(bytes[6] & 0xff)) << 48) |
			   (((long)(bytes[7] & 0xff)) << 56);
	}

	public double readDouble () throws IOException
	{
		return Double.longBitsToDouble (readLong ());
	}

	public String readCString () throws IOException
	{
		byte[] buffer = m_strBuffer;
		if (buffer == null)
		{
			buffer = new byte[200];
			m_strBuffer = buffer;
		}

		int pos = 0;

		int ch;
		InputStream is = m_is;

		while ((ch = is.read ()) >= 0)
		{
			if (ch == '\0')
				break;
			if (pos >= buffer.length)
			{
				byte[] newBuffer = new byte[buffer.length + buffer.length / 2];
				System.arraycopy (buffer, 0, newBuffer, 0, pos);
				buffer = newBuffer;
			}
			buffer[pos++] = (byte) ch;
		}
		if ((ch == -1) && (pos == 0))
			throw new EOFException ();
		m_location += pos + 1;		// +1 for terminating null.
		if (pos == 0)
			return "";
		return new String (buffer, 0, pos, BOM.utf8);
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
		readByte ();	// binary subtype
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
