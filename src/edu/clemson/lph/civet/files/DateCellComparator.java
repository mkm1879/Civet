package edu.clemson.lph.civet.files;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import org.apache.log4j.Logger;
import edu.clemson.lph.civet.Civet;

public class DateCellComparator implements Comparator<String> {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	private SimpleDateFormat df = new SimpleDateFormat( "MMM d, yyyy");

	public DateCellComparator() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int compare(String arg0, String arg1) {
		if( arg0 == null && arg1 == null)
			return 0;
		if( arg0 == null && arg1 != null)
			return 1;
		if( arg0 != null && arg1 == null)
			return -1;
		if( arg0 != null && arg1 != null ) {
			java.util.Date date0 = null;
			java.util.Date date1 = null;
			try {
				date0 = df.parse(arg0);
				date1 = df.parse(arg1);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				logger.error(e);
			}
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
