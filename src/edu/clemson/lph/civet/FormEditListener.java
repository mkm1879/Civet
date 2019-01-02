package edu.clemson.lph.civet;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class FormEditListener implements ItemListener, DocumentListener {
	private boolean bChanged = false;

	public FormEditListener() {
		bChanged = false;
	}

	public void clear( ) {
		bChanged = false;
	}
	
	public boolean isChanged() {
		return bChanged;
	}

	@Override
	public void itemStateChanged(ItemEvent event) {
		if (event.getStateChange() == ItemEvent.SELECTED) {
			bChanged = true;
		} else if (event.getStateChange() == ItemEvent.DESELECTED) {
	        bChanged = true;
	    }
	}
    
	@Override
	public void changedUpdate(DocumentEvent arg0) {
		bChanged = true;
	}

	@Override
	public void insertUpdate(DocumentEvent arg0) {
		bChanged = true;
	}

	@Override
	public void removeUpdate(DocumentEvent arg0) {
		bChanged = true;
	}       

}
