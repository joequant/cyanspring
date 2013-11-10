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

public enum OrderField {
		ID("id"),
		SYMBOL("Symbol"),
		DESC("Desc"),
		SIDE("Side"),
		PRICE("Price"),
		QUANTITY("Qty"),
		CREATED("Created"),
		MODIFIED("Modified"),
		TYPE("Type"),
		CUMQTY("CumQty"),
		AVGPX("AvgPx"),
		SERVER_ID("Server Id"),
		STRATEGY("Strategy"),
		STRATEGY_ID("Strategy Id"),
		START_TIME("Start Time"),
		END_TIME("End Time"),
		ORDER_ID("Order ID"), //the child order id for execution
		PARENT_ORDER_ID("Parent Order ID"),
		POV("POV"),
		POV_LIMIT("POV limit"),
		DISPLAY_QUANTITY("Dis Qty"),
		SEQ_ID("Seq ID"),
		ORDSTATUS("Status"),
		CLORDERID("ClOrderId"),
		EXECID("ExecId"),
		STATE("State"),
		CON_ID("CondId"),
		LEAVES_QTY("LeavesQty"),
		SOURCE("Source"),
		CLIENTID("ClientID"),
		LAST_SHARES("LastShares"),
		LAST_PX("LastPx"),
		IS_FIX("isFIX"),
		POSITION("Position"),
		POS_AVGPX("Pos Px"),
		POS_VALUE("Pos Value"),
		PNL("P/L"),
		ALERT_TYPE("Alert"),
		ALERT_MSG("Alert msg"),
		NOTE("Note"),
		AHIGH("AH"), // all time high
		ALOW("AL"),	 // all time low
		LAST("Last"),	 // last price
		CHANGE("Chg"),
		CHANGE_PERCENT("Chg%"),
		;
		
		static HashMap<String, OrderField> map = new HashMap<String, OrderField>();
		
		private String value;
		OrderField(String value) {
			this.value = value;
		}
		public String value() {
			return value;
		}
		
		static public OrderField getValue(String str) {
			return map.get(str);
		}

		public static void validate() throws Exception {
			map.clear();
			for (OrderField field: OrderField.values()) {
				if (map.containsKey(field.value()))
					throw new Exception("OrderField duplicated: " + field.value);
				else
					map.put(field.value(), field);
			}
			
		}

}
