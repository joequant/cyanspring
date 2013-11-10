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
package com.cyanspring.common.staticdata;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.IPlugin;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class RefDataManager implements IPlugin {
	private static final Logger log = LoggerFactory
			.getLogger(RefDataManager.class);
	String refDataFile;
	Map<String, RefData> map = new HashMap<String, RefData>();
	
	@SuppressWarnings("unchecked")
	@Override
	public void init() throws Exception {
		log.info("initialising");
		XStream xstream = new XStream(new DomDriver());
		File file = new File(refDataFile);
		List<RefData> list;
		if (file.exists()) {
			list = (List<RefData>)xstream.fromXML(file);
		} else {
			throw new Exception("Missing refdata file: " + refDataFile);
		}
		
		for(RefData refData: list) {
			map.put(refData.getSymbol(), refData);
		}
	}
	
	@Override
	public void uninit() {
		log.info("uninitialising");
		map.clear();
	}
	public RefData getRefData(String symbol) {
		return map.get(symbol);
	}

	public String getRefDataFile() {
		return refDataFile;
	}

	public void setRefDataFile(String refDataFile) {
		this.refDataFile = refDataFile;
	}
	
	
}
