package edu.clemson.lph.civet.files;

import java.text.SimpleDateFormat;

import javax.swing.table.DefaultTableCellRenderer;

@SuppressWarnings("serial")
public class DateCellRenderer extends DefaultTableCellRenderer {
	private static final String sFmt = "EEE, MMM d, yyyy HH:mm";

	public DateCellRenderer() {
		super();
	}
	
	public void setValue(Object value) {
		if( value instanceof java.util.Date ) {
			final SimpleDateFormat fmt = new SimpleDateFormat(sFmt);
			String sDisplay = fmt.format(value);
			setText(sDisplay);
		}
		else {
			super.setValue(value);
		}
    }

}
