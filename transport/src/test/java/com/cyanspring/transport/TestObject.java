package com.cyanspring.transport;

import org.junit.Ignore;

@Ignore
public class TestObject {
	public enum Type { Type1, Type2 }
	public Type type;
	public String name;
	public int age;
	public TestObject(Type type, String name, int age) {
		super();
		this.type = type;
		this.name = name;
		this.age = age;
	}
	
}
