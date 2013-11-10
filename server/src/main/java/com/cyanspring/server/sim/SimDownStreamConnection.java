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
package com.cyanspring.server.sim;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import webcurve.common.ExchangeListener;
import webcurve.exchange.Exchange;

import com.cyanspring.common.business.ChildOrder;
import com.cyanspring.common.business.Execution;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.downstream.DownStreamException;
import com.cyanspring.common.downstream.IDownStreamConnection;
import com.cyanspring.common.downstream.IDownStreamListener;
import com.cyanspring.common.downstream.IDownStreamSender;
import com.cyanspring.common.type.ExchangeOrderType;
import com.cyanspring.common.type.ExecType;
import com.cyanspring.common.type.OrdStatus;
import com.cyanspring.common.type.OrderSide;

public class SimDownStreamConnection implements IDownStreamConnection {
	private static final Logger log = LoggerFactory
			.getLogger(SimDownStreamConnection.class);
	private static AtomicInteger nextId = new AtomicInteger(0);
	private Exchange exchannge;
	private String id;
	private IDownStreamListener listener;
	private static final String broker = "algo";
	private boolean sendPendingAck;
	private Map<String, ChildOrder> uOrders = Collections.synchronizedMap(new HashMap<String, ChildOrder>());
	private Map<String, webcurve.common.Order> dOrders = Collections.synchronizedMap(new HashMap<String, webcurve.common.Order>());
	
	public SimDownStreamConnection(Exchange exchange) {
		this.exchannge = exchange;
		this.id = ""+nextId.incrementAndGet();
		exchange.orderListenerKeeper.addExchangeListener(orderListener);
		exchange.tradeListenerKeeper.addExchangeListener(tradeListener);
	}
	
	ExchangeListener<webcurve.common.Order> orderListener = new ExchangeListener<webcurve.common.Order>() {
		@Override
		public void onChangeEvent(webcurve.common.Order order) {
			ChildOrder uOrder = uOrders.get(order.getClOrderId());
			if(uOrder == null) // not my order
				return;
			uOrder.touch();
			uOrders.put(uOrder.getId(), uOrder);
			if(order.getStatus().equals(webcurve.common.Order.STATUS.NEW)) {
				uOrder.setOrdStatus(OrdStatus.NEW);
				dOrders.put(uOrder.getId(), order);
				listener.onOrder(ExecType.NEW, uOrder, null, null);
			} else if(order.getStatus().equals(webcurve.common.Order.STATUS.AMENDED)) {
				uOrder.setOrdStatus(OrdStatus.REPLACED);
				uOrder.setQuantity(order.getQuantity() + order.getCumQty());
				uOrder.setPrice(order.getPrice());
				listener.onOrder(ExecType.REPLACE, uOrder, null, null);
			} else if(order.getStatus().equals(webcurve.common.Order.STATUS.CANCELLED)) {
				uOrder.setOrdStatus(OrdStatus.CANCELED);
				uOrders.remove(uOrder.getId());
				dOrders.remove(uOrder.getId());
				listener.onOrder(ExecType.CANCELED, uOrder, null, null);
			} else if(order.getStatus().equals(webcurve.common.Order.STATUS.REJECTED)) {
				uOrder.setOrdStatus(OrdStatus.REJECTED);
				listener.onOrder(ExecType.REJECTED, uOrder, null, null);
			} else if(order.getStatus().equals(webcurve.common.Order.STATUS.FILLING)) {
				uOrder.setOrdStatus(OrdStatus.PARTIALLY_FILLED);
				// trade listener will do the update
			} else if(order.getStatus().equals(webcurve.common.Order.STATUS.DONE)) {
				uOrder.setOrdStatus(OrdStatus.FILLED);
				// trade listener will do the update
			} else {
				log.error("unhandled status: " + order.getStatus());
			}
		}
	};

	private void updateOrder(webcurve.common.Order myOrder, webcurve.common.Trade trade) {
		ChildOrder uOrder = uOrders.get(myOrder.getClOrderId());
		uOrder = uOrder.clone();
		uOrders.put(uOrder.getId(), uOrder);

		uOrder.setCumQty(myOrder.getCumQty());
		uOrder.setAvgPx(myOrder.getAvgPrice());
		uOrder.touch();
		
		ExecType execType = ExecType.PARTIALLY_FILLED;
		if(myOrder.getStatus().equals(webcurve.common.Order.STATUS.DONE)) {
			uOrders.remove(uOrder.getId());
			dOrders.remove(uOrder.getId());
			execType = ExecType.FILLED;
		} else if (myOrder.getStatus().equals(webcurve.common.Order.STATUS.FILLING)) {
			execType = ExecType.PARTIALLY_FILLED;
		} else {
			log.error("Trade without proper status type: " + execType);
			return;
		}
		Execution execution = new Execution(uOrder.getSymbol(), uOrder.getSide(), trade.getQuantity(), 
				trade.getPrice(), uOrder.getId(), uOrder.getParentOrderId(), uOrder.getStrategyId(), ""+trade.getTradeID());
		uOrder.setOrdStatus(OrdStatus.getStatus(execType.value()));
		listener.onOrder(execType, uOrder, execution, null);
	}
	
