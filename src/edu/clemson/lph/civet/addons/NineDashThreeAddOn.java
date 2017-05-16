package edu.clemson.lph.civet.addons;

import java.awt.Window;

import edu.clemson.lph.civet.AddOn;
import edu.clemson.lph.db.DatabaseConnectionFactory;

public class NineDashThreeAddOn implements AddOn {

	public NineDashThreeAddOn() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getMenuText() {
		// TODO Auto-generated method stub
		return "Enter NPIP 9-3 Movement Forms";
	}

	@Override
	public void execute(Window parent) {
		DatabaseConnectionFactory factory = InitAddOns.getFactory();
		NineDashThreeDialog dlg = new NineDashThreeDialog(parent, factory);
		dlg.setVisible(true);
	}
}
