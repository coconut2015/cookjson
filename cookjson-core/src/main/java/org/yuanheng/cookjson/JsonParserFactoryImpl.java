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

import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;

/**
 * @author	Heng Yuan
 */
class JsonParserFactoryImpl implements JsonParserFactory
{
	private final Map<String, ?> m_config;
	private final ConfigHandler m_handler;

	public JsonParserFactoryImpl (Map<String, ?> config, ConfigHandler handler)
	{
		m_config = config;
		m_handler = handler;
	}

	@Override
	public JsonParser createParser (Reader reader)
	{
		return m_handler.createParser (m_config, reader);
	}

	@Override
	public JsonParser createParser (InputStream is)
	{
		return m_handler.createParser (m_config, is);
	}

	@Override
	public JsonParser createParser (InputStream is, Charset charset)
	{
		return m_handler.createParser (m_config, is, charset);
	}

	@Override
	public JsonParser createParser (JsonObject obj)
	{
		return new JsonStructureParser (obj);
	}

	@Override
	public JsonParser createParser (JsonArray array)
	{
		return new JsonStructureParser (array);
	}

	@Override
	public Map<String, ?> getConfigInUse ()
	{
		return Collections.unmodifiableMap (m_config);
	}
}
