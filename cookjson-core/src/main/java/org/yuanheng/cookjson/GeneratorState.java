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

/**
 * @author	Heng Yuan
 */
class GeneratorState
{
	/** Data generation has ended. */
	public final static int END = -1;
	/** Initial state. */
	public final static int INITIAL = 0;

	/** In array and started working on the first element. */
	public final static int IN_ARRAY = 1;
	/** In object and started working on the first element. */
	public final static int IN_OBJECT = 2;
}
