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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.json.spi.JsonProvider;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;

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
		formatter.printHelp (pw, 78, "convertjson [options]", null, options, 2, HelpFormatter.DEFAULT_DESC_PAD, null);
		pw.flush ();
	}

	public static void main (String[] args)
	{
		Options options = new Options ();

		options.addOption ("h", "help", false, "print this message.");

		options.addOption ("f", "from", true, "from file");
		options.addOption ("t", "to", true, "to file");
		// source options
		options.addOption ("a", "array", false, "treat BSON root document as array.");
		// target options
		// -- JSON specific
		options.addOption ("p", "pretty", false, "pretty output for JSON format.");
		// -- BSON specific
		options.addOption ("d", "double", false, "use double for BSON to store BigDecimal / BigInteger.");
		options.addOption ("n", "nofix", false, "disable fixing of BSON lengths.");

		if (args.length == 0)
		{
			usage (options);
			System.exit (1);
		}

		String src = null;
		String dst = null;
		boolean pretty = false;
		boolean useDouble = false;
		boolean rootAsArray = false;
		boolean fixBson = true;

		CommandLine cmdLine = null;
		try
		{
			cmdLine = new DefaultParser ().parse (options, args);
		}
		catch (ParseException ex)
		{
			System.out.println (ex.getMessage ());
			System.exit (1);
		}

		for (Option opt : cmdLine.getOptions ())
		{
			switch (opt.getOpt ().charAt (0))
			{
				case 'h':
					usage (options);
					return;
				case 'f':
					src = opt.getValue ();
					break;
				case 't':
					dst = opt.getValue ();
					break;
				case 'a':
					rootAsArray = true;
					break;
				case 'd':
					useDouble = true;
					break;
				case 'p':
					pretty = true;
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

		try
		{
			JsonProvider provider = JsonProvider.provider ();

			HashMap<String, Object> bsonConfig = new HashMap<String, Object> ();
			bsonConfig.put (CookJsonProvider.FORMAT, CookJsonProvider.FORMAT_BSON);
			if (useDouble)
			{
				bsonConfig.put (CookJsonProvider.USE_DOUBLE, Boolean.TRUE);
			}
			if (rootAsArray)
			{
				bsonConfig.put (CookJsonProvider.ROOT_AS_ARRAY, Boolean.TRUE);
			}

			HashMap<String, Object> textConfig = new HashMap<String, Object> ();
			if (pretty)
			{
				textConfig.put (JsonGenerator.PRETTY_PRINTING, Boolean.TRUE);
			}

			FileInputStream is = new FileInputStream (src);
			JsonParser p;
			if (srcBson)
			{
				JsonParserFactory f = provider.createParserFactory (bsonConfig);
				p = f.createParser (is);
			}
			else
			{
				JsonParserFactory f = provider.createParserFactory (textConfig);
				p = f.createParser (is);
			}
	
			FileOutputStream os = new FileOutputStream (dst);
			JsonGenerator g;
			if (dstBson)
			{
				JsonGeneratorFactory f = provider.createGeneratorFactory (bsonConfig);
				g = f.createGenerator (os);
			}
			else
			{
				JsonGeneratorFactory f = provider.createGeneratorFactory (textConfig);
				g = f.createGenerator (os);
			}
			Utils.convert (p, g);
			g.close ();
			p.close ();

			if (dstBson && fixBson)
				FixBson.fix (new File (dst));
		}
		catch (IllegalStateException ex)
		{
			System.out.println ("State error.");
			assert Debug.debug (ex);
			System.exit (1);
		}
		catch (Exception ex)
		{
			System.out.println (ex.getMessage ());
			assert Debug.debug (ex);
			System.exit (1);
		}
	}
}
