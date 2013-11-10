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

import com.cyanspring.common.Clock;
import com.cyanspring.common.data.DataObject;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.util.IdGenerator;

public abstract class BaseOrder extends DataObject {
	
	abstract protected String generateId();
	
	public Map<String, Object> update(Map<String, Object> map) {
		Map<String, Object> result = new HashMap<String, Object>();
		for(Entry<String, Object> entry: map.entrySet()) {
			result.put(entry.getKey(), put(entry.getKey(), entry.getValue()));
		}
		this.touch();
		return result;
	}

	protected BaseOrder() {
		super();
	}
	
	protected void init() {
		Date now = Clock.getInstance().now();
		put(OrderField.CREATED.value(), now);
		put(OrderField.MODIFIED.value(), now);
		put(OrderField.ID.value(), generateId());
		put(OrderField.SEQ_ID.value(), IdGenerator.getInstance().getNextID());
		put(OrderField.SERVER_ID.value(), IdGenerator.getInstance().getSystemId());
	}
	
	public BaseOrder(HashMap<String, Object> map) {
		super(map);
		init();
	}
	
	public BaseOrder(String symbol, OrderSide side, double quantity, double price)
	{
		init();
		put(OrderField.SYMBOL.value(), symbol);
		put(OrderField.SIDE.value(), side);
		put(OrderField.QUANTITY.value(), quantity);
		put(OrderField.PRICE.value(), price);
	}
	
	public Date getModified() {
		return get(Date.class, OrderField.MODIFIED.value());
	}

	public Date getCreated() {
		return get(Date.class, OrderField.CREATED.value());
	}

	public String getId() {
		return get(String.class, OrderField.ID.value());
	}

	public OrderSide getSide() {
		return get(OrderSide.class, OrderField.SIDE.value());
	}

	public double getPrice() {
		return get(Double.TYPE, OrderField.PRICE.value());
	}

	public void setPrice(double price) {
		put(OrderField.PRICE.value(), price);
	}

	public double getQuantity() {
		return get(Double.TYPE, OrderField.QUANTITY.value());
	}

	public void setQuantity(double quantity) {
		put(OrderField.QUANTITY.value(), quantity);
	}

	public String getSymbol() {
		return get(String.class, OrderField.SYMBOL.value());
	}
	
	public String getServerId() {
		return get(String.class, OrderField.SERVER_ID.value());
	}

	protected void setId(String id) {
		put(OrderField.ID.value(), id);
	}
	
	protected void setSymbol(String symbol) {
		put(OrderField.SYMBOL.value(), symbol);
	}

	public void setModified(Date modified) {
		put(OrderField.MODIFIED.value(), modified);
	}
	
	protected void setCreated(Date created) {
		put(OrderField.CREATED.value(), created);
	}

	protected void setSide(OrderSide side) {
		put(OrderField.SIDE.value(), side);
	}
	
	protected void setSeqId(String seqId) {
		put(OrderField.SEQ_ID.value(), seqId);
	}
	
	protected void setServerId(String systemId) {
		put(OrderField.SERVER_ID.value(), systemId);
	}
	
	protected String fieldsToString() {
		String id = get(String.class, OrderField.ID.value());
		String symbol = get(String.class, OrderField.SYMBOL.value());
		OrderSide side = get(OrderSide.class, OrderField.SIDE.value());
		double quantity = get(Double.TYPE, OrderField.QUANTITY.value());
		double price = get(Double.TYPE, OrderField.PRICE.value());
		
		return id + ", "
		+ symbol + ", "
		+ side!=null?side.toString():"Two way" + ", "
		+ price + ", "
		+ quantity
		;
	}
	
	@Override
	public String toString() {
		return "[" + fieldsToString() + "]";
	}

	public Date getTimeModified() {
		return get(Date.class, OrderField.MODIFIED.value());
	}

	public String getSeqId() {
		return get(String.class, OrderField.SEQ_ID.value());
	}

	public void touch() {
		put(OrderField.MODIFIED.value(), Clock.getInstance().now());
		put(OrderField.SEQ_ID.value(), IdGenerator.getInstance().getNextID());
	}
	
}
