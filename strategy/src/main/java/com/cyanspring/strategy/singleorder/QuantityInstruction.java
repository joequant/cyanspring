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
package com.cyanspring.strategy.singleorder;

public class QuantityInstruction {
	double aggresiveQty;
	double passiveQty;
	
	public double getAggresiveQty() {
		return aggresiveQty;
	}
	public void setAggresiveQty(double aggresiveQty) {
		this.aggresiveQty = aggresiveQty;
	}
	public double getPassiveQty() {
		return passiveQty;
	}
	public void setPassiveQty(double passiveQty) {
		this.passiveQty = passiveQty;
	}
	
//	public boolean isNull() {
//		return PriceUtils.Equal(aggresiveQty, 0) && PriceUtils.Equal(passiveQty, 0);
//	}
//	
	@Override
	public String toString() {
		return "[" + aggresiveQty + ", " + passiveQty +"]";
	}
}
