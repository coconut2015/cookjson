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

import javax.json.stream.JsonGenerationException;
import javax.json.stream.JsonGenerator;

/**
 * Add two binary related functions not present in JsonGenerator.
 *
 * @author	Heng Yuan
 */
public interface CookJsonGenerator extends JsonGenerator
{
	/**
	 * Writes the specified data as a binary value within the current
	 * object context.  Depends on the implementation, it can be written
	 * as binary, or as Base64 / Hexadecimal strings.
	 *
	 * @param	name
	 *			the key name.
	 * @param	value
	 *			a binary data to be written
	 * @return	this.
	 * @throws	JsonGenerationException
	 *			if this method is not called within an array context
	 * @throws	javax.json.JsonException
	 *			in case of other errors (such as I/O).
	 * @see org.yuanheng.cookjson.value.CookJsonBinary
	 */
	public JsonGenerator write (String name, byte[] value);

	/**
	 * Writes the specified data as a binary value within the current
	 * array context.  Depends on the implementation, it can be written
	 * as binary, or as Base64 / Hexadecimal strings.
	 *
	 * @param	value
	 *			a binary data to be written
	 * @return	this.
	 * @throws	JsonGenerationException
	 *			if this method is not called within an array context
	 * @throws	javax.json.JsonException
	 *			in case of other errors (such as I/O).
	 * @see org.yuanheng.cookjson.value.CookJsonBinary
	 */
	public JsonGenerator write (byte[] value);
}
