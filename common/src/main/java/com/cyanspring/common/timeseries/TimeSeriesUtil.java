package com.cyanspring.common.timeseries;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.util.PriceUtils;

public class TimeSeriesUtil {
	private static final Logger log = LoggerFactory
			.getLogger(TimeSeriesUtil.class);
	
	// short term MA crossed long term MA in period of bin
	static public CrossingEnum checkCrossing(List<TimeValue<Double>> ss, List<TimeValue<Double>> sl, int bin) {
		if(ss.size() < bin || sl.size() < bin) {
			log.error("time series length is less than bin: " + ss.size() + " : " + sl.size() + " : " + bin);
			return CrossingEnum.NA;
		}
		
		int ltrend = 0;
		if(PriceUtils.GreaterThan(sl.get(0).getValue(), sl.get(bin-1).getValue()))
			ltrend = 1;
		else if(PriceUtils.LessThan(sl.get(0).getValue(), sl.get(bin-1).getValue()))
			ltrend = -1;
		
		int strend = 0;
		if(PriceUtils.GreaterThan(ss.get(0).getValue(), sl.get(0).getValue()))
			strend = 1;
		else if(PriceUtils.LessThan(ss.get(0).getValue(), sl.get(0).getValue()))
			strend = -1;
		
		boolean less = false; // ss is at one point less than sl
		boolean greater = false; // ss is at one point greater than sl
		for(int i=0; i<bin; i++) {
			if(PriceUtils.LessThan(ss.get(i).getValue(),sl.get(i).getValue()))
				less = true;
			
			if(PriceUtils.GreaterThan(ss.get(i).getValue(),sl.get(i).getValue()))
				greater = true;
		}
		
		if(ltrend > 0) {
			if(strend >= 0 && less)
				return CrossingEnum.UP_UP;
			
			if(strend <= 0 && greater)
				return CrossingEnum.UP_DOWN;
		} else if (ltrend < 0) {
			if(strend >= 0 && less)
				return CrossingEnum.DOWN_UP;
			
			if(strend <= 0 && greater)
				return CrossingEnum.DOWN_DOWN;
		} else {
			if(strend >= 0 && less)
				return CrossingEnum.UP_UP;
			
			if(strend <= 0 && greater)
				return CrossingEnum.DOWN_DOWN;
		}
		
		return CrossingEnum.NA;
	}
	
	// MA trending in period of bin
	static public TrendEnum checkTrending(List<TimeValue<Double>> ts, int bin) {
		if(ts.size()<bin) {
			log.error("time series length is less than bin: " + ts.size() + " : " + bin);
			return TrendEnum.NA;
		}
		
		if(ts.get(0).getValue() > ts.get(bin-1).getValue())
			return TrendEnum.UP;
		
		if(ts.get(0).getValue() < ts.get(bin-1).getValue())
			return TrendEnum.DOWN;
		
		return TrendEnum.FLAT;
	}
	
	// long term MA abov short term MA between period of bin
	public static boolean checkAbove(List<TimeValue<Double>> ss, List<TimeValue<Double>> sl, int bin) {
		for(int i=0; i<bin; i++) {
			if(ss.get(i).getValue() > sl.get(i).getValue())
				return false;
		}
		return true;
	}

}
