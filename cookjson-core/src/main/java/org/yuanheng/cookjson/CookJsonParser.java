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

import javax.json.JsonValue;
import javax.json.stream.JsonParser.Event;

/**
 * @author	Heng Yuan
 */
public interface CookJsonParser extends javax.json.stream.JsonParser
{
	/**
	 * Gets the current event.
	 *
	 * @return	the current event.
	 */
	public Event getEvent ();

	/**
	 * Based on the current event, retrieve the JsonValue.
	 * <p>
	 * In case of START_OBJECT and START_ARRAY, JsonObject and JsonArray
	 * objects are returned.  This feature is to allow the mixing of
	 * streaming and model based APIs.
	 *
	 * @return	the JsonValue associated with the current event.
	 */
	public JsonValue getValue ();

	/**
     * Returns true if the current JSON value at the current parser state is
     * actually a binary data.
     *
     * @return	true if the String value is actually a binary.
     * @throws	IllegalStateException
     *			if the current parser state is not {@code VALUE_STRING}
	 */
	public boolean isBinary ();

	/**
     * Returns the {@code byte[]} value if the parser state is
     * {@link Event#VALUE_STRING} and the data is actually a binary data.
     *
     * @return	the binary value.
     * @throws	IllegalStateException
     *			if the current parser state is not {@code VALUE_STRING}
     *			or the current value is not binary.
	 */
	public byte[] getBytes ();
}
