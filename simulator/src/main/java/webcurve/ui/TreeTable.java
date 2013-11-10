package webcurve.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.AbstractCellEditor;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeModel;

/**
 * @author dennis_d_chen@yahoo.com
 */
public class TreeTable extends JTable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// inner class
	public class TableTree extends JTree implements TableCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		protected int visibleRow;
		   
		public TableTree(TreeModel model) { 
		    super(model); 
		}

		public void setBounds(int x, int y, int w, int h) {
		    super.setBounds(x, 0, w, TreeTable.this.getHeight());
		    
		}

		public void paint(Graphics g) {
		    g.translate(0, -visibleRow * getRowHeight());
		    super.paint(g);
		}

		public Component getTableCellRendererComponent(JTable table,
							       Object value,
							       boolean isSelected,
							       boolean hasFocus,
							       int row, int column) {
		    if(isSelected)
		    	setBackground(table.getSelectionBackground());
		    else
		    	setBackground(table.getBackground());
	       
		    visibleRow = row;
		    return this;
		}

	}	
	
	private void loadIcons()
	{
		if (null == tree)
			return;
		
		//change icon
		Icon moneyIcon = new ImageIcon("images/money.png");
		Icon emblemIcon = new ImageIcon("images/emblem.png");
    
	    DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer)tree.getCellRenderer();
	    renderer.setLeafIcon(emblemIcon);
	    renderer.setClosedIcon(moneyIcon);
	    renderer.setOpenIcon(moneyIcon);

	}
	
	TableTree tree; 
    public TreeTable(TreeTableModel treeTableModel) {
    	super();

		tree = new TableTree(treeTableModel); 
    	treeTableModel.setTree(tree);
		super.setModel(treeTableModel);
		//add(tree);
		
		
		//tree.setBounds(0, 0, col.getWidth(), 0);
	
		tree.setSelectionModel(new DefaultTreeSelectionModel() { 
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			{
		    	setSelectionModel(listSelectionModel); 
			}
		});
		
		// Make the tree and table row heights the same. 
		tree.setRowHeight(getRowHeight());
		tree.setRootVisible(false);
		
		loadIcons();
		
		// Install the tree editor renderer and editor. 
		setDefaultRenderer(TreeTableModel.class, tree); 
		setDefaultEditor(TreeTableModel.class, new TreeTableCellEditor());  
	
		setShowGrid(false);
		setIntercellSpacing(new Dimension(0, 0)); 	        
    }

    public int getEditingRow() {
        return (getColumnClass(editingColumn) == TreeTableModel.class) ? -1 : editingRow;  
    }

    public class TreeTableCellEditor extends AbstractCellEditor implements TableCellEditor {
    	/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Component getTableCellEditorComponent(JTable table, Object value,
    						     boolean isSelected, int r, int c) {
    	    return tree;
    	}

//		@Override
		public Object getCellEditorValue() {
			return null;
		}

    }
    
    @Override
    public void updateUI()
    {
    	super.updateUI();
    	//loadIcons();
  
    }
    
    public Object getOrderAt(int row)
    {
    	return ((TreeTableModel)this.getModel()).getOrderAt(row);
    }
}
