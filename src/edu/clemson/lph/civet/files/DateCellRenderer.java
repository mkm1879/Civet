package edu.clemson.lph.civet.files;

import java.text.SimpleDateFormat;

import javax.swing.table.DefaultTableCellRenderer;

@SuppressWarnings("serial")
public class DateCellRenderer extends DefaultTableCellRenderer {
	private static final String sFmt = "EEE, MMM d, yyyy HH:mm";
	private static final String sShortFmt = "MMM d, yyyy";

	public DateCellRenderer() {
		super();
	}
	
	public void setValue(Object value) {
		if( value instanceof java.util.Date ) {
			final SimpleDateFormat fmt = new SimpleDateFormat(sFmt);
			final SimpleDateFormat sFmt = new SimpleDateFormat(sShortFmt);
			String sDisplay = fmt.format(value);
			if( sDisplay.endsWith("00:00") )
				sDisplay = sFmt.format(value);
			setText(sDisplay);
		}
		else {
			super.setValue(value);
		}
    }

}
