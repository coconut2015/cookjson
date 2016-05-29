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

/**
 * @author	Heng Yuan
 */
enum BsonSubType
{
	Generic (0),
	Function (1),
	@Deprecated
	Binary (2),
	@Deprecated
	UUID_old (3),
	UUID (4),
	MD5 (5),
	UserDefined (0x80);

	public byte type;
	private BsonSubType (int type)
	{
		this.type = (byte) type;
	}
}