	ExchangeListener<webcurve.common.Trade> tradeListener = new ExchangeListener<webcurve.common.Trade>() {
		@Override
		public void onChangeEvent(webcurve.common.Trade trade) {
			webcurve.common.Order order;
			order = trade.getBidOrder();
			if(dOrders.containsKey(order.getClOrderId()))
				updateOrder(order, trade);
			
			order = trade.getAskOrder();
			if(dOrders.containsKey(order.getClOrderId()))
				updateOrder(order, trade);
		}
	};
	
	private webcurve.common.Order.TYPE mapOrderType(ExchangeOrderType type) throws DownStreamException {
		if(type.equals(ExchangeOrderType.MARKET))
			return webcurve.common.Order.TYPE.MARKET;
		if(type.equals(ExchangeOrderType.LIMIT))
			return webcurve.common.Order.TYPE.LIMIT;
		if(type.equals(ExchangeOrderType.IOC))
			return webcurve.common.Order.TYPE.FAK;
		else
			throw new DownStreamException("ExchangeOrderType not supported by simulator: " + type);
	}
	
	private webcurve.common.BaseOrder.SIDE mapSide(OrderSide side) {
		if(side.equals(OrderSide.Buy))
			return webcurve.common.BaseOrder.SIDE.BID;
		else
			return webcurve.common.BaseOrder.SIDE.ASK;
	}
	
	private IDownStreamSender sender = new IDownStreamSender() {
		
		@Override
		public void newOrder(ChildOrder order) throws DownStreamException {
			
			uOrders.put(order.getId(), order);
			if(sendPendingAck) {
				order.setOrdStatus(OrdStatus.PENDING_NEW);
				listener.onOrder(ExecType.PENDING_NEW, order, null, "");
			}
			
			webcurve.common.Order exOrder = SimDownStreamConnection.this.exchannge.enterOrder(order.getSymbol(), 
					mapOrderType(order.getType()), mapSide(order.getSide()), 
					(int)order.getQuantity(), order.getPrice(), broker, order.getId());
			
			if(exOrder == null) {
				order.setOrdStatus(OrdStatus.REJECTED);
				listener.onOrder(ExecType.REJECTED, order, null, "price or quantity 0");
			}
		}

		@Override
		public void amendOrder(ChildOrder order, Map<String, Object> fields)
				throws DownStreamException {
			ChildOrder uOrder = uOrders.get(order.getId());
			if(null == uOrder) {
				listener.onOrder(ExecType.REJECTED, order, null, "Can't find child order in downstream connection");
				return;
			}
			
			webcurve.common.Order dOrder = dOrders.get(order.getId());
			if(null == dOrder) {
				listener.onOrder(ExecType.REJECTED, order, null, "Can't find exchange order in downstream connection");
				return;
			}
				
			int qty = 0;
			double price = 0;
			try {
				Object oQty = fields.get(OrderField.QUANTITY.value());
				if(oQty != null) {
					qty = (int)((Double)oQty).doubleValue();
					qty -= order.getCumQty();
				}
				
				Object oPrice = fields.get(OrderField.PRICE.value());
				if(oPrice != null)
					price = (Double)oPrice;
				
				if(sendPendingAck) {
					order.setOrdStatus(OrdStatus.PENDING_REPLACE);
					listener.onOrder(ExecType.PENDING_REPLACE, order, null, "");
				}

			} catch (Exception e) {
				throw new DownStreamException(e.getMessage());
			}
			if (!SimDownStreamConnection.this.exchannge.amendOrder(dOrder.getOrderID(), order.getSymbol(), 
					dOrder.getSide(), qty, price, order.getId())){
				listener.onOrder(ExecType.REJECTED, order, null, "Exchange amend failed: " + order.getId());
			}		

		}

		@Override
		public void cancelOrder(ChildOrder order) throws DownStreamException {
			ChildOrder uOrder = uOrders.get(order.getId());
			if(null == uOrder) {
				listener.onOrder(ExecType.REJECTED, order, null, "Can't find child order in downstream connection");
				return;
				
			}
			
			webcurve.common.Order dOrder = dOrders.get(order.getId());
			if(null == dOrder) {
				listener.onOrder(ExecType.REJECTED, order, null, "Can't find exchange order in downstream connection");
				return;
			}
			
			if(sendPendingAck) {
				order.setOrdStatus(OrdStatus.PENDING_CANCEL);
				listener.onOrder(ExecType.PENDING_CANCEL, order, null, "");
			}

			if (!SimDownStreamConnection.this.exchannge.cancelOrder(dOrder.getOrderID(), order.getSymbol(), dOrder.getSide(), order.getId())) {
				listener.onOrder(ExecType.REJECTED, order, null, "Exchange cancel failed: " + order.getId());
			}		
			
		}

		@Override
		public boolean getState() {
			return true;
		}
		
	};
	
	@Override
	public IDownStreamSender setListener(IDownStreamListener listener) {
		this.listener = listener;
		if(null != listener)
			listener.onState(true);
		return sender;
	}
	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public boolean getState() {
		return true;
	}

	@Override
	public void init() {
	}

	@Override
	public void uninit() {
	}


}
