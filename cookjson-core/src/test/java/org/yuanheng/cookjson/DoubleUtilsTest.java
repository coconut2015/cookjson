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

import org.junit.Assert;
import org.junit.Test;

/**
 * @author	Heng Yuan
 */
public class DoubleUtilsTest
{
	@Test
	public void test ()
	{
		Assert.assertEquals (Double.toString (Double.NaN), DoubleUtils.toString (Double.NaN));
		Assert.assertEquals (Double.toString (Double.POSITIVE_INFINITY), DoubleUtils.toString (Double.POSITIVE_INFINITY));
		Assert.assertEquals (Double.toString (Double.NEGATIVE_INFINITY), DoubleUtils.toString (Double.NEGATIVE_INFINITY));
		Assert.assertEquals ("0", DoubleUtils.toString (0.0));
		Assert.assertEquals ("1", DoubleUtils.toString (1.0));
		Assert.assertEquals ("12", DoubleUtils.toString (12.0));
		Assert.assertEquals ("1234", DoubleUtils.toString (1234.0));
		Assert.assertEquals ("1234.1234", DoubleUtils.toString (1234.1234));
		Assert.assertEquals ("0.1234", DoubleUtils.toString (0.1234));
		Assert.assertEquals ("0.01234", DoubleUtils.toString (0.01234));
		Assert.assertEquals ("0.001234", DoubleUtils.toString (0.001234));
		Assert.assertEquals ("0.0001234", DoubleUtils.toString (0.0001234));
		Assert.assertEquals ("1.234e-5", DoubleUtils.toString (0.00001234));
		Assert.assertEquals ("1.1234", DoubleUtils.toString (1.1234));
		Assert.assertEquals ("5e-100", DoubleUtils.toString (5e-100));
		Assert.assertEquals ("5e100", DoubleUtils.toString (5e100));
		Assert.assertEquals ("12345678980123456", DoubleUtils.toString (12345678980123456L));
		Assert.assertEquals ("1.2345678980123456e17", DoubleUtils.toString (123456789801234567L));
		Assert.assertEquals ("-1", DoubleUtils.toString (-1.0));
		Assert.assertEquals ("-12", DoubleUtils.toString (-12.0));
		Assert.assertEquals ("-1234", DoubleUtils.toString (-1234.0));
		Assert.assertEquals ("-1234.1234", DoubleUtils.toString (-1234.1234));
		Assert.assertEquals ("-0.1234", DoubleUtils.toString (-0.1234));
		Assert.assertEquals ("-0.01234", DoubleUtils.toString (-0.01234));
		Assert.assertEquals ("-0.001234", DoubleUtils.toString (-0.001234));
		Assert.assertEquals ("-0.0001234", DoubleUtils.toString (-0.0001234));
		Assert.assertEquals ("-1.1234", DoubleUtils.toString (-1.1234));
		Assert.assertEquals ("-5e-100", DoubleUtils.toString (-5e-100));
		Assert.assertEquals ("-5e100", DoubleUtils.toString (-5e100));
		Assert.assertEquals ("-12345678980123456", DoubleUtils.toString (-12345678980123456L));
		Assert.assertEquals ("-1.2345678980123456e17", DoubleUtils.toString (-123456789801234567L));
		Assert.assertEquals (Integer.toString (Integer.MIN_VALUE), DoubleUtils.toString (Integer.MIN_VALUE));
		Assert.assertEquals (Integer.toString (Integer.MAX_VALUE), DoubleUtils.toString (Integer.MAX_VALUE));
		Assert.assertEquals ("1.7976931348623157e308", DoubleUtils.toString (Double.MAX_VALUE));
		Assert.assertEquals ("2.2250738585072014e-308", DoubleUtils.toString (Double.MIN_NORMAL));
		Assert.assertEquals ("-1.7976931348623157e308", DoubleUtils.toString (-Double.MAX_VALUE));
		Assert.assertEquals ("-2.2250738585072014e-308", DoubleUtils.toString (-Double.MIN_NORMAL));
		// it should be noted that 5e-324 is the same as 4.9e-324 in binary
		// representation
		Assert.assertEquals ("5e-324", DoubleUtils.toString (Double.MIN_VALUE));
	}
}
