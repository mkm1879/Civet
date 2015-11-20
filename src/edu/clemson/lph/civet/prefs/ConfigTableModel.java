package edu.clemson.lph.civet.prefs;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class ConfigTableModel extends AbstractTableModel {
	private ArrayList<ArrayList<String>> aRows;
	private HashMap<String, ArrayList<String>> mChoices;

	public ConfigTableModel() {
		aRows = new ArrayList<ArrayList<String>>();
		mChoices = new HashMap<String, ArrayList<String>>();
	}
	
	public void addRow( String sName, String sValue, String sDescription, String sType, String sHelpText ) {
		ArrayList<String> aRow = new ArrayList<String>();
		aRow.add(sName);
		aRow.add(sValue);
		aRow.add(sDescription);
		aRow.add(sType);
		aRow.add(sHelpText);
		aRows.add(aRow);
	}
	
	public void setChoices( String sName, ArrayList<String> aChoices ) {
		if( sName != null && sName.trim().length() > 0 && aChoices != null )
			mChoices.put(sName, aChoices);
	}
	
	public void refresh() {
		// setup cell renderers and editors based on sType for each row.
	}
	
	public String getTypeAt( int rowIndex ) {
		if( rowIndex < 0 || rowIndex > aRows.size() )
			return null;
		ArrayList<String> row = aRows.get(rowIndex);
		String sType = row.get(3);
		return sType;
	}

	@Override
	public int getRowCount() {
		return aRows.size();
	}

	@Override
	public int getColumnCount() {
		return 4;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if( rowIndex < 0 || rowIndex > aRows.size() || columnIndex < 0 || columnIndex > 5 )
			return null;
		return aRows.get(rowIndex).get(columnIndex);
	}

}
