package edu.clemson.lph.controls;
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
import javax.swing.*;

import org.apache.log4j.*;

import java.awt.*;
import java.util.ArrayList;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.db.*;


@SuppressWarnings("serial")
public class DBComboBox extends JComboBox<String> {
	protected static final Logger logger = Logger.getLogger(Civet.class.getName());
	protected boolean bBlankDefault = true;

	protected DatabaseConnectionFactory factory = null;
	protected DBComboBoxModel model;
	protected String sQuery = null;
	protected boolean bLimitToList = false;

	public DBComboBox() {
		model = new DBComboBoxModel();
		try {
			jbInit();
		}
		catch (Exception ex) {
			logger.error(ex.getMessage() + "\nError in DBCombobox init");
		}
	}

	public DBComboBox( DatabaseConnectionFactory factory, String sQuery ) {
		model = new DBComboBoxModel( factory, sQuery );
		setModel( model );
		model.setParent( this );
		try {
			jbInit();
		}
		catch (Exception ex) {
			logger.error(ex.getMessage() + "\nError in DBCombobox init");
		}
	}
	
	public DBComboBox( DBComboBoxModel model ) {
		this.model = model;
		try {
			jbInit();
		}
		catch (Exception ex) {
			logger.error(ex.getMessage() + "\nError in DBCombobox init");
		}
	}

	public void setModel( DBComboBoxModel model ) {
		this.model = model;
		super.setModel(this.model);
	}

	public void setDBFactory( DatabaseConnectionFactory factory ) throws Exception {
		this.factory = factory;
		model.setDBFactory(factory);
	}
	public DatabaseConnectionFactory getDatabaseConnectionFactory() { return factory; }

	public void setBlankDefault( boolean bBlank ) { 
		this.bBlankDefault = bBlank; 
		model.setBlankDefault( bBlank );
	}

	public void addSelectionChangeListener( ComboBoxSelectionListener listener ) {
		model.addSelectionChangeListener( listener );
	}

	public void setQuery( String sQuery ) {
		this.sQuery = sQuery;
		model.setQuery( sQuery );
	}

	public String getQuery() { return sQuery; }

	public void refresh() {
		model.refresh();
		setModel(model);
	}// End populating constructor

	private void jbInit() throws Exception {
		// Will be removed when refresh() is first called.  Puts something in designer.
		addItem("Not Loaded (call refresh() first)");
	}

	public int getSelectedKeyInt() {
		return model.getSelectedKeyInt();
	}

	public Object getSelectedKey() {
		return model.getSelectedKey();
	}

	public void setSelectedKey( int iKey ) {
		model.setSelectedKey(iKey);
	}

	public void setSelectedKey( Object oKey ) {
		model.setSelectedKey(oKey);
	}
	
	public void setSelectedCode( String sCode ) {
		model.setSelectedCode(sCode);
	}


	public String getSelectedValue() {
		return model.getSelectedValue();
	}
	
	public String getSelectedCode() {
		return model.getSelectedCode();
	}
	
	public ArrayList<String> getValues() {
		return model.getValues();
	}
	
	public int getKeyForValue( String sValue ) {
		return model.getKeyIntForValue(sValue);
	}
	
	public String getValueForKeyInt( int iKey ) {
		return model.getValueForKey(iKey);
	}
	
	public String getValueForKey( Object oKey ) {
		return model.getValueForKey(oKey);
	}
	
	public String getValueForCode( String sCode ) {
		return model.getValueForCode( sCode );
	}
	
	public void setLimitToList( boolean bLimit ) {
		bLimitToList = bLimit;
	}

	public void setSelectedValue( String sValue ) {
		model.setSelectedItem(sValue);
		if( bLimitToList ) {
			int iKey = model.getSelectedKeyInt();
			if( iKey < 0 ) {
				logger.error( "DBCombobox set to non-existent value " + sValue, new Exception( "Invalid Combobox value") );
				model.setSelectedItem(null);
			}
		}
	}

	public void setBBlankDefault(boolean bBlankDefault) {
		this.bBlankDefault = bBlankDefault;
	}

	public boolean isBBlankDefault() {
		return bBlankDefault;
	}

	public Container getRootParent() {
		Container parent = this.getParent();
		Container cParent = null;
		while (! (parent instanceof JFrame) && ! (parent instanceof JDialog)) {
			if (parent == null)return cParent; // I give up.  Where am I?
			parent = parent.getParent();
		}
		if (parent instanceof JFrame) cParent = (JFrame) parent;
		else if (parent instanceof JDialog) cParent = (JDialog) parent;
		return cParent;

	}

	public JFrame getFrameParent() {
		Container parent = this.getParent();
		JFrame fParent = null;
		while (! (parent instanceof JFrame) && ! (parent instanceof JDialog)) {
			if (parent == null)return fParent; // I give up.  Where am I?
			parent = parent.getParent();
		}
		if (parent instanceof JFrame) fParent = (JFrame) parent;
		return fParent;
	}

	public JDialog getDialogParent() {
		Container parent = this.getParent();
		JDialog dParent = null;
		while (! (parent instanceof JFrame) && ! (parent instanceof JDialog)) {
			if (parent == null)return dParent; // I give up.  Where am I?
			parent = parent.getParent();
		}
		if (parent instanceof JDialog) dParent = (JDialog) parent;
		return dParent;
	}

	public Window getWindowParent() {
		Container parent = this.getParent();
		Window wParent = null;
		while (! (parent instanceof Window) ) {
			if (parent == null) return wParent; // I give up.  Where am I?
			parent = parent.getParent();
		}
		wParent = (Window) parent;
		return wParent;
	}

} // end class DBComboBox
