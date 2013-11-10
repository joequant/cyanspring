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
package com.cyanspring.server.strategy;

import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.TreeSet;

import org.junit.Test;

import com.cyanspring.common.strategy.PriceAllocation;
import com.cyanspring.common.strategy.PriceInstruction;
import com.cyanspring.common.type.ExchangeOrderType;
import com.cyanspring.common.type.OrderSide;
import com.cyanspring.common.util.PriceUtils;

public class PriceInstructionTest {

	@Test
	public void testSamePriceMerge() {
		PriceInstruction pi = new PriceInstruction();
		pi.add(new PriceAllocation("0005.HK", OrderSide.Buy, 68.4, 4800, ExchangeOrderType.LIMIT, ""));
		pi.add(new PriceAllocation("0005.HK", OrderSide.Buy, 68.4, 6400, ExchangeOrderType.LIMIT, ""));
		Collection<TreeSet<PriceAllocation>> col = pi.getAllocations().values();
		assertTrue(col.size() == 1);
		TreeSet<PriceAllocation> pas = col.iterator().next();
		assertTrue(pas.size() == 1);
		PriceAllocation pa = pas.first();
		assertTrue(PriceUtils.Equal(pa.getPrice(), 68.4));
		assertTrue(PriceUtils.Equal(pa.getQty(), 11200));
		
	}
}
