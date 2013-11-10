/*******************************************************************************
 * Copyright (c) 2011-2012 Cyan Spring Limited
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms specified by license file attached.
 * 
 * Software distributed under the License is released on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/
package com.cyanspring.common.util;

import com.cyanspring.common.type.OrderSide;

public class PriceUtils {
	private static final int scale = 7;
	private static final double roundingFactor = Math.pow(10, (double) scale);
	private static final double EPSILON = 1.0 / roundingFactor;

	static public boolean Equal(double x, double y) {
		return Math.abs(x - y) < EPSILON;
	}

	static public boolean GreaterThan(double x, double y) {
		return x - y > EPSILON;
	}

	static public boolean LessThan(double x, double y) {
		return y - x > EPSILON;
	}

	static public boolean EqualGreaterThan(double x, double y) {
		return Equal(x, y) || GreaterThan(x, y);
	}

	static public boolean EqualLessThan(double x, double y) {
		return Equal(x, y) || LessThan(x, y);
	}

	static public int Compare(double x, double y) {
		if (Equal(x, y))
			return 0;

		if (GreaterThan(x, y))
			return 1;
		else
			return -1;
	}

	// sort by ascending, least priority order coming after
	static public int CompareBySide(OrderSide side, double x, double y) {
		 if (side == OrderSide.Buy)
			 return PriceUtils.Compare(y, x);
		 else
			 return PriceUtils.Compare(x, y);
	}

	// x in the limit of y price
	static public boolean inLimit(double x, double y, OrderSide side) {
		if (side == OrderSide.Buy)
			return EqualLessThan(x, y);
		else
			return EqualGreaterThan(x, y);
	}

	static public boolean validPrice(double price) {
		return GreaterThan(price, 0);
	}

	static public boolean isZero(double x) {
		return PriceUtils.Equal(x, 0);
	}
}
