package com.cyanspring.common.business;

public class FieldDef {
	private String name;
	private Class<?> type;
	private boolean input;
	private boolean amendable;
	private String value; //default value in string if any
	
	public FieldDef() {
	}
	
	public FieldDef(String name, Class<?> type, boolean input,
			boolean amendable, String value) {
		super();
		this.name = name;
		this.type = type;
		this.input = input;
		this.amendable = amendable;
		this.value = value;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Class<?> getType() {
		return type;
	}
	public void setType(Class<?> type) {
		this.type = type;
	}
	public boolean isInput() {
		return input;
	}
	public void setInput(boolean input) {
		this.input = input;
	}
	public boolean isAmendable() {
		return amendable;
	}
	public void setAmendable(boolean amendable) {
		this.amendable = amendable;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return name + "," + input + "," + amendable + "," + value;
	}
}
