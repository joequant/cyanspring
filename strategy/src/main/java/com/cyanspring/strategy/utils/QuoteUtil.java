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
package com.cyanspring.strategy.utils;

import java.util.List;

import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.type.QtyPrice;
import com.cyanspring.common.util.PriceUtils;

public class QuoteUtil {
	
	public static boolean priceCanMatch(double quotePrice, double orderPrice, OrderSide side) {
		return side.isBuy() && PriceUtils.EqualGreaterThan(orderPrice, quotePrice) ||
			   side.isSell() && PriceUtils.EqualLessThan(orderPrice, quotePrice);
	}
	
	public static QtyPrice getLevelOneMatchingQtyPrice(Quote quote, double price, double quantity, OrderSide side) {
		QtyPrice qp = new QtyPrice(0, 0);
		if(side.isBuy() && PriceUtils.EqualGreaterThan(price, quote.getAsk())) {
			qp.setPrice(quote.getAsk());
			qp.setQuantity(Math.min(quantity, quote.getAskVol()));
		}
		if(side.isSell() && PriceUtils.EqualLessThan(price, quote.getBid())) {
			qp.setPrice(quote.getBid());
			qp.setQuantity(Math.min(quantity, quote.getBidVol()));
		}
		
		return qp; 
	}
	
	public static double getOppositeQuantityToPrice(Quote quote, double price, OrderSide side) {
		List<QtyPrice> list = side.isBuy()?quote.getAsks():quote.getBids();
		double result = 0;
		for(QtyPrice qp: list) {
			if((side.isBuy() && PriceUtils.GreaterThan(qp.price, price)) ||
			   (!side.isBuy() && PriceUtils.LessThan(qp.price, price)))
				break;
			
			result += qp.quantity;
		}
		return result;
	}
	
	// return the price at which the cum depth quantity can fill the quantity passed in
	// if the depth cum quantity can't full fill the quantity passed in, return 0
	public static double getOppositePriceToQuantity(Quote quote, double quantity, OrderSide side) {
		List<QtyPrice> list = side.isBuy()?quote.getAsks():quote.getBids();

		//if no level 2 data
		if(null == list || list.size() == 0 || PriceUtils.isZero(list.get(0).getQuantity())) {
			if(PriceUtils.EqualGreaterThan(side.isBuy()?quote.getAskVol():quote.getBidVol(), quantity))
				return side.isBuy()?quote.getAsk():quote.getBid();
			
			return 0;
		}

		for(QtyPrice qp: list) {
			quantity -= qp.getQuantity();
			if(PriceUtils.EqualLessThan(quantity, 0))
				return qp.price;
		}
		return 0;
	}
	
	// return the price at which the cum depth value can fill the quantity passed in
	// if the depth cum quantity can't full fill the quantity passed in, return 0
	public static double getOppositeValueToQuantity(Quote quote, double quantity, OrderSide side) {
		List<QtyPrice> list = side.isBuy()?quote.getAsks():quote.getBids();
		
		//if no level 2 data
		if(null == list || list.size() == 0 || PriceUtils.isZero(list.get(0).getQuantity())) {
			if(PriceUtils.EqualGreaterThan(side.isBuy()?quote.getAskVol():quote.getBidVol(), quantity))
				return (side.isBuy()?quote.getAsk():quote.getBid()) * quantity;
			
			return 0;
		}
		
		double result = 0;
		for(QtyPrice qp: list) {
			double residual = quantity;
			quantity -= qp.getQuantity();
			if(PriceUtils.EqualLessThan(quantity, 0)) {
				result += qp.getPrice() * residual;
				return result;
			} else {
				result += qp.getPrice() * qp.getQuantity();
			}
		}
		return 0;
	}

	public static boolean validateQuote(Quote quote) {
		if(PriceUtils.EqualLessThan(quote.getBid(), 0.0) && PriceUtils.EqualLessThan(quote.getAsk(), 0.0))
			return false;
		
		if(PriceUtils.EqualLessThan(quote.getBidVol(), 0.0) && PriceUtils.EqualLessThan(quote.getAskVol(), 0.0))
			return false;
		
		return true;
	}
}
