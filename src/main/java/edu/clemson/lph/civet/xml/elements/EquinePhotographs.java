/*
Copyright 2014-2018 Michael K Martin

This file is part of Civet.

Civet is free software: you can redistribute it and/or modify
it under the terms of the Lesser GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Civet is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the Lesser GNU General Public License
along with Civet.  If not, see <http://www.gnu.org/licenses/>.
*/

package edu.clemson.lph.civet.xml.elements;

import java.util.ArrayList;

public class EquinePhotographs {
	// Ignoring the ImageRef part of Photograph because not used or supplied by Civet
	// This breaks the pattern slightly.
	public ArrayList<String> views = new ArrayList<String>();
	
	/**
	 * Populate with just the Photograph/View values( Left, Front, and/or Right )
	 * @param view1
	 * @param view2
	 * @param view3
	 */
	public EquinePhotographs(String view1, String view2, String view3) {
		if( view1 != null && view1.trim().length() > 0 ) views.add(view1);
		if( view2 != null && view2.trim().length() > 0 ) views.add(view2);
		if( view3 != null && view3.trim().length() > 0 ) views.add(view3);
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for( String sView : views ) {
			sb.append(sView);
			sb.append( ", ");
		}
		String sRet = sb.toString();
		return sRet.substring(0, sRet.length() - 2);
	}
}