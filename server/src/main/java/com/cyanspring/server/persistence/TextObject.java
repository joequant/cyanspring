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
package com.cyanspring.server.persistence;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.cyanspring.common.Clock;
import com.cyanspring.common.util.IdGenerator;

public class TextObject implements Serializable {
	private static final long serialVersionUID = -2201356105198952571L;
	private String id;
	private String serverId;
	private Date timeStamp;
	private String xml;
	private int line;
	
	public TextObject() {
		super();
	}
	public TextObject(String id, Date timeStamp, String xml, int line) {
		super();
		this.id = id;
		this.timeStamp = timeStamp;
		this.xml = xml;
		this.line = line;
		this.serverId = IdGenerator.getInstance().getSystemId();
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Date getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}
	public String getXml() {
		return xml;
	}
	public void setXml(String xml) {
		this.xml = xml;
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}
	
	public String getServerId() {
		return serverId;
	}
	
	public void setServerId(String serverId) {
		this.serverId = serverId;
	}
	
	public static List<String> chop(String str, int size) {
		ArrayList<String> result = new ArrayList<String>();
		int pos = 0;
		while(pos + size < str.length()) {
			result.add(str.substring(pos, pos + size));
			pos += size;
		}
		if(pos + size >= str.length()) {
			result.add(str.substring(pos, str.length()));
		}
		return result;
	}
	
	public static String assemble(List<TextObject> lines) {
		StringBuilder sb = new StringBuilder();
		for(TextObject line: lines) {
			sb.append(line.getXml());
		}
		return sb.toString();
	}
	
	public static List<TextObject> createTextObjects(String id, String xml, int size) {
		ArrayList<TextObject> result = new ArrayList<TextObject>();
		List<String> lines = chop(xml, size);
		int lineNo = 0;
		for(String line: lines) {
			result.add(new TextObject(id, Clock.getInstance().now(), line, ++lineNo));
		}
		return result;
	}
}
