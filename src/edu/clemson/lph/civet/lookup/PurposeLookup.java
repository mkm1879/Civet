package edu.clemson.lph.civet.lookup;
/*
Copyright 2014 Michael K Martin

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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.CivetConfig;
import edu.clemson.lph.db.DBComboBoxModel;
import edu.clemson.lph.db.DBTableSource;
import edu.clemson.lph.utils.LabeledCSVParser;
import edu.clemson.lph.utils.StringUtils;

@SuppressWarnings("serial")
public class PurposeLookup extends DBComboBoxModel {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	private static final ArrayList<String> aStdCodes = 
			new ArrayList<String>(Arrays.asList("breeding","feeding","grazing","medicalTreatment",
					                            "pet","race","rodeo","sale","show","slaughter","training","other"));

	/**
	 * Default constructor assumes existence of a PurposeTable in CivetConfig and will use that 
	 * for all lookups from this object.  Including its function as a DBComboBoxModel based on iKey
	 */
	public PurposeLookup() {
		readPurposeTable();		
	}
	
	private void readPurposeTable() {
			hValuesKeys.clear();
			hKeysValues.clear();
			super.removeAllElements();
			if( bBlank ) {
				super.addElement("");
				hValuesCodes.put("", "");
				hCodesValues.put("", "");
			}
			for( String sCode : aStdCodes ) {
				String sName = StringUtils.toTitleCase(sCode);
				if( "medicalTreatment".equals(sCode) )
					sName = "Medical Treatment";
				 super.addElement(sName);
				 hValuesCodes.put(sName, sCode);
				 hKeysValues.put(sCode, sName);
			}
	}
	
	@Override
	public void refresh() {
		readPurposeTable();
	}


}

