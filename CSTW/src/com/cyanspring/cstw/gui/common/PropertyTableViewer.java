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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyanspring.common.business.util.DataConvertException;
import com.cyanspring.common.business.util.GenericDataConverter;
import com.cyanspring.common.type.KeyValue;
import com.cyanspring.cstw.common.ImageID;
import com.cyanspring.cstw.gui.Activator;

public class PropertyTableViewer extends TableViewer {
	private static final Logger log = LoggerFactory.getLogger(PropertyTableViewer.class);
	private List<String> properties = new ArrayList<String>();
	private PropertyEditingSupport editingSupport;
	private List<KeyValue> changedFields = new ArrayList<KeyValue>();
	private HashMap<String, Object> savedInput;
	private Composite parent;
	private GenericDataConverter dataConverter;
	private ImageRegistry imageRegistry;
	private PropertyTableComparator comparator;
	
	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
//			log.debug("getElements " + parent);
			if (parent instanceof HashMap) {
				@SuppressWarnings("unchecked")
				HashMap<String, Object> map = (HashMap<String, Object>)parent;
				ArrayList<KeyValue> list = new ArrayList<KeyValue>();
				
				for (String key: properties) {
					Object value = map.get(key);
					if (null != value) {
						list.add(new KeyValue(key, value));
					} else {
						//TODO remove it from properties??
					}
				}
				Set<Entry<String, Object>> set = map.entrySet();
				for(Entry<String, Object> entry: set) {
					if(!properties.contains(entry.getKey()))
						list.add(new KeyValue(entry.getKey(), entry.getValue()));
				}
				return list.toArray();
			}
	        return null;
		}
	}
	
	@Override
	public void applyEditorValue() {
		super.applyEditorValue();
	}
//	class ViewLabelProvider extends LabelProvider implements
//			ITableLabelProvider {
//		@Override
//		public String getColumnText(Object obj, int index) {
//			if (obj instanceof KeyValue) {
//				KeyValue kv = (KeyValue)obj;
//				if (index == 0)
//					return kv.key;
//				else if (index == 1)
//					return getText(kv.value);
//			}
//			return getText(obj);
//		}
//
//		@Override
//		public Image getColumnImage(Object obj, int index) {
//			if (index == 1 && obj instanceof KeyValue) {
//				KeyValue kv = (KeyValue)obj;
//				if(kv.value instanceof Boolean) {
//					return ((Boolean)kv.value)?imageRegistry.getDescriptor(ImageID.TRUE_ICON.toString()).createImage() :
//						imageRegistry.getDescriptor(ImageID.FALSE_ICON.toString()).createImage();
//				}
//			}
//			return null;
//		}
//	}
	
	public void init() {
		imageRegistry = Activator.getDefault().getImageRegistry();

		setContentProvider(new ViewContentProvider());
//		setLabelProvider(new ViewLabelProvider());

		final Table table = this.getTable();
		table.setLinesVisible(true);
		TableLayout layout = new TableLayout();
		table.setLayout(layout);
		comparator = new PropertyTableComparator();
		setComparator(comparator);
		
		final TableColumn column0 = new TableColumn(table, SWT.NONE);
		column0.setText("Name");
		column0.setWidth(50);
		column0.setResizable(true);
		column0.setMoveable(false);
		column0.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				comparator.setColumn(0);
				int dir = comparator.getDirection();
				getTable().setSortDirection(dir);
				refresh();
			}
		});
		
		TableViewerColumn tvColumn0 = new TableViewerColumn(this, column0);
		tvColumn0.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object obj) {
				if (obj instanceof KeyValue) {
					return ((KeyValue)obj).key;
				}
				return obj == null?null:obj.toString();
			}

			@Override
			public Image getImage(Object obj) {
				return null;
			}
		});


		final TableColumn column1 = new TableColumn(table, SWT.NONE);
		column1.setText("Value");
		column1.setWidth(50);
		column1.setResizable(true);
		column1.setMoveable(false);

		editingSupport = new PropertyEditingSupport(this, dataConverter);
		TableViewerColumn tvColumn1 = new TableViewerColumn(this, column1);
