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
package com.cyanspring.common.data;

import java.io.CharArrayWriter;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.util.ReflectionUtil;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.CompactWriter;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class DataObject implements Cloneable{
	private static final Logger log = LoggerFactory
			.getLogger(DataObject.class);
	static private XStream xstream = new XStream(new DomDriver());
	private HashMap<String, Object> fields;

	public Map<String, Object> getFields() {
		return fields;
	}

	public DataObject() {
		fields = new HashMap<String, Object>();
	}
	
	public DataObject(Map<String, Object> fields) {
		this.fields = new HashMap<String, Object>(fields);
	}

	@SuppressWarnings("unchecked")
	public <T> T get(Class<T> t, String fieldName) {
		Object obj = fields.get(fieldName);
		if (t.isPrimitive() && null == obj) {
			return (T)ReflectionUtil.getPrimitiveDefaultValues(t);
		}
			
		return (T)obj;
	}

	public <T> T get(T e, Class<T> t, String fieldName) {
		if(fieldExists(fieldName)) {
			return get(t, fieldName);
		} else
			return e;
	}
	
	public boolean fieldExists(String name) {
		return fields.containsKey(name);
	}
	
	public Object put(String name, Object value) {
		return fields.put(name, value);
	}
	
	public Object remove(String name) {
		return fields.remove(name);
	}
	
	public String toXML() {
		return xstream.toXML(this);
	}
	
	public String toCompactXML() {
		return toCompactXML(this);
	}
	
	static public String toXML(Object obj) {
		return xstream.toXML(obj);
	}
	
	@SuppressWarnings("unchecked")
	static public <T> T fromString(Class<T> t, String str) {
		ClassLoader cl = t.getClassLoader();
		if (cl != null)
			xstream.setClassLoader(cl);

		return (T)xstream.fromXML(str);
	}

	static public String toCompactXML(Object obj) {
		CharArrayWriter writer = new CharArrayWriter();
		xstream.marshal(obj, new CompactWriter(writer));
		return new String(writer.toCharArray());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Object clone() {
		DataObject obj = null;
		try {
			obj = (DataObject)super.clone();
			obj.fields = (HashMap<String, Object>)this.fields.clone();
		} catch (CloneNotSupportedException e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		}
		return obj;
	}
}
