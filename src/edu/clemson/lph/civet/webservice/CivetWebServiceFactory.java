package edu.clemson.lph.civet.webservice;

import edu.clemson.lph.civet.prefs.CivetConfig;
/**
 * Simple class to build either the old or new implementation based on the
 * URL in the user's CivetConfig.txt
 * @author mike
 * 
Copyright 2016 Michael K Martin

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

 *
 */
public class CivetWebServiceFactory {

	private CivetWebServiceFactory() {
		// TODO Auto-generated constructor stub
	}

	public static CivetWebServices getService() {
		CivetWebServices service = null;
		String sHerdsUrl = CivetConfig.getHERDSWebServiceURL();
		if( sHerdsUrl.toUpperCase().endsWith("USAHERDS.API") )
			service = new CivetWebServicesNew();
		else
			service = new CivetWebServicesOld();
		return service;
	}
}
