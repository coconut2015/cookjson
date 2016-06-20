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

import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;

import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;

/**
 * @author	Heng Yuan
 */
class JsonGeneratorFactoryImpl implements JsonGeneratorFactory
{
	private final Map<String, ?> m_config;
	private final ConfigHandler m_handler;

	public JsonGeneratorFactoryImpl (Map<String, ?> config, ConfigHandler handler)
	{
		m_config = config;
		m_handler = handler;
	}

	@Override
	public JsonGenerator createGenerator (Writer writer)
	{
		return m_handler.createGenerator (m_config, writer);
	}

	@Override
	public JsonGenerator createGenerator (OutputStream os)
	{
		return m_handler.createGenerator (m_config, os);
	}

	@Override
	public JsonGenerator createGenerator (OutputStream os, Charset charset)
	{
		return m_handler.createGenerator (m_config, os, charset);
	}

	@Override
	public Map<String, ?> getConfigInUse ()
	{
		return Collections.unmodifiableMap (m_config);
	}
}
