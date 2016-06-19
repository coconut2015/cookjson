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
 * See http://bsonspec.org/spec.html for spec.
 * @author	Heng Yuan
 */
class BsonType
{
	public final static int Double = 1;
	public final static int String = 2;
	public final static int Document = 3;
	public final static int Array = 4;
	public final static int Binary = 5;
	@Deprecated
	public final static int Undefined = 6;
	public final static int ObjectId = 7;
	public final static int Boolean = 8;
	public final static int DateTime = 9;
	public final static int Null = 0x0a;
	public final static int RegEx = 0x0b;
	@Deprecated
	public final static int DBPointer = 0x0c;
	public final static int JavaScript = 0x0d;
	public final static int Deprecated = 0x0e;
	public final static int JavaScriptScope = 0x0f;
	public final static int Integer = 0x10;
	public final static int TimeStamp = 0x11;
	public final static int Long = 0x12;

	public final static int MinKey = 0xff;
	public final static int MaxKey = 0x7f;
}
