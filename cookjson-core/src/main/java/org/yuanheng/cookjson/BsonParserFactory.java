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

import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;

/**
 * @author	Heng Yuan
 */
class BsonParserFactory implements JsonParserFactory
{
	private final Map<String, ?> m_config;

	public BsonParserFactory (Map<String, ?> config)
	{
		m_config = config;
	}

	@Override
	public JsonParser createParser (Reader reader)
	{
		throw new JsonException ("BSON does not support Reader I/O.");
	}

	@Override
	public JsonParser createParser (InputStream is)
	{
		return new BsonParser (is);
	}

	@Override
	public JsonParser createParser (InputStream is, Charset charset)
	{
		return new BsonParser (is);
	}

	@Override
	public JsonParser createParser (JsonObject obj)
	{
		return null;
	}

	@Override
	public JsonParser createParser (JsonArray array)
	{
		return null;
	}

	@Override
	public Map<String, ?> getConfigInUse ()
	{
		return Collections.unmodifiableMap (m_config);
	}
}
