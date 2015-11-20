package edu.clemson.lph.civet.prefs;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

@SuppressWarnings("serial")
public class PrefsCellEditor extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {
	
	TableCellPanel panel = null;

	public PrefsCellEditor() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Object getCellEditorValue() {
		return panel.getValue();
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		ConfigTableModel model = (ConfigTableModel)table.getModel();
		String sType = model.getTypeAt(row);
		panel = new TableCellPanel(sType);
		panel.setValue( (String)value );
		return panel;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		ConfigTableModel model = (ConfigTableModel)table.getModel();
		String sType = model.getTypeAt(row);
		panel = new TableCellPanel(sType);
		panel.setValue( (String)value );
		return panel;
	}

}
