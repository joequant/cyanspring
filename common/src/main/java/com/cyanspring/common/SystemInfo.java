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
package com.cyanspring.common;

public class SystemInfo {
	private String env = "Test";
	private String category = "EB";
	private String id = "CSTW";
	private String url = "tcp://localhost:61616";
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public void setEnv(String env) {
		this.env = env;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getEnv() {
		return env;
	}
	public String getCategory() {
		return category;
	}

	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	@Override
	public String toString() {
		return env + ":" + category + ":" + id + ":" + url;
	}
}
