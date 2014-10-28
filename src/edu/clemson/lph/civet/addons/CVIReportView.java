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
import java.awt.*;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.controls.*;
import edu.clemson.lph.db.*;
import edu.clemson.lph.dialogs.*;

import java.io.*;
import java.awt.event.*;

/**
 * <p>Title: SC Prem ID</p>
 * <p>Description: SC Premises Identification Data Hub</p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Clemson Livestock Poultry Health</p>
 * @author Michael K. Martin
 * @version 1.0
 */

@SuppressWarnings("serial")
public class CVIReportView extends JDialog implements TableModelListener {
	private JPanel panel1 = new JPanel();
	private BorderLayout borderLayout1 = new BorderLayout();
	private JPanel jpMain = new JPanel();
	private JPanel jpButtons = new JPanel();
	private JButton jbCancel = new JButton();
	private BorderLayout borderLayout2 = new BorderLayout();
	private JScrollPane spMain = new JScrollPane();
	private JTable tblResults = new JTable();
	private DatabaseConnectionFactory factory;
	private final JButton bSave = new JButton("Save to File");
	private CVIReportModel model;
	private java.util.Date dStart;
	private java.util.Date dEnd;
	private ProgressDialog prog;
	public static final Logger logger = Logger.getLogger(Civet.class.getName());


	public CVIReportView(DatabaseConnectionFactory factory, String title, java.util.Date dStart, java.util.Date dEnd ) {
		this.factory = factory;
		this.dStart = dStart;
		String sStart = DateField.dateToText(dStart);
		this.dEnd = dEnd;
		String sEnd = DateField.dateToText(dEnd);
		setTitle(title + ": " + sStart + " to " + sEnd);
		setModal(true);
		try {
			jbInit();
			pack();
		}
		catch(Exception ex) {
			logger.error(ex.getMessage() + "\nError in UNIDENTIFIED" );
		}
	}

	public CVIReportView() {
		this(null, "", DateField.textToDate("01/01/2012"), DateField.textToDate("07/01/2012"));
	}

	private String makeFileName() {
		StringBuffer sb = new StringBuffer();
		String sTitle = getTitle();
		for( int i = 0; i < sTitle.length(); i++ ) {
			char cNext = sTitle.charAt(i);
			if( Character.isLetterOrDigit(cNext))
				sb.append(cNext);
		}
		sb.append(".csv");
		return sb.toString();
	}

	private void doSaveFile() {
		JFileChooser fc = new JFileChooser();
		FileNameExtensionFilter filter;
		filter = new FileNameExtensionFilter(
				"CSV Files", "txt");
		fc.setFileFilter(filter);
		fc.setSelectedFile(new File( makeFileName() ) );
		int iRet = fc.showSaveDialog(this);
		if (iRet == JFileChooser.APPROVE_OPTION) {
			File fFile = fc.getSelectedFile();
			model.exportCSV( fFile );
		}
	}

	private void jbInit() throws Exception {
		panel1.setLayout(borderLayout1);
		jbCancel.setMaximumSize(new Dimension(75, 25));
		jbCancel.setMinimumSize(new Dimension(75, 25));
		jbCancel.setPreferredSize(new Dimension(75, 25));
		jbCancel.setText("Close");
		jbCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				setVisible(false);
			}
		});
		jpMain.setLayout(borderLayout2);
		getContentPane().add(panel1);
		panel1.add(jpMain,  BorderLayout.CENTER);
		panel1.add(jpButtons,  BorderLayout.SOUTH);
		bSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doSaveFile();
			}
		});
		bSave.setMaximumSize(new Dimension(125, 25));
		bSave.setMinimumSize(new Dimension(125, 25));
		bSave.setPreferredSize(new Dimension(125, 25));
		jpButtons.add(bSave);
		jpButtons.add(jbCancel, null);
		jpMain.add(spMain, BorderLayout.CENTER);
		model = new CVIReportModel( factory, dStart, dEnd );
		tblResults.setVisible(false);
		prog = new ProgressDialog( this, "USAHERDS: Civet", "Loading data" );
		prog.setAuto(true);
		prog.setVisible(true);
		model.addThreadListener( new ThreadListener() {
			@Override
			public void onThreadComplete(Thread thread) {
				tblResults.setVisible(true);
				prog.setVisible(false);
				prog.dispose();
			}
		});
		model.addTableModelListener( this );
		tblResults.setModel( model );
		tblResults.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		TableColumn col = tblResults.getColumnModel().getColumn(0);
		int width = 250;
		col.setPreferredWidth(width);
		model.refresh();
		spMain.setViewportView(tblResults);
		panel1.setPreferredSize(new Dimension(800,200));
		setLocation( 250, 250 );
	}

	public void setPanelSize( int iWidth, int iHeight ) {
		panel1.setPreferredSize(new Dimension( iWidth, iHeight ) );
	}

	public void setPanelWidth( int iWidth ) {
		int iHeight = panel1.getPreferredSize().height;
		panel1.setPreferredSize(new Dimension( iWidth, iHeight ) );
	}

	public void increasePanelWidth( double dMulti ) {
		int iHeight = panel1.getPreferredSize().height;
		int iWidth = (int)( (panel1.getPreferredSize().getWidth() * dMulti) );
		panel1.setPreferredSize(new Dimension( iWidth, iHeight ) );
	}

	void jbCancel_actionPerformed(ActionEvent e) {
		setVisible( false );
	}

	@Override
	public void tableChanged(TableModelEvent arg0) {
		TableColumn col = tblResults.getColumnModel().getColumn(0);
		int width = 250;
		col.setPreferredWidth(width);
	}

}
