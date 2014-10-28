package edu.clemson.lph.civet.addons;
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
import java.awt.Window;

import org.apache.log4j.Logger;

import edu.clemson.lph.civet.AddOn;
import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.controls.DateField;
import edu.clemson.lph.db.DatabaseConnectionFactory;
import edu.clemson.lph.dialogs.TwoLineQuestionDialog;

public class CVIStatsReport implements AddOn {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
	private DatabaseConnectionFactory factory;

	public CVIStatsReport() {
	}

	@Override
	public String getMenuText() {
		return "CVI Statistics Report";
	}

	@Override
	public void execute(Window parent) {
		if( factory == null )
			factory = InitAddOns.getFactory();
		doReportCVIStats();
	}
	
	// TODO move this to AddOns
	private void doReportCVIStats() {
  		TwoLineQuestionDialog dlg = new TwoLineQuestionDialog( (Window)null, "Civet: CVI Statistics Report", 
  				"Start Date?", "End Date?", true);
  		dlg.setIntro("Start and End Dates for Report Period");
		dlg.setVisible(true);
		String sStart = dlg.getAnswerOne();
		String sEnd = dlg.getAnswerTwo();
  		java.util.Date dStart = DateField.textToDate(sStart);
		java.util.Date dEnd = DateField.textToDate(sEnd);
		if( dStart == null || dEnd == null ) return;  // Already prompted by DBFormModel.textToDate()
		sStart = DateField.dateToText(dStart, "MM/dd/yyyy");
		sEnd = DateField.dateToText(dEnd, "MM/dd/yyyy");
		CVIReportView rpt = new CVIReportView( factory, "USAHERDS CVI Report", dStart, dEnd );
		rpt.setVisible(true);

	}

}
