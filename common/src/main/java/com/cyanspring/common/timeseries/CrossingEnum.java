package com.cyanspring.common.timeseries;

public enum CrossingEnum {
	UP_UP, // long term going up, short term up crossing 
	DOWN_UP, // long term going down, short term up crossing
	UP_DOWN, // long term going up, short term down crossing
	DOWN_DOWN, // long timer going down, short term down crossing
	NA // any other situation we don't care
}
