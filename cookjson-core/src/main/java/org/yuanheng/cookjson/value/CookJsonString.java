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
package org.yuanheng.cookjson.value;

import javax.json.JsonString;

/**
 * @author	Heng Yuan
 */
public class CookJsonString implements JsonString
{
	private String m_value;

	public CookJsonString (String value)
	{
		m_value = value;
	}

	@Override
	public ValueType getValueType ()
	{
		return ValueType.STRING;
	}

	@Override
	public String getString ()
	{
		return m_value;
	}

	@Override
	public CharSequence getChars ()
	{
		return m_value;
	}

	@Override
	public int hashCode ()
	{
		return m_value.hashCode ();
	}

	@Override
	public String toString ()
	{
		return m_value;
	}
}
