package edu.clemson.lph.civet; 
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
import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import edu.clemson.lph.civet.lookup.SpeciesLookup;
import edu.clemson.lph.civet.xml.elements.Animal;
import edu.clemson.lph.civet.xml.elements.AnimalTag;
import edu.clemson.lph.controls.DBNumericField;
import edu.clemson.lph.dialogs.MessageDialog;
import edu.clemson.lph.utils.ClipboardUtils;
import edu.clemson.lph.utils.IDTypeGuesser;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.apache.log4j.Logger;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

@SuppressWarnings("serial")
public class AddAnimalsDialog extends JDialog {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
	private final JPanel contentPanel = new JPanel();
	private JTextField jtfNewId;
	private DBNumericField jtfAddNum;
	private JTable tblIDs;
	private AnimalIDListTableModel tableModel;
	ArrayList<SpeciesRecord> aSpecies;
	private JComboBox<String> cbSpecies;
	private JTextField jtfPrefix;
	private int iDefaultWidth = 675;
	private int iLongSpeciesLen = 7;


	/**
	 * Create the dialog.
	 */
	public AddAnimalsDialog( ArrayList<SpeciesRecord> aSpecies, AnimalIDListTableModel tableModel ) {
		this.aSpecies = aSpecies;
		this.tableModel = tableModel;
		ImageIcon appIcon = new ImageIcon(getClass().getResource("/edu/clemson/lph/civet/res/civet32.png"));
		this.setIconImage(appIcon.getImage());
		setTitle("Civet: Add Animal IDs");
		setBounds(500, 400, iDefaultWidth, 400);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JPanel panel = new JPanel();
			contentPanel.add(panel, BorderLayout.NORTH);
			{
				JLabel lblSpecies = new JLabel("Species");
				panel.add(lblSpecies);
			}
			{
				cbSpecies = new JComboBox<String>();
				for( SpeciesRecord rSpecies : aSpecies ) {
					String sSpeciesCode = rSpecies.sSpeciesCode;
					String sSpeciesName = SpeciesLookup.getNameForCode(sSpeciesCode);
					if( sSpeciesName.trim().length() > iLongSpeciesLen ) {
						int iAdd = (sSpeciesName.trim().length() - iLongSpeciesLen) * 15;
						setBounds(500, 400, iDefaultWidth + iAdd, 400);
					}
					cbSpecies.addItem(sSpeciesName);
				}
				panel.add(cbSpecies);
			}
			{
				jtfPrefix = new JTextField();
				jtfPrefix.setToolTipText("<html>ID prefix.  This value will not be erased between IDs.<br>If it is '840' or '982', 15 digit EID logic will be used to pad prefix + ID.</html>");
				panel.add(jtfPrefix);
				jtfPrefix.setColumns(10);
			}
			{
				jtfNewId = new JTextField();
				jtfNewId.setToolTipText("<html>ID or suffix.  If it ends in digits, it can be starting point of a sequence.<br>The Enter key pressed here adds this ID and returns here.</html>");
				jtfNewId.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						doEnter();
					}
				});
				jtfNewId.addKeyListener( new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						if( e.getKeyCode() == KeyEvent.VK_V 
								&& ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)
								&& ((e.getModifiers() & KeyEvent.SHIFT_MASK) != 0)) {
							pasteIDs();
						}
					}
				});

				panel.add(jtfNewId);
				jtfNewId.setColumns(25);
			}
			{
				JButton btnAdd = new JButton("Add");
				panel.add(btnAdd);
				btnAdd.addActionListener( new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						jtfAddNum.setText("");
						doEnter();
					}
				});
			}
			{
				jtfAddNum = new DBNumericField();
				jtfAddNum.setToolTipText("<html>Number of tags to generate.<br>The Enter key pressed here adds this number of IDs and returns to the ID field.</html>");
				jtfAddNum.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						doEnter();
					}
				});
				panel.add(jtfAddNum);
				jtfAddNum.setColumns(2);
			}
			{
				JButton btnAddNum = new JButton("Add #");
				panel.add(btnAddNum);
				{
					JButton btnPasteIds = new JButton("Paste IDs");
					btnPasteIds.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							pasteIDs();
						}
					});
					panel.add(btnPasteIds);
				}
				btnAddNum.addActionListener( new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						addIDs();
					}
				});
			}
		}
		{
			JScrollPane scrollPane = new JScrollPane();
			contentPanel.add(scrollPane, BorderLayout.CENTER);
			{
				tblIDs = new JTable();
				tblIDs.setModel( tableModel );
				scrollPane.setViewportView(tblIDs);
				tblIDs.addKeyListener( new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						if( e.getKeyCode() == KeyEvent.VK_DELETE ) {
							int aRows[] = tblIDs.getSelectedRows();
							AddAnimalsDialog.this.tableModel.deleteRows( aRows );
						}
					}
				});
				tblIDs.getColumnModel().getColumn(0).setPreferredWidth(30);
				tblIDs.getColumnModel().getColumn(0).setMinWidth(30);
				tblIDs.getColumnModel().getColumn(0).setMaxWidth(30);
				tblIDs.getColumnModel().getColumn(1).setPreferredWidth(130);
				tblIDs.getColumnModel().getColumn(1).setMaxWidth(130);
				
				TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(tblIDs.getModel());
				tblIDs.setRowSorter(sorter);
