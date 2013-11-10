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

import java.util.Map;
import java.util.Map.Entry;

import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.strategy.PriceAllocation;
import com.cyanspring.common.type.ExchangeOrderType;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.util.IdGenerator;

public class Instrument extends DataObject {
	
	public void update(Map<String, Object> map) {
		for(Entry<String, Object> entry: map.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}
	
	public Instrument(String symbol) {
		this(symbol, 0, 0, 0);
	}
	
	public Instrument(String symbol, double position, double posAvgPx, double pnl) {
		put(OrderField.SYMBOL.value(), symbol);
		put(OrderField.POSITION.value(), position);
		put(OrderField.POS_AVGPX.value(), posAvgPx);
		put(OrderField.PNL.value(), pnl);
		String id = IdGenerator.getInstance().getNextID() + "P";
		put(OrderField.ID.value(), id);
		put(OrderField.SERVER_ID.value(), IdGenerator.getInstance().getSystemId());
	}
	
	public String getId() {
		return get(String.class, OrderField.ID.value());
	}
	
	public String getSymbol() {
		return get(String.class, OrderField.SYMBOL.value());
	}
	
	public String getStrategyId() {
		return get(String.class, OrderField.STRATEGY_ID.value());
	}

	public double getPosition() {
		return get(Double.TYPE, OrderField.POSITION.value());
	}

	public double getPosAvgPx() {
		return get(Double.TYPE, OrderField.POS_AVGPX.value());
	}

	public double getPosValue() {
		return getPosition() * getPosAvgPx();
	}

	public double getPnl() {
		return get(Double.TYPE, OrderField.PNL.value());
	}

	void setPosition(double position) {
		put(OrderField.POSITION.value(), position);
	}

	void setPosAvgPx(double posAvgPx) {
		put(OrderField.POS_AVGPX.value(), posAvgPx);
	}

	void setPnl(double pnl) {
		put(OrderField.PNL.value(), pnl);
	}

	public void processExecution(Execution execution) {
		double qty = execution.getSide().isBuy()? execution.getQuantity() : -execution.getQuantity();
		double oldPos = getPosition();
		double newPos = oldPos + qty;
		double oldAvgPx = getPosAvgPx();
		double newAvgPx = oldAvgPx;
		double oldPnl = getPnl();
		double newPnl = oldPnl; 

		if(Math.abs(oldPos) > Math.abs(newPos)) {// only calculate P&L when position is reduced
			newPnl += qty * (oldAvgPx - execution.getPrice());
		} else { // only calculate avgPx when position is increased
			newAvgPx = (oldPos * oldAvgPx + qty * execution.getPrice()) / newPos;
		}
		put(OrderField.POS_VALUE.value(), newPos * newAvgPx);
		setPosition(newPos);
		setPosAvgPx(newAvgPx);
		setPnl(newPnl);
	}

	public PriceAllocation createPriceAllocation(OrderSide side, double price, double qty, ExchangeOrderType orderType) {
		return new PriceAllocation(this.getSymbol(), side, price, qty, orderType, this.getId());
	}
	
	public ChildOrder createChildOrder(OrderSide side, 
			double quantity, double price, ExchangeOrderType type) {
		return new ChildOrder(this.getSymbol(), side, quantity, price, type, this.getId(), this.getId());
	}
	
}
