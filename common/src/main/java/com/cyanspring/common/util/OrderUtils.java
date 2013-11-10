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


import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.cyanspring.common.business.ChildOrder;
import com.cyanspring.common.marketdata.Quote;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.type.QtyPrice;

public class OrderUtils {
	public static boolean inLimit(double price, double limit, OrderSide side) {
		if(PriceUtils.Equal(limit, 0))
			return true;
		
		if(side.equals(OrderSide.Buy)) {
			return PriceUtils.EqualLessThan(price, limit);
		} else {
			return PriceUtils.EqualGreaterThan(price, limit);
		}
	}
	
	public static final Comparator<ChildOrder> childOrderComparator =
		new Comparator<ChildOrder>() {
			 public int compare(ChildOrder x, ChildOrder y) {
				 int result = PriceUtils.CompareBySide(x.getSide(), x.getPrice(), y.getPrice());
				 if (result == 0) {
					 result = x.getCreated().compareTo(y.getCreated());
					 if (result == 0)
						 result = x.getId().compareTo(y.getId());
				 }
				 return result;
			 }
		};

	
	public static Set<ChildOrder> getSortedOpenChildOrders(Collection<ChildOrder> childOrders) {
		Set<ChildOrder> set = new TreeSet<ChildOrder>(childOrderComparator);
		for (ChildOrder order: childOrders) {
			set.add(order);
		}
		return set;
	}

	
	public static Quote calAdjustedQuote(Quote quote, Set<ChildOrder> orders) {
		Quote adjQuote = (Quote)quote.clone();

		if(orders.size() == 0)
			return adjQuote;
		
		Iterator<ChildOrder> it = orders.iterator();
		ChildOrder order = it.next();
		OrderSide side = order.getSide();
		
		List<QtyPrice> list; 
		if(side.equals(OrderSide.Buy)) 
			list = adjQuote.getBids();
		else
			list = adjQuote.getAsks();
		
		if(list == null)
			return null;
		
		for(ChildOrder child: orders) {
			for(QtyPrice qp: list) {
				if(PriceUtils.Equal(child.getPrice(), qp.getPrice())) {
					qp.setQuantity(qp.getQuantity() - child.getRemainingQty());
					if(PriceUtils.LessThan(qp.getQuantity(), 0)) //market data not yet reflected your orders
						return null;
				} else if (side.equals(OrderSide.Buy)) {
					if(PriceUtils.LessThan(qp.getPrice(), child.getPrice())) {
						break;
					}
				} else {
					if(PriceUtils.GreaterThan(qp.getPrice(), child.getPrice())) {
						break;
					}
				}
			}
		}

		QtyPrice top = null;
		while(list.size()>0) {
			top = list.get(0);
			if(PriceUtils.Equal(top.getQuantity(), 0)) {
				list.remove(0);
			} else {
				break;
			}
		}
		
		if(top != null) {
			if(side.equals(OrderSide.Buy)) {
				adjQuote.setBid(top.getPrice());
				adjQuote.setBidVol(top.getQuantity());
			} else{
				adjQuote.setAsk(top.getPrice());
				adjQuote.setAskVol(top.getQuantity());
			}
		}
		
		return adjQuote;
	}
	
	static public boolean isBetterPrice(OrderSide side, double p1, double p2) {
		if(PriceUtils.Equal(p2, 0)) // consider as no limit
			return true;
		if(side.isBuy()) {
			return PriceUtils.EqualLessThan(p1, p2);
		} else {
			return PriceUtils.EqualGreaterThan(p1, p2);
		}
	}
}