//		tvColumn.setLabelProvider(new CellLabelProvider() {
//			@Override
//			public void update(ViewerCell cell) {
//				PropertyTableViewer.KeyValue kv = (PropertyTableViewer.KeyValue) cell.getElement();
//				cell.setText(kv.value.toString());
//			}			
//		});
		tvColumn1.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object obj) {
				if (obj instanceof KeyValue) {
					KeyValue kv = (KeyValue)obj;
					try {
						return dataConverter.toString(kv.key, kv.value);
					} catch (DataConvertException e) {
						log.error(e.getMessage(), e);
						e.printStackTrace();
					}
				}
				return obj == null?null:obj.toString();
			}

			@Override
			public Image getImage(Object obj) {
				if (obj instanceof KeyValue) {
					KeyValue kv = (KeyValue)obj;
					if(kv.value instanceof Boolean) {
						return ((Boolean)kv.value)?imageRegistry.getDescriptor(ImageID.TRUE_ICON.toString()).createImage() :
							imageRegistry.getDescriptor(ImageID.FALSE_ICON.toString()).createImage();
					}
				}
				return null;
			}
		});
		tvColumn1.setEditingSupport(editingSupport);


		// Provide the input to the ContentProvider
		table.addListener(SWT.Paint, new Listener() {
			public void handleEvent(Event event) {
				column0.setWidth(table.getClientArea().width/2);
				column1.setWidth(table.getClientArea().width/2);
				table.removeListener(SWT.Paint, this);
			}
		});
		
		table.setHeaderVisible(true);
		refresh();
	}

	public PropertyTableViewer(Composite parent, int style, GenericDataConverter dataConverter) {
		super(parent, style);
		this.parent = parent;
		this.dataConverter = dataConverter;
	}

	private void setEditModeColor(List<String> editableFields) {
		Color grey = this.getControl().getDisplay().getSystemColor(SWT.COLOR_GRAY);
		Table table = getTable();
		TableItem[] items = table.getItems();
		for(TableItem item: items) {
			KeyValue kv = (KeyValue)item.getData();
			item.setBackground(0, grey);
			if(!editableFields.contains(kv.key))
				item.setBackground(1, grey);
		}
	}

	private void resetEditModeColor() {
		Color white = this.getControl().getDisplay().getSystemColor(SWT.COLOR_WHITE);
		Table table = getTable();
		TableItem[] items = table.getItems();
		for(TableItem item: items) {
			item.setBackground(0, white);
			item.setBackground(1, white);
		}
	}
	
	public List<KeyValue> workoutChangedFields() {
		@SuppressWarnings("unchecked")
		HashMap<String, Object> current = (HashMap<String, Object>)this.getInput();
		List<KeyValue> result = new ArrayList<KeyValue>();
		 Set<Entry<String, Object>> set = current.entrySet();
		 for(Entry<String, Object> entry: set) {
			 Object oldValue = savedInput.get(entry.getKey());
			if ((oldValue == null && entry.getValue() != null) || 
				(oldValue != null && entry.getValue() == null)) {
				 result.add(new KeyValue(entry.getKey(), entry.getValue()));
			} else if (oldValue != null && entry.getValue() != null){
				try {
					String strOld = dataConverter.toString(entry.getKey(), oldValue);
					String strNew = "";
					if(entry.getValue() instanceof String) {
						strNew = (String)entry.getValue();
					} else {
						strNew = dataConverter.toString(entry.getKey(), oldValue);
					}
					if(!strOld.equals(strNew)) {
						 result.add(new KeyValue(entry.getKey(), entry.getValue()));
					}
				} catch (DataConvertException e) {
					log.error(e.getMessage(), e);
				}
			}
		 }
		 
		 return result;
	}

	@SuppressWarnings("unchecked")
	public void turnOnEditMode(List<String> editableFields) {
		savedInput = (HashMap<String, Object>)this.getInput();
		if(savedInput != null)
			this.setInput(savedInput.clone());
		setEditModeColor(editableFields);
		editingSupport.setEditableFields(editableFields);
	}
	
	public void turnOffEditMode() {
		setInput(savedInput);
		resetEditModeColor();
		editingSupport.setEditableFields(null);
	}

	public List<KeyValue> getChangedFields() {
		return changedFields;
	}

	public HashMap<String, Object> getSavedInput() {
		return savedInput;
	}

	public Composite getParent() {
		return parent;
	}

	public void setParent(Composite parent) {
		this.parent = parent;
	}

}
