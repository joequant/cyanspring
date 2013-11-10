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

import java.util.HashMap;

import com.cyanspring.common.type.OrdStatus;
import com.cyanspring.common.type.OrderSide;

public abstract class Order extends BaseOrder {

//	protected double cumQty; // quantity executed
//	protected double avgPx; // average price
//	protected OrdStatus ordStatus;
	
	@Override
	protected void init() {
		super.init();
		put(OrderField.ORDSTATUS.value(), OrdStatus.NEW);
		put(OrderField.CUMQTY.value(), 0.0);
		put(OrderField.AVGPX.value(), 0.0);
	}

	public Order(String symbol, OrderSide side, double quantity, double price) {
		super(symbol, side, quantity, price);
	}

	public Order(HashMap<String, Object> map) {
		super(map);
	}
	
	protected Order() {
		super();
	}
	
	@Override
	abstract protected String generateId();

	@Override
	protected String fieldsToString() {
		OrdStatus ordStatus = get(OrdStatus.class, OrderField.ORDSTATUS.value());
		Double cumQty = get(Double.TYPE, OrderField.CUMQTY.value());
		Double avgPx = get(Double.TYPE, OrderField.AVGPX.value());
		
		return super.fieldsToString() + ", "
		+ ordStatus + ", "
		+ cumQty + ", "
		+ avgPx;
	}

	public double getCumQty() {
		return get(Double.TYPE, OrderField.CUMQTY.value());
	}

	public double getAvgPx() {
		return get(Double.TYPE, OrderField.AVGPX.value());
	}

	public OrdStatus getOrdStatus() {
		return get(OrdStatus.class, OrderField.ORDSTATUS.value());
	}

	public double getRemainingQty() {
		return getQuantity() - getCumQty();
	}

	public void setOrdStatus(OrdStatus ordStatus) {
		put(OrderField.ORDSTATUS.value(), ordStatus);
	}

	public void setCumQty(double cumQty) {
		put(OrderField.CUMQTY.value(), cumQty);
	}

	public void setAvgPx(double avgPx) {
		put(OrderField.AVGPX.value(), avgPx);
	}
	
}
