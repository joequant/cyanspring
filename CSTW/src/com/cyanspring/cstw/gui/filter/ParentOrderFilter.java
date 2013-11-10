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
package com.cyanspring.cstw.gui.filter;

import java.util.HashMap;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class ParentOrderFilter extends ViewerFilter {
	String column;
	String pattern;
	public void setMatch(String column, String pattern) {
		this.column = column;
		this.pattern = pattern;
	}
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (!(element instanceof HashMap))
			return true;
		
		@SuppressWarnings("unchecked")
		HashMap<String, Object> map = (HashMap<String, Object>)element;
		Object field = map.get(column);
		if (null == field)
			return true;
		
		if(field.toString().toUpperCase().indexOf(pattern.toUpperCase()) > -1)
			return true;
		return false;
	}

	public String getColumn() {
		return column;
	}

	public String getPattern() {
		return pattern;
	}

}
