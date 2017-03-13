package edu.clemson.lph.civet.files;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import org.apache.log4j.Logger;
import edu.clemson.lph.civet.Civet;

public class DateCellComparator implements Comparator<java.util.Date> {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	private SimpleDateFormat df = new SimpleDateFormat( "MMM d, yyyy");

	public DateCellComparator() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int compare(java.util.Date arg0, java.util.Date arg1) {
		if( arg0 == null && arg1 == null)
			return 0;
		if( arg0 == null && arg1 != null)
			return 1;
		if( arg0 != null && arg1 == null)
			return -1;
		if( arg0 != null && arg1 != null ) {
			java.util.Date date0 = arg0;
			java.util.Date date1 = arg1;
			if( date0.getTime() == date1.getTime() )
				return 0;
			if( date0.getTime() > date1.getTime() )
				return 1;
			if( date0.getTime() < date1.getTime() )
				return -1;
		}
		return 0;
	}

}
