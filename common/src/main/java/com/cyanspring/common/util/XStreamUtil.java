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

import com.thoughtworks.xstream.XStream;

public class XStreamUtil {
	@SuppressWarnings("rawtypes")
	static public Object fromXml(XStream xstream, Class t, String str) {
		ClassLoader save = xstream.getClassLoader();
		ClassLoader cl = t.getClassLoader();
		if (cl != null)
			xstream.setClassLoader(cl);

		Object result = xstream.fromXML(str);
		xstream.setClassLoader(save);
		return result;

	}
}
