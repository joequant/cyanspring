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
package com.cyanspring.sample.multiinstrument.dollarneutral;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.business.Instrument;
import com.cyanspring.common.strategy.StrategyException;
import com.cyanspring.common.util.PriceUtils;
import com.cyanspring.strategy.multiinstrument.MultiInstrumentStrategy;

public class DollarNeutralStrategy extends MultiInstrumentStrategy {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory
			.getLogger(DollarNeutralStrategy.class);
	@Override
	public void validate() throws StrategyException {
		super.validate();
		
		for(Instrument instr: this.data.getInstrumentData().values()) {
			int leg = instr.get(int.class, "Leg");
			if(!(leg == 1 || leg == 2))
				throw new StrategyException("Leg must be either 1 or 2");
		}
		
		// calculate the quantity distribution base on weighting and ref price
		double leg1TotalWeight = 0.0;
		double leg2TotalWeight = 0.0;
		double totalValue = this.data.get(double.class, "Value");
		for(Instrument instr: this.data.getInstrumentData().values()) {
			if(instr.get(int.class, "Leg") == 1)
				leg1TotalWeight += instr.get(double.class, "Weight");
			else 
				leg2TotalWeight += instr.get(double.class, "Weight");
		}
		
		double leg1RefValue = 0;
		double leg2RefValue = 0;
		for(Instrument instr: this.data.getInstrumentData().values()) {
			if(instr.get(int.class, "Leg") == 1) {
				double weight = instr.get(double.class, "Weight");
				double weighting = weight / leg1TotalWeight;
				double value = totalValue * weighting;
				instr.put("Value", value);
				instr.put("Weight %", weighting);
				double qty = value / instr.get(double.class, "Ref price");
				int lotSize = refDataManager.getRefData(instr.getSymbol()).getLotSize();
				if (lotSize == 0)
					lotSize = 1;
				double refQty = ((int)qty) / lotSize * lotSize;
				instr.put("Ref qty", refQty);
				// calculate total value after we round to lots
				leg1RefValue += refQty * instr.get(double.class, "Ref price");
			} else {
				double weight = instr.get(double.class, "Weight");
				double weighting = weight / leg2TotalWeight;
				double value = totalValue * weighting;
				instr.put("Value", value);
				instr.put("Weight %", weighting);
				double qty = value / instr.get(double.class, "Ref price");
				int lotSize = refDataManager.getRefData(instr.getSymbol()).getLotSize();
				if (lotSize == 0)
					lotSize = 1;
				double refQty = ((int)qty) / lotSize * lotSize;
				instr.put("Ref qty", refQty);
				leg2RefValue += refQty * instr.get(double.class, "Ref price");
			}
		}
		
		// check the ref value(rounded to lots) whether it is acceptable within the 
		// "Allow diff" range
		double allowDiff = this.data.get(double.class, "Allow diff");
		if(Math.abs(totalValue-leg1RefValue) > allowDiff) {
			throw new StrategyException("Leg 1 ref value exceed allow diff value: " + leg1RefValue);
		}
		if(Math.abs(totalValue-leg2RefValue) > allowDiff) {
			throw new StrategyException("Leg 2 ref value exceed allow diff value: " + leg2RefValue);
		}
		
		this.data.put("leg1RefValue", leg1RefValue);
		this.data.put("leg2RefValue", leg2RefValue);
	}
	
	public boolean isLegged() {
		for(Instrument instr: this.data.getInstrumentData().values()) {
			double position = instr.getPosition();
			double refQty = instr.get(double.class, "Ref qty");
			if(!PriceUtils.Equal(Math.abs(position), refQty))
				return true;
		}
		return false;
	}

}
