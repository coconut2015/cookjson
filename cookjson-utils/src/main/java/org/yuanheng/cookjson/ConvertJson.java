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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;

import org.apache.commons.cli.*;

/**
 * An extremely simple utility that dumps a BSON file to JSON file.
 *
 * @author	Heng Yuan
 */
public class ConvertJson
{
	private static void usage (Options options)
	{
		PrintWriter pw = new PrintWriter (System.out);
		HelpFormatter formatter = new HelpFormatter ();
		formatter.printHelp (pw, 78, "cookjson-utils.jar [options]", null, options, 2, HelpFormatter.DEFAULT_DESC_PAD, null);
		pw.flush ();
	}

	public static void main (String[] args) throws Exception
	{
		Options options = new Options ();

		options.addOption ("f", "from", true, "from file");
		options.addOption ("t", "to", true, "to file");
		options.addOption ("p", "pretty", false, "pretty output for text format.");
		options.addOption ("h", "help", false, "print this message.");

		if (args.length == 0)
		{
			usage (options);
			System.exit (1);
		}

		String src = null;
		String dst = null;
		boolean pretty = false;

		CommandLine cmdLine = new DefaultParser ().parse (options, args);

		for (Option opt : cmdLine.getOptions ())
		{
			switch (opt.getOpt ().charAt (0))
			{
				case 'h':
					usage (options);
					System.exit (0);
					break;
				case 'p':
					pretty = true;
					break;
				case 'f':
					src = opt.getValue ();
					break;
				case 't':
					dst = opt.getValue ();
					break;
			}
		}

		if (src == null)
		{
			System.out.println ("missing source file.");
			System.exit (1);
		}

		if (dst == null)
		{
			System.out.println ("missing target file.");
			System.exit (1);
		}

		boolean srcBson = false;
		boolean dstBson = false;

		if (src.endsWith (".bson"))
			srcBson = true;
		if (dst.endsWith (".bson"))
			dstBson = true;

		FileInputStream is = new FileInputStream (src);
		JsonParser p;

		if (srcBson)
			p = new BsonParser (is);
		else
			p = Json.createParser (is);

		FileOutputStream os = new FileOutputStream (dst);
		JsonGenerator g;
		if (dstBson)
			g = new CheckedBsonGenerator (os);
		else
		{
			if (pretty)
			{
				g = new FastPrettyJsonGenerator (new OutputStreamWriter (os, "utf-8"));
			}
			else
			{
				g = new FastJsonGenerator (new OutputStreamWriter (os, "utf-8"));
			}
		}
		Utils.convert (p, g);
		g.close ();
		p.close ();
	}
}
