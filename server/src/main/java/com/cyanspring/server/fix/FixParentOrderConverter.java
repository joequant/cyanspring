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
package com.cyanspring.server.fix;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import quickfix.FieldNotFound;
import quickfix.field.AvgPx;
import quickfix.field.ClOrdID;
import quickfix.field.CumQty;
import quickfix.field.ExecID;
import quickfix.field.ExecTransType;
import quickfix.field.ExecType;
import quickfix.field.LastPx;
import quickfix.field.LastShares;
import quickfix.field.LeavesQty;
import quickfix.field.OrdStatus;
import quickfix.field.OrdType;
import quickfix.field.OrderID;
import quickfix.field.OrderQty;
import quickfix.field.OrigClOrdID;
import quickfix.field.Price;
import quickfix.field.Side;
import quickfix.field.Symbol;
import webcurve.util.PriceUtils;

import com.cyanspring.common.business.FieldDef;
import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;
import com.cyanspring.common.business.util.DataConvertException;
import com.cyanspring.common.business.util.GenericDataConverter;
import com.cyanspring.common.strategy.StrategyException;
import com.cyanspring.common.util.DualMap;
import com.cyanspring.common.util.IdGenerator;
import com.cyanspring.server.downstream.fix.FixConvertException;
import com.cyanspring.server.downstream.fix.FixUtils;
import com.cyanspring.common.strategy.IStrategyFactory;

public class FixParentOrderConverter {
	private static final Logger log = LoggerFactory
			.getLogger(FixParentOrderConverter.class);
	
	@Autowired
	private GenericDataConverter fixDataConverter;
	
	@Autowired
	IStrategyFactory strategyFactory;
	
	@Autowired
	@Qualifier("fixToOrderMap")
	private DualMap<Integer, String> fixToOrderMap;


	private void processCustomMap(quickfix.Message message, Map<String, Object> map) throws FieldNotFound, DataConvertException, FixConvertException, StrategyException {
		Integer strategyNameTag = fixToOrderMap.getKeyByValue(OrderField.STRATEGY.value());
		if(null == strategyNameTag)
			throw new FixConvertException("'Strategy' field is not defined in fixToOrderMap");
		
		if(!message.isSetField(strategyNameTag)) {
			throw new FixConvertException("Strategy field " + strategyNameTag + " is not presented in this message");
		}
		String strategyName = message.getString(strategyNameTag);
		 Map<String, FieldDef> fieldDefs = strategyFactory.getStrategyFieldDef(strategyName);
		
		for(Entry<Integer, String> entry: fixToOrderMap.entrySet()) {
			if(message.isSetField(entry.getKey())) {
				FieldDef fieldDef = fieldDefs.get(entry.getValue());
				if(null == fieldDef) {
					log.error("Field not defined in input fields: " + entry.getValue());
					continue;
				}
				
				Object obj = fixDataConverter.fromString(fieldDef.getType(), entry.getValue(), message.getString(entry.getKey()));
				map.put(entry.getValue(), obj);
			}
		}
	}
	
	public Map<String, Object> fixToParentOrder(quickfix.Message message) throws FieldNotFound, FixConvertException, DataConvertException, StrategyException {
		Map<String, Object> result = new HashMap<String, Object>();
		
		if(message.isSetField(new ClOrdID())) {
			result.put(OrderField.CLORDERID.value(), message.getField(new ClOrdID()).getValue());
		}

		if(message.isSetField(new Side())) {
			char side = message.getField(new Side()).getValue();
			result.put(OrderField.SIDE.value(), FixUtils.fromFixOrderSide(side));
		}
		
		if(message.isSetField(new OrdType())) {
			char type = message.getField(new OrdType()).getValue();
			result.put(OrderField.TYPE.value(), FixUtils.fromFixOrderType(type));
		}
		
		if(message.isSetField(new Symbol())) {
			String symbol = message.getField(new Symbol()).getValue();
			result.put(OrderField.SYMBOL.value(), symbol);
		}
		
		if(message.isSetField(new Price())) {
			result.put(OrderField.PRICE.value(), message.getField(new Price()).getValue());
		}

		if(message.isSetField(new OrderQty())) {
			result.put(OrderField.QUANTITY.value(), message.getField(new OrderQty()).getValue());
		}

//		this field will be transfered as txId
//		if(message.isSetField(new ClientID())) {
//			result.put(OrderField.CLIENTID.value(), message.getField(new ClientID()).getValue());
//		}

		processCustomMap(message, result);
		
		return result;
	}

	private void addExtraFields(quickfix.Message message, ParentOrder order) throws DataConvertException {
		for(Entry<Integer, String> entry: fixToOrderMap.entrySet()) {
			Map<String, Object> map = order.getFields();
			Object obj = map.get(entry.getValue());
			String str = fixDataConverter.toString(entry.getValue(), obj);
			message.setString(entry.getKey(), str);
		}
	}
	
	public quickfix.fix42.ExecutionReport parentOrderToFix(
			com.cyanspring.common.type.ExecType execType, ParentOrder order, String origClOrdID) throws FixConvertException {
		
		double leavesQty = 0;
		if(order.getOrdStatus().isCompleted())
			leavesQty = order.getQuantity() - order.getCumQty();
		quickfix.fix42.ExecutionReport er = new quickfix.fix42.ExecutionReport(
					new OrderID(order.getId()), 
					new ExecID(IdGenerator.getInstance().getNextID()),
					new ExecTransType(ExecTransType.NEW), 
					new ExecType(execType.value()), 
					new OrdStatus(order.getOrdStatus().value()), 
					new Symbol(order.getSymbol()), 
					new Side(FixUtils.toFixOrderSide(order.getSide())),
					new LeavesQty(leavesQty), 
					new CumQty(order.getCumQty()), 
					new AvgPx(order.getAvgPx())
			);

		String clOrdID = order.get(String.class, OrderField.CLORDERID.value());
		if(clOrdID != null)
			er.setField(new ClOrdID(clOrdID));
		
		if(origClOrdID != null)
			er.setField(new OrigClOrdID(origClOrdID));
		
		er.setField(new OrderQty(order.getQuantity()));
		
		if(!PriceUtils.Equal(order.getPrice(), 0))
			er.setField(new Price(order.getPrice()));
		
		if(execType == com.cyanspring.common.type.ExecType.PARTIALLY_FILLED ||
		   execType == com.cyanspring.common.type.ExecType.FILLED) {
			er.setField(new LastShares(order.get(new Double(0), Double.class, OrderField.LAST_SHARES.value())));
			er.setField(new LastPx(order.get(new Double(0), Double.class, OrderField.LAST_PX.value())));
		}
		
		try {
			addExtraFields(er, order);
		} catch (DataConvertException e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		}
		return er;
	}
	
	public GenericDataConverter getFixDataConverter() {
		return fixDataConverter;
	}
	
	public void setFixDataConverter(GenericDataConverter fixDataConverter) {
		this.fixDataConverter = fixDataConverter;
	}
	

}
