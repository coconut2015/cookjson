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

import java.io.File;
import java.io.PrintWriter;

import org.apache.commons.cli.*;

/**
 * This file fixes the streaming Bson file's length value for Document / Array
 * types.
 *
 * @author	Heng Yuan
 */
public class FixBson
{
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
					return;
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

		BsonFixLength.fix (file);
	}
}
