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
package com.cyanspring.cstw.business;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.business.OrderField;
import com.cyanspring.common.business.ParentOrder;

public class SingleOrderStrategyCache {
	private static final Logger log = LoggerFactory.getLogger(SingleOrderStrategyCache.class);
	private Map<String, ParentOrder> parentOrders = new HashMap<String, ParentOrder>();
	private List<Map<String, Object>> innerOrders = new ArrayList<Map<String, Object>>();

	synchronized public void update(ParentOrder order) {
		ParentOrder existing = parentOrders.get(order.getId());
		if (null != existing ) {
			if (order.getSeqId().compareTo(existing.getSeqId()) >= 0) {
				order.setPos(existing.getPos());
				parentOrders.put(order.getId(), order);
				innerOrders.set(order.getPos(), order.getFields());
			} else {
				log.warn("Dropping parent order (" + order.getId() + ") update due to sequence id (" 
						+ order.getSeqId() + ")less than current (" + existing.getSeqId() + ")");
			}
		} else {
			order.setPos(innerOrders.size());
			parentOrders.put(order.getId(), order);
			innerOrders.add(order.getFields());
		}
	}
	
	synchronized public void clearOrders(String server) {
		// remove orders from this server from parentOrders
		boolean cleared = false;
		for(Map<String, Object> map: innerOrders) {
			String name = (String)map.get(OrderField.SERVER_ID.value());
			if(name.equals(server)) {
				String key = (String) map.get(OrderField.ID.value());
				parentOrders.remove(key);
				log.debug("Removed order: " + key);
				cleared = true;
			}
		}
		
		// clear innerOrders then re-set pos in order
		if(cleared) {
			innerOrders.clear();
			for(ParentOrder order: parentOrders.values()) {
				order.setPos(innerOrders.size());
				innerOrders.add(order.getFields());
			}
		}
	}
	synchronized public List<Map<String, Object>> getOrders() {
		return innerOrders;
	}
	
	synchronized public ParentOrder getParentOrder(String id) {
		return parentOrders.get(id);
	}
	
	synchronized public void clear() {
		parentOrders.clear();
		innerOrders.clear();
	}
}
