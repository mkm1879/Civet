package edu.clemson.lph.utils;

import java.util.Comparator;

public class StringComparator implements Comparator<String> {

	@Override
	public int compare(String obj1, String obj2) {
	    if (obj1 == null) {
	        return -1;
	    }
	    if (obj2 == null) {
	        return 1;
	    }
	    if (obj1.equals( obj2 )) {
	        return 0;
	    }
	    return obj1.compareTo(obj2);
	  }

}

