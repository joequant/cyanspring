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
package com.cyanspring.common.marketdata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Trade implements Cloneable{
	private static final Logger log = LoggerFactory
			.getLogger(Trade.class);
	String id;
	String symbol;
	double price;
	double quantity;
	boolean buyDriven;
	
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public double getQuantity() {
		return quantity;
	}
	public void setQuantity(double quantity) {
		this.quantity = quantity;
	}
	public boolean isBuyDriven() {
		return buyDriven;
	}
	public void setBuyDriven(boolean buyDriven) {
		this.buyDriven = buyDriven;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	@Override
	public Trade clone() {
		try {
			return (Trade)super.clone();
		} catch (CloneNotSupportedException e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		}
		return null;
	}
}
