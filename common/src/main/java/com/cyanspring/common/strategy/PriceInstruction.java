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
package com.cyanspring.common.strategy;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.common.strategy.PriceAllocation;


/**
 * @author 
 *
 */
public class PriceInstruction {
	Map<String, TreeSet<PriceAllocation>> allocations = new HashMap<String, TreeSet<PriceAllocation>>();

	final Comparator<PriceAllocation> comparator = new Comparator<PriceAllocation>() {

		@Override
		public int compare(PriceAllocation o1, PriceAllocation o2) {
			if (o1.getSymbol().equals(o2.getSymbol())) {
				if(o1.getSide().equals(o2.getSide())) {
					if (o1.getOrderType().equals(o2.getOrderType())) {
						return PriceUtils.CompareBySide(o1.getSide(), o1.getPrice(), o2.getPrice());
					} else {
						return o1.getOrderType().compareTo(o2.getOrderType());
					}
				} else {
					return o1.getSide().compareTo(o2.getSide());
				}
			} else {
				return o1.getSymbol().compareTo(o2.getSymbol());
			}
		}

	};		
	
	
	public Map<String, TreeSet<PriceAllocation>> getAllocations() {
		return allocations;
	}
	
	public TreeSet<PriceAllocation> getAllocationsById(String id) {
		return allocations.get(id);
	}
	
	public void add(PriceAllocation pa)
	{
		TreeSet<PriceAllocation> set = allocations.get(pa.getParentId());
		if (null == set) {
			set = new TreeSet<PriceAllocation>(comparator);
			allocations.put(pa.getParentId(), set);
		}
		boolean found = false;
		for (PriceAllocation x: set) {
			if(x.getSymbol().equals(pa.getSymbol()) &&
					x.getSide().equals(pa.getSide()) &&
					x.getOrderType().equals(pa.getOrderType()) &&
						PriceUtils.Equal(pa.getPrice(), x.getPrice())){
				x.setQty(x.getQty() + pa.getQty());
				found = true;
				break;
			}
		}
		
		if (!found)	{
			set.add(pa);
		}
	}
	
	public void clear() {
		allocations.clear();
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for(TreeSet<PriceAllocation> set: allocations.values()) {
			for(PriceAllocation pa: set) {
				result.append("\n");
				result.append(pa);
			}
		}
		return result.toString();
	}
}
