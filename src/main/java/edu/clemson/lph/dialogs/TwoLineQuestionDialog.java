package edu.clemson.lph.dialogs;
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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.Font;

public class TwoLineQuestionDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JFrame mDummyParent;
	private String mTitle;
	private int deltaX = 0;
	private int deltaY = 0;
	private boolean bOK = false;
	private String sQuestionOne;
	private String sAnswerOne;
	private String sQuestionTwo;
	private String sAnswerTwo;

	private JLabel lQuestionOne = new JLabel();
	private JTextField jtfAnswerOne = new JTextField();
	private JLabel lQuestionTwo = new JLabel();
	private JTextField jtfAnswerTwoClear = new JTextField();
	private JTextField jtfAnswerTwo = jtfAnswerTwoClear;
	private JPasswordField jtfPassword = new JPasswordField();
	private boolean bPassword = false;
	private String sIntro;
	private JLabel lIntro;

	/**
	@wbp.parser.constructor 
	 */
	public TwoLineQuestionDialog( String sTitle, String sQuestionOne, String sQuestionTwo, boolean bModal ) {
		super( new JFrame( sTitle ) );
		mDummyParent = (JFrame)super.getParent();
		// Make sure dummy window isn't on screen.
		mDummyParent.setLocation(-100,-100);
		setModal(bModal);
		mTitle = sTitle;
		this.sQuestionOne = sQuestionOne;
		this.sQuestionTwo = sQuestionTwo;
		initGui();
	}

	public TwoLineQuestionDialog( Window parent, String sTitle, String sQuestionOne, String sQuestionTwo, boolean bModal ) {
		super( parent );
		setModal(bModal);
		mDummyParent = null;
		mTitle = sTitle;
		this.sQuestionOne = sQuestionOne;
		this.sQuestionTwo = sQuestionTwo;
		initGui();
	}

	/**
	 * Turns the second input into a password (dots instead of letters) input to
	 * prevent shoulder surfing
	 * @param bPassword  True to obscure second answer box
	 */
	public void setPassword( boolean bPassword ) {
		this.bPassword = bPassword;
		// slight of hand to replace answer two with a password field
		// but still get the answer.
		if( bPassword ) {
			jtfAnswerTwo.setVisible(false);
			jtfAnswerTwo = jtfPassword;
			jtfPassword.setVisible(true);
		}
		else {
			jtfPassword.setVisible(false);
			jtfAnswerTwo = jtfAnswerTwoClear;  // Just in case anyone ever changes their mind.
			jtfAnswerTwo.setVisible(true);
		}
	}
	
	public void setVisible( boolean bVis ) {
		if( mDummyParent != null && mDummyParent.isVisible() ) {
			mDummyParent.setVisible(bVis);
			if(!bVis ) {
				mDummyParent.dispose();
			}
		}
		super.setVisible( bVis );
	}

	public boolean isPassword() { return bPassword; }

	public void setAnswerOne(String sAnswerOne) { this.sAnswerOne = sAnswerOne; jtfAnswerOne.setText(sAnswerOne); }
	public String getAnswerOne() { return sAnswerOne; }
	public void setAnswerTwo(String sAnswerTwo) { this.sAnswerTwo = sAnswerTwo; jtfAnswerTwo.setText(sAnswerTwo); }
	public String getAnswerTwo() { return sAnswerTwo; }

	public void setDeltas( int deltaX, int deltaY ) {
		this.deltaX = deltaX;
		this.deltaY = deltaY;
	}
	
	public void setIntro( String sIntro ) {
		this.sIntro = sIntro;
		lIntro.setText(sIntro);
	}

	public boolean isExitOK() {
		return bOK;
	}

	public void center() {
		//Center the window
		boolean bSmall = false;
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = getSize();
		if( frameSize.height > screenSize.height ) {
			frameSize.height = screenSize.height;
			bSmall = true;
		}
		if( frameSize.width > screenSize.width ) {
			frameSize.width = screenSize.width;
			bSmall = true;
		}
		if( bSmall ) {
			setLocation( (screenSize.width - frameSize.width) / 2, 0);
		}
		else {
			setLocation( deltaX + (screenSize.width - frameSize.width) / 2,
					deltaY + (screenSize.height - frameSize.height) / 2);
		}
	}

	/**
	 * Create the dialog.
	 */
	public void initGui() {
		setBounds(100, 100, 450, 300);
		setTitle(mTitle);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);

		lQuestionOne.setText(sQuestionOne);
		lQuestionOne.setBounds(27, 83, 337, 14);
		contentPanel.add(lQuestionOne);
		jtfAnswerOne.setText("");
		jtfAnswerOne.setBounds(27, 108, 337, 20);
		contentPanel.add(jtfAnswerOne);
		lQuestionTwo.setText(sQuestionTwo);
		lQuestionTwo.setBounds(27, 139, 337, 14);
		contentPanel.add(lQuestionTwo);
		jtfAnswerTwo.setText("");
		jtfAnswerTwo.setBounds(27, 164, 337, 20);
		contentPanel.add(jtfAnswerTwo);
		jtfPassword.setText("");
		jtfPassword.setBounds(27, 164, 337, 20);
		jtfPassword.setVisible(false);
		contentPanel.add(jtfPassword);
		
		lIntro = new JLabel(sIntro);
		lIntro.setFont(new Font("Tahoma", Font.BOLD, 14));
		lIntro.setBounds(27, 35, 337, 30);
		contentPanel.add(lIntro);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						bOK = true;
						sAnswerOne = jtfAnswerOne.getText();
						sAnswerTwo = jtfAnswerTwo.getText();
						TwoLineQuestionDialog.this.setVisible(false);
					}
				});
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						bOK = false;
						TwoLineQuestionDialog.this.setVisible(false);
					}
				});
				buttonPane.add(cancelButton);
			}
		}
	}
}
