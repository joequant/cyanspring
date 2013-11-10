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
package com.cyanspring.common.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReflectionUtil {
	@SuppressWarnings("rawtypes")
	static private DualMap<Class, Class> typeMap = new DualMap<Class, Class>();
	static {
	       typeMap.put(Integer.class, Integer.TYPE );        
	       typeMap.put(Long.class, Long.TYPE );        
	       typeMap.put(Double.class, Double.TYPE );        
	       typeMap.put(Float.class, Float.TYPE );        
	       typeMap.put(Boolean.class, Boolean.TYPE );        
	       typeMap.put(Character.class, Character.TYPE );        
	       typeMap.put(Byte.class, Byte.TYPE );        
	       typeMap.put(Void.class, Void.TYPE );        
	       typeMap.put(Short.class, Short.TYPE ); 	
	}

	@SuppressWarnings("rawtypes")
	static private HashMap<Class, Object> defaultValues = new HashMap<Class, Object>();
	static {
		defaultValues.put(Integer.TYPE, new Integer(0) );        
		defaultValues.put(Long.TYPE, new Long(0) );        
		defaultValues.put(Double.TYPE, new Double(0) );        
		defaultValues.put(Float.TYPE, new Float(0) );        
		defaultValues.put(Boolean.TYPE, new Boolean(false) );        
		defaultValues.put(Character.TYPE, (Object)new Character('\u0000') );        
		defaultValues.put(Byte.TYPE, new Byte((byte)0));        
		defaultValues.put(Void.TYPE, null );        
		defaultValues.put(Short.TYPE, new Short((short)0)); 	
	}

	@SuppressWarnings("rawtypes")
	static public Class getWrapper(Class t) {
		return typeMap.getKeyByValue(t);
	}
	
	@SuppressWarnings("rawtypes")
	static public Object getPrimitiveDefaultValues(Class t) {
		return defaultValues.get(t);
	}
	
	// can only call public methods
	@SuppressWarnings({ "unchecked", "rawtypes" })
	static public <T> T callMethod(Class<T> returnType, Object caller, String method, Object[] args) {
		Class cls = caller.getClass();
		Method methods[] = cls.getMethods();
		
		for(Method m: methods) {
			if (m.getName().equals(method)) {
				Class[] params = m.getParameterTypes();
				if(params.length != args.length)
					continue;
				
				for(int i=0; i< params.length; i++) {
					Class primitiveType = typeMap.get(args[i].getClass());
					if (params[i].equals(args[i].getClass()) ||
						(primitiveType != null && params[i].equals(primitiveType))) {
						
						try {
							return (T)m.invoke(caller, args);
						} catch (Exception e) {
							return null;
						}
					}

				}
			}
		}
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	static public <T> T callStaticMethod(Class<T> caller, String method, Object[] args) {
		Method methods[] = caller.getMethods();
		
		for(Method m: methods) {
			if (m.getName().equals(method) && Modifier.isStatic(m.getModifiers())) {
				Class[] params = m.getParameterTypes();
				if(params.length != args.length)
					continue;
				
				for(int i=0; i< params.length; i++) {
					Class primitiveType = typeMap.get(args[i].getClass());
					if (params[i].equals(args[i].getClass()) ||
						(primitiveType != null && params[i].equals(primitiveType))) {
						
						try {
							return (T)m.invoke(caller, args);
						} catch (Exception e) {
							return null;
						}
					}

				}
			}
		}
		return null;
	}

	// can only call public methods
	@SuppressWarnings({ "unchecked", "rawtypes" })
	static public <T> T callMethod(Object caller, String method, Object[] args) {
		Class cls = caller.getClass();
		Method methods[] = cls.getMethods();
		
		for(Method m: methods) {
			if (m.getName().equals(method)) {
				Class[] params = m.getParameterTypes();
				if(params.length != args.length)
					continue;
				
				for(int i=0; i< params.length; i++) {
					Class primitiveType = typeMap.get(args[i].getClass());
					if (params[i].equals(args[i].getClass()) ||
						(primitiveType != null && params[i].equals(primitiveType))) {
						
						try {
							return (T)m.invoke(caller, args);
						} catch (Exception e) {
							return null;
						}
					}

				}
			}
		}
		return null;
	}

	static public boolean isEnum(Object obj) {
		if(null == obj)
			return false;
		
		return obj.getClass().isEnum();
	}
	
	@SuppressWarnings("rawtypes")
	static public String[] getEnumStringValues(Object obj) {
		Class clazz = obj.getClass();
		Field[] flieds = clazz.getDeclaredFields();
		List<String> list = new ArrayList<String>();
	    for (Field field : flieds) {
	    	if (field.isEnumConstant())
	    		list.add(field.getName());
	    }
	    String[] result  = new String[list.size()];
	    list.toArray(result);
	    return result;
	}
}
