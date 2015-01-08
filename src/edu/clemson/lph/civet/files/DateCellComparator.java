package edu.clemson.lph.civet.files;

import java.util.Comparator;
import java.util.Date;

public class DateCellComparator implements Comparator<java.util.Date> {

	public DateCellComparator() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int compare(Date arg0, Date arg1) {
		if( arg0 == null && arg1 == null)
			return 0;
		if( arg0 == null && arg1 != null)
			return 1;
		if( arg0 != null && arg1 == null)
			return -1;
		if( arg0 != null && arg1 != null ) {
			if( arg0.getTime() == arg1.getTime() )
				return 0;
			if( arg0.getTime() > arg1.getTime() )
				return 1;
			if( arg0.getTime() < arg1.getTime() )
				return -1;
		}
		return 0;
	}

}
