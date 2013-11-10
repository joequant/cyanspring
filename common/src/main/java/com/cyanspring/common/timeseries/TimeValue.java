package com.cyanspring.common.timeseries;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeValue<T> {
	private static SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
	private Date date;
	private T value;
	
	public TimeValue(Date date, T value) {
		super();
		this.date = date;
		this.value = value;
	}
	public Date getDate() {
		return date;
	}
	public T getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return "[" + format.format(date) + "," + value.toString() + "]";
	}
}
