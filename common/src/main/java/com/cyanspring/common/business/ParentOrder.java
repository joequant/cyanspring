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
package com.cyanspring.common.business;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.type.ExchangeOrderType;
import com.cyanspring.common.type.OrdStatus;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.type.OrderType;
import com.cyanspring.common.type.StrategyState;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.common.util.PriceUtils;

public class ParentOrder extends Order {
	private static final Logger log = LoggerFactory
			.getLogger(ParentOrder.class);
	transient private int pos;
//	private OrderType orderType;
//	private String strategy;
//	private Date startTime;
//	private Date endTime;
//	private StrategyState state = StrategyState.Paused;
//	transient private HashMap<String, ChildOrder> activeChildOrders = new HashMap<String, ChildOrder>();
//	transient private HashMap<String, ChildOrder> archiveChildOrders = new HashMap<String, ChildOrder>();
	
	public ParentOrder(HashMap<String, Object> map) {
		super(map);
	}
	
	public ParentOrder(String symbol, OrderSide side, long quantity, double price, OrderType orderType) {
		super(symbol, side, quantity, price);
		put(OrderField.TYPE.value(), orderType);
	}

	@Override
	protected void init() {
		super.init();
		setState(StrategyState.Paused);
	}
	synchronized public void processExecution(Execution execution) {
		double cumQty = getCumQty();
		double avgPx = getAvgPx();
		avgPx = (avgPx * cumQty + execution.getPrice() * execution.getQuantity()) / (cumQty + execution.getQuantity());
		cumQty += execution.getQuantity();
		setCumQty(cumQty);
		setAvgPx(avgPx);

		log.debug("cumtQty, avgPx: " + cumQty + ", " + avgPx);
		touch();
		
		put(OrderField.LAST_SHARES.value(), execution.getQuantity());
		put(OrderField.LAST_PX.value(), execution.getPrice());
		if(getOrdStatus().isReady()) {
			if(PriceUtils.EqualGreaterThan(cumQty, getQuantity())) {
				setOrdStatus(OrdStatus.FILLED);
			} else {
				setOrdStatus(OrdStatus.PARTIALLY_FILLED);
			}
		}

	}

	@Override
	protected String generateId() {
		return IdGenerator.getInstance().getNextID() + "P";
	}
	
	public ParentOrder clone() {
		return (ParentOrder)super.clone();
	}

	public int getPos() {
		return pos;
	}

	public void setPos(int pos) {
		this.pos = pos;
	}
	
	public OrderType getOrderType() {
		return get(OrderType.class, OrderField.TYPE.value());
	}

	public String getStrategy() {
		return get(String.class, OrderField.STRATEGY.value());
	}
	
//	@Override
//	protected void pack() {
//		super.pack();
//		put(OrderField.TYPE.value(), orderType);
//		put(OrderField.STRATEGY.value(), strategy);
//		put(OrderField.START_TIME.value(), startTime);
//		put(OrderField.END_TIME.value(), endTime);
//		put(OrderField.STATE.value(), state);
//	}
//	
//	@Override
//	protected void unpack() {
//		super.unpack();
//		orderType = get(orderType, OrderType.class, OrderField.TYPE.value());
//		strategy = get(strategy, String.class, OrderField.STRATEGY.value());
//		startTime = get(startTime, Date.class, OrderField.START_TIME.value());
//		endTime = get(endTime, Date.class, OrderField.END_TIME.value());
//		state = get(state, StrategyState.class, OrderField.STATE.value());
//	}

	public StrategyState getState() {
		return get(StrategyState.class, OrderField.STATE.value());
	}

	public void setState(StrategyState state) {
		put(OrderField.STATE.value(), state);
	}
	
	public Date getStartTime() {
		return get(Date.class, OrderField.START_TIME.value());
	}

	public void setStartTime(Date startTime) {
		put(OrderField.START_TIME.value(), startTime);
	}

	public Date getEndTime() {
		return get(Date.class, OrderField.END_TIME.value());
	}

	public void setEndTime(Date endTime) {
		put(OrderField.END_TIME.value(), endTime);
	}

	public boolean priceInLimit(double price) {
		OrderType orderType = getOrderType();
		double myPrice = getPrice();
		if(orderType.equals(OrderType.Market) || PriceUtils.Equal(myPrice, 0))
			return true;
		
		if(getSide().isBuy()) {
			return PriceUtils.EqualLessThan(price, myPrice);
		} else {
			return PriceUtils.EqualGreaterThan(price, myPrice);
		}
		
	}
	
	public ChildOrder createChild(double quantity, double price, ExchangeOrderType type) {
		return new ChildOrder(getSymbol(), getSide(), quantity, price, type, getId(), getId());
	}
	
	public Map<String, Object> diff(ParentOrder order) {
		Map<String, Object> result = new HashMap<String, Object>();
		
		Map<String, Object> mine = getFields();
		Map<String, Object> other = order.getFields();
		
		for(Entry<String, Object> entry: mine.entrySet()) {
			Object value = other.get(entry.getKey());
			if(null == value) {	
				if(null != entry.getValue()) // fields are extra(as comparing to other order)
					result.put(entry.getKey(), value);
			} else if (!value.equals(entry.getValue())) { // fields are different
				result.put(entry.getKey(), value);
			}
		}
		
		// fields are missing(as comparing to other order)
		for(Entry<String, Object> entry: other.entrySet()) {
			Object value = mine.get(entry.getKey());
			if(null != entry.getValue() && null == value) {	
				result.put(entry.getKey(), entry.getValue());
			}
		}
		
		return result;
	}
}