//				List<RowSorter.SortKey> sortKeys = new ArrayList<>(25);
//				sortKeys.add(new RowSorter.SortKey(2, SortOrder.ASCENDING));
//				sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
//				sorter.setSortKeys(sortKeys);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
				okButton.addActionListener( new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						doSave();
					}
				});
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
				cancelButton.addActionListener( new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						setVisible( false );
					}
				});
			}
		}
	}
	
	public String getSelectedSpecies() {
		String sSpecies = (String)cbSpecies.getSelectedItem();
		return sSpecies;
	}
	
	public static String padTag( String sPrefix, String sSuffix, int iChars ) {
		if( sPrefix == null || sSuffix == null ) return null;
		StringBuffer sb = new StringBuffer();
		int iLen = sPrefix.length() + sSuffix.length();
		sb.append(sPrefix);
		while( iLen < iChars ) {
			sb.append('0');
			iLen++;
		}
		sb.append(sSuffix);
		return sb.toString();
	}

	
	private void addID() {
		String sPrefix = jtfPrefix.getText().trim();
		String sSuffix = jtfNewId.getText().trim();
		String sID = null;
		if( "840".equals(sPrefix) || "982".equals(sPrefix) )
			sID = padTag(sPrefix, sSuffix, 15);
		else
			sID = sPrefix + sSuffix;
		String sSpecies = (String)cbSpecies.getSelectedItem();
		String sSpeciesCode = SpeciesLookup.getCodeForName( sSpecies );
//		int iNextRow = model.getMaxRowID() + 1;
		AnimalTag.Types type = IDTypeGuesser.getTagType(sID);
		Animal a = new Animal( sSpeciesCode, type, sID );
		if( sID != null && sID.trim().length() > 0 ) {
			tableModel.addRow(a);
		}
		jtfNewId.setText("");
		jtfNewId.requestFocus();
	}
	
	private void doEnter() {
		if( jtfNewId.getText().trim().length() > 0 ) {
			if( jtfAddNum.getText().trim().length() > 0 ) {
				// Do add number
				addIDs();
			}
			else {
				// Do add
				addID();
			}
		}
		else {
			doSave();
		}
	}
	
	private void doSave() {
		tableModel.save();
		setVisible( false );
	}
	
	private void addIDs() {
		String sPrefix = jtfPrefix.getText().trim();
		String sID = jtfNewId.getText().trim();
		int iNum = -1;
		try {
			iNum = Integer.parseInt(jtfAddNum.getText());
		} catch( NumberFormatException nfe ) {
			MessageDialog.showMessage(this, "Civet Error: Number Format", "Cannot parse " + jtfAddNum.getText() + " as number to add");
			jtfNewId.requestFocus();
			return;
		}
		String sSpecies = (String)cbSpecies.getSelectedItem();
		String sSpeciesCode = SpeciesLookup.getCodeForName( sSpecies );
		if( sID != null && sID.trim().length() > 0 && iNum > 0 ) {
			try {
				ArrayList<String> sIDs = null;
				try {
					sIDs = IdListGen.getIds( sPrefix, sID, iNum );
				}
				catch( NumberFormatException nfe ) {
					MessageDialog.showMessage(this, "Civet Error: Number Format", nfe.getMessage() );
					jtfNewId.requestFocus();
					return;
				}
				catch( Exception e ) {
					MessageDialog.showMessage(this, "Civet Error: Error", e.getMessage() );
					jtfNewId.requestFocus();
					return;
				}
				for( String sNext : sIDs ) {
					AnimalTag.Types type = IDTypeGuesser.getTagType(sNext);
					Animal a = new Animal( sSpeciesCode, type, sNext );
					tableModel.addRow(a);
				}
			} catch (Exception e) {
				logger.error("Failed to Add Multiple Animals", e);
			}
			jtfNewId.setText("");
		}
		jtfNewId.requestFocus();
	}
	
	private void pasteIDs() {
		List<String> sIDs = ClipboardUtils.getClipStringList();
		if( sIDs == null || sIDs.size() == 0 ) return;
		String sSpecies = (String)cbSpecies.getSelectedItem();
		String sSpeciesCode = SpeciesLookup.getCodeForName( sSpecies );
		for( String sID : sIDs ) {
			Animal a = new Animal( sSpeciesCode, sID );
			if( sID != null && sID.trim().length() > 0 ) {
				tableModel.addRow(a);
			}
		}
	}

}
