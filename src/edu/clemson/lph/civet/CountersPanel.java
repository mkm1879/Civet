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
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.FlowLayout;

@SuppressWarnings("serial")
public class CountersPanel extends JPanel {
	private CivetEditDialog parent;
	private JPanel pCounters;
	private JLabel fileCounter1;
	private JLabel fileCounter2;
	private JLabel pageCounter1;
	private JLabel pageCounter2;
	private JButton bFileBack;
	private JButton bFileForward;
	private JButton bPageBack;
	private JButton bPageForward;

	/**
	 * Create the panel.
	 */
	public CountersPanel(CivetEditDialog parent) {
		this.parent = parent;
		setLayout( new FlowLayout() );
		
		this.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				CountersPanel.this.parent.doPickPage(e);
			}
		});
		
		/** file back icon */
		bFileBack = new JButton();
		bFileBack.setBorderPainted(false);
		bFileBack.setBorder(null);
		bFileBack.setMargin(new Insets(0,0,0,0));

		bFileBack.setIcon(new ImageIcon(getClass().getResource("/edu/clemson/lph/civet/res/back.gif")));
		bFileBack.setToolTipText("Rewind one file");
		add(bFileBack);
		bFileBack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CountersPanel.this.parent.getController().fileBackward();
			}
		});
		
		/**page back icon*/
		bPageBack = new JButton();
		bPageBack.setBorderPainted(false);
		bPageBack.setBorder(null);
		bPageBack.setMargin(new Insets(0,0,0,0));
		bPageBack.setBorderPainted(false);
		bPageBack.setIcon(new ImageIcon(getClass().getResource("/edu/clemson/lph/civet/res/pageBack.gif")));
		bPageBack.setToolTipText("Rewind one page");
		add(bPageBack);
		bPageBack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CountersPanel.this.parent.getController().pageBack();
			}
		});

		pCounters = new JPanel();
		pCounters.setLayout(new GridLayout(2, 0, 0, 0));

		JPanel pFileCounter = new JPanel();
		FlowLayout flowLayout = (FlowLayout) pFileCounter.getLayout();
		flowLayout.setVgap(0);
		flowLayout.setHgap(0);
		
		/** File Counters */
		fileCounter1=new JLabel("File 1");
		fileCounter1.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				CountersPanel.this.parent.doPickPage(e);
			}
		});
		pFileCounter.add(fileCounter1);
		
		fileCounter2=new JLabel(" of 1");//000 used to set prefered size
		fileCounter2.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				CountersPanel.this.parent.doPickPage(e);
			}
		});
		pFileCounter.add(fileCounter2);
		pCounters.add(pFileCounter);
		this.add(pCounters);
		

		/** Page Counters */
		JPanel pPageCounter = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) pPageCounter.getLayout();
		flowLayout_1.setVgap(0);
		flowLayout_1.setHgap(0);
		pageCounter1=new JLabel("Page 1");
		pageCounter1.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				CountersPanel.this.parent.doPickPage(e);
			}
		});
		pPageCounter.add(pageCounter1);
		
		pageCounter2=new JLabel(" of 1");//000 used to set prefered size
		pageCounter2.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				CountersPanel.this.parent.doPickPage(e);
			}
		});
		pPageCounter.add(pageCounter2);
		pCounters.add(pPageCounter);
		
		/**page forward icon*/
		bPageForward = new JButton();
		bPageForward.setBorderPainted(false);
		bPageForward.setBorder(null);
		bPageForward.setBorderPainted(false);
		bPageForward.setMargin(new Insets(0,0,0,0));

		bPageForward.setBorderPainted(false);
		bPageForward.setIcon(new ImageIcon(getClass().getResource("/edu/clemson/lph/civet/res/pageForward.gif")));
		bPageForward.setToolTipText("Forward one page");
		add(bPageForward);
		bPageForward.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CountersPanel.this.parent.getController().pageForward();
			}
		});

		/**file forward icon*/
		bFileForward = new JButton();
		bFileForward.setBorderPainted(false);
		bFileForward.setBorder(null);
		bFileForward.setBorderPainted(false);
		bFileForward.setMargin(new Insets(0,0,0,0));
		bFileForward.setBorderPainted(false);
		bFileForward.setIcon(new ImageIcon(getClass().getResource("/edu/clemson/lph/civet/res/forward.gif")));
		bFileForward.setToolTipText("Forward one file");
		add(bFileForward);
		bFileForward.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CountersPanel.this.parent.getController().fileForward();
			}
		});
		
	}
	
	public void setPageBackEnabled( boolean bEnabled ) {
		bPageBack.setEnabled(bEnabled);
	}
	
	public void setFileBackEnabled( boolean bEnabled ) {
		bFileBack.setEnabled(bEnabled);
	}
	
	public void setPageForwardEnabled( boolean bEnabled ) {
		bPageForward.setEnabled(bEnabled);
	}
	
	public void setFileForwardEnabled( boolean bEnabled ) {
		bFileForward.setEnabled(bEnabled);
	}
	
	public void setPage( int iPageNo ) {
		pageCounter1.setText("Page " + Integer.toString(iPageNo));
	}
	
	public void setPages( int iPageCount ) {
		pageCounter2.setText(" of " + iPageCount);
	}

	public void setFile( int iFileNo ) {
		fileCounter1.setText("File " + Integer.toString(iFileNo));
	}
	
	public void setFiles( int iFileCount ) {
		fileCounter2.setText(" of " + iFileCount);
	}


}
