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

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.apache.commons.cli.*;

/**
 * This file fixes the streaming Bson file's length value for Document / Array
 * types.
 *
 * @author	Heng Yuan
 */
public class FixBson
{
	public static void getOffsets (JsonParser p, ArrayList<Pair> pairs) throws IOException
	{
		long offset;
		long start;
		Stack<Long> matches = new Stack<Long> ();
		Pair pair;

		boolean firstObject = true;
		boolean justStarted = false;

		while (p.hasNext ())
		{
			Event e = p.next ();
			switch (e)
			{
				case START_OBJECT:
					if (firstObject)
					{
						offset = p.getLocation ().getStreamOffset ();
						matches.push (offset);
						firstObject = false;
					}
					justStarted = true;
					break;
				case KEY_NAME:
					if (justStarted)
					{
						justStarted = false;
						offset = p.getLocation ().getStreamOffset ();
						matches.push (offset - 4);
					}
					break;
				case END_OBJECT:
					offset = p.getLocation ().getStreamOffset () + 1;
					if (justStarted)
					{
						start = offset - 5;
						justStarted = false;
					}
					else
					{
						start = matches.pop ();
					}
					pair = new Pair ();
					pair.offset = start;
					pair.size = (int) (offset - start);
					pairs.add (pair);
					break;
				default:
					break;
			}
		}
	}

	private static void usage (Options options)
	{
		PrintWriter pw = new PrintWriter (System.out);
		HelpFormatter formatter = new HelpFormatter ();
		formatter.printHelp (pw, 78, "fixbson [options] [file]", null, options, 2, HelpFormatter.DEFAULT_DESC_PAD, null);
		pw.flush ();
	}

	public static void main (String[] args) throws Exception
	{
		Options options = new Options ();

		options.addOption ("h", "help", false, "print this message.");

		if (args.length == 0)
		{
			usage (options);
			System.exit (1);
		}

		CommandLine cmdLine = new DefaultParser ().parse (options, args);

		for (Option opt : cmdLine.getOptions ())
		{
			switch (opt.getOpt ().charAt (0))
			{
				case 'h':
					usage (options);
					System.exit (0);
					break;
			}
		}

		args = cmdLine.getArgs ();
		if (args.length == 0)
		{
			System.out.println ("Missing filename.");
			System.exit (1);
		}

		File file = new File (args[0]);
		if (!file.exists ())
		{
			System.out.println (file + " does not exist.");
			System.exit (1);
		}
		if (!file.canRead ())
		{
			System.out.println (file + " cannot be read.");
			System.exit (1);
		}
		if (!file.canWrite ())
		{
			System.out.println (file + " cannot be written.");
			System.exit (1);
		}

		FileInputStream is = new FileInputStream (file);
		JsonParser p = new BasicBsonParser (is);

		ArrayList<Pair> pairs = new ArrayList<Pair> ();

		// compute the offsets and sizes need to be updated.
		getOffsets (p, pairs);
		p.close ();

		// sort the pairs
		Pair[] pa = pairs.toArray (new Pair[pairs.size ()]);

		Arrays.sort (pa);

		byte[] bytes = new byte[4];
		ByteBuffer buffer = ByteBuffer.wrap (bytes);
		RandomAccessFile f = new RandomAccessFile (file, "rw");
		FileChannel channel = f.getChannel ();
		for (int i = 0; i < pairs.size (); ++i)
		{
			Utils.setInt (bytes, pa[i].size);
			channel.write (buffer, pa[i].offset);
			buffer.position (0);
		}
		f.close ();
	}
}
