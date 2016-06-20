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

import java.util.Collections;
import java.util.Map;

import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;

/**
 * @author	Heng Yuan
 */
class JsonBuilderFactoryImpl implements JsonBuilderFactory
{
	private final Map<String, ?> m_config;

	public JsonBuilderFactoryImpl (Map<String, ?> config)
	{
		m_config = config;
	}

	@Override
	public JsonObjectBuilder createObjectBuilder ()
	{
		return new JsonObjectBuilderImpl ();
	}

	@Override
	public JsonArrayBuilder createArrayBuilder ()
	{
		return new JsonArrayBuilderImpl ();
	}

	@Override
	public Map<String, ?> getConfigInUse ()
	{
		return Collections.unmodifiableMap (m_config);
	}
}
