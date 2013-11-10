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
package com.cyanspring.cstw.gui.common;

import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.business.util.DataConvertException;
import com.cyanspring.common.business.util.GenericDataConverter;
import com.cyanspring.common.type.KeyValue;
import com.cyanspring.common.util.ReflectionUtil;

public class PropertyEditingSupport extends EditingSupport {
	private static final Logger log = LoggerFactory
			.getLogger(PropertyEditingSupport.class);
	List<String> editableFields;
	private final PropertyTableViewer viewer;
	GenericDataConverter dataConverter;
	
	public PropertyEditingSupport(PropertyTableViewer viewer, GenericDataConverter dataConverter) {
		super(viewer);
		this.viewer = viewer;
		this.dataConverter = dataConverter;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		if(element instanceof KeyValue) {
			KeyValue kv = (KeyValue)element;
			if(null == kv.value)
				return new TextCellEditor(viewer.getTable());
			
			if(ReflectionUtil.isEnum(kv.value)) {
				ComboBoxCellEditor editor = new ComboBoxCellEditor(viewer.getTable(), ReflectionUtil.getEnumStringValues(kv.value));
				return editor;
			} else if (kv.value.getClass().equals(Boolean.class)) {
				CheckboxCellEditor editor = new CheckboxCellEditor(viewer.getTable(), SWT.CHECK | SWT.READ_ONLY);
				return editor;
			}
		}
		return new TextCellEditor(viewer.getTable());
	}

	@Override
	protected boolean canEdit(Object element) {
		if(element instanceof KeyValue) {
			KeyValue kv = (KeyValue)element;
			if(null != editableFields && editableFields.contains(kv.key))
				return true;
		}
		return false;
	}

	@Override
	protected Object getValue(Object element) {
		if(element instanceof KeyValue) {
			KeyValue kv = (KeyValue)element;
			if(kv.value == null)
				return "";
			
			if(ReflectionUtil.isEnum(kv.value)) {
				String[] values = ReflectionUtil.getEnumStringValues(kv.value);
				for(int i=0; i<values.length; i++) {
					if(values[i].equals(kv.value.toString()))
						return i;
				}
				return null;
			} else if (kv.value.getClass().equals(Boolean.class)) {
				return kv.value;
			}
			try {
				return dataConverter.toString(kv.key, kv.value);
			} catch (DataConvertException e) {
				log.error(e.getMessage(), e);
				return kv.value.toString();
			}

//			return kv.value.toString();
		}
		return null;
	}

	@Override
	protected void setValue(Object element, Object value) {
		if(element instanceof KeyValue) {
			KeyValue kv = (KeyValue)element;
			if(value == null || kv.value == null) {
				kv.value = value;
			} else if(ReflectionUtil.isEnum(kv.value)) {
				Integer i = (Integer)value;
				String[] values = ReflectionUtil.getEnumStringValues(kv.value);
				kv.value = ReflectionUtil.callStaticMethod(kv.value.getClass(), "valueOf", new String[]{values[i]});
			} else if(kv.value.getClass().equals(Boolean.class)) {
				kv.value = value;
			} else {
				kv.value = value;
			}
//			else {
//				try {
//					kv.value = dataConverter.fromString(kv.value.getClass(), kv.key, (String)value);
//				} catch (Exception e) {
//					log.error(e.getMessage(), e);
//					e.printStackTrace();
//				}
//			}
			@SuppressWarnings("unchecked")
			HashMap<String, Object> map = (HashMap<String, Object>)viewer.getInput();
			map.put(kv.key, kv.value);
			viewer.update(element, null);
		}
	}


	public List<String> getEditableFields() {
		return editableFields;
	}

	public void setEditableFields(List<String> editableFields) {
		this.editableFields = editableFields;
	}

}
