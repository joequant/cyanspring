package com.cyanspring.common.util;

import java.util.Calendar;
import java.util.Date;

public class Time {
	static final long millisInDay = 60 * 60 * 24 * 1000; 
	static final int millisInHour = 60 * 60 * 1000;
	static final int millisInMinute = 60 * 1000;
	static final int millisSecond = 1000;
	
	int hour;
	int minute;
	int second;
	int ms;
	
	public Time() {
	}
	
	public Time(int hour, int minute, int second, int ms) {
		super();
		this.hour = hour;
		this.minute = minute;
		this.second = second;
		this.ms = ms;
	}
	
	public Time(long date) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(date);
		this.hour = cal.get(Calendar.HOUR_OF_DAY);
		this.minute = cal.get(Calendar.MINUTE);
		this.second = cal.get(Calendar.SECOND);
		this.ms = cal.get(Calendar.MILLISECOND);
	}

	//local time
	public Time(Date date) {
		this(date.getTime());
	}
	
	public int getHour() {
		return hour;
	}
	public void setHour(int hour) {
		this.hour = hour;
	}
	public int getMinute() {
		return minute;
	}
	public void setMinute(int minute) {
		this.minute = minute;
	}
	public int getSecond() {
		return second;
	}
	public void setSecond(int second) {
		this.second = second;
	}
	public int getMs() {
		return ms;
	}
	public void setMs(int ms) {
		this.ms = ms;
	}
	
	public int getTime() {
		int result = this.hour * millisInHour +
				this.minute * millisInMinute +
				this.second * millisSecond +
				this.ms;
		return result;
	}

	@Override
	public String toString() {
		return this.hour + ":" + this.minute + ":" + this.second + "." + this.ms; 
	}

	@Override
	public boolean equals(Object obj) {
		if(null == obj || !(obj instanceof Time))
			return false;
		Time time = (Time)obj;
		return this.getTime() == time.getTime();
	}
	
	public boolean before(Time time) {
		return this.getTime() < time.getTime();
	}
	
	public boolean after(Time time) {
		return this.getTime() > time.getTime();
	}
}
