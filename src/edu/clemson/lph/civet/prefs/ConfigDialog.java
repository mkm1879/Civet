package edu.clemson.lph.civet.prefs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.UIManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.JTabbedPane;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.mailman.MailMan;

@SuppressWarnings("serial")
public class ConfigDialog extends JDialog {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());

	private JTabbedPane tpTabs;
	private ArrayList<ConfigEntryPanel> aEntries = null;
	private Properties props = null;
	private String sTitleBase = "Civet: Configuration Settings (edits CivetConfig.txt)";

	private boolean bComplete;
	private boolean bExitOK;

	/**
	 * Launch the application.  Testing only!
	 */
	public static void main(String[] args) {
		try {
			String os = System.getProperty("os.name");
			if( os.toLowerCase().contains("mac os") ) {
				System.setProperty("apple.laf.useScreenMenuBar", "true");
				System.setProperty("com.apple.mrj.application.apple.menu.about.name",
						"Civet");
				System.setProperty("com.apple.mrj.application.growbox.intrudes",
						"false");
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			}
			else {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
		} 
		catch (Exception e) {
			logger.error(e.getMessage());
		}
		try {
			ConfigDialog dialog = new ConfigDialog();
			dialog.setModal(true);;
			dialog.setVisible(true);
			if( dialog.isComplete() ) System.out.println("Good to go");
			else System.out.println( "Incomplete");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public ConfigDialog() {
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		CivetConfig.initConfig();
		props = CivetConfig.getProps();
		aEntries = new ArrayList<ConfigEntryPanel>();
		bExitOK = false;
		setBounds(100, 100, 650, 450);
		getContentPane().setLayout(new BorderLayout());
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
				okButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						saveConfig();
					}
				});
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
				cancelButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cancelConfig();
					}
				});
			}
		}
		{
			tpTabs = new JTabbedPane(JTabbedPane.TOP);
			getContentPane().add(tpTabs, BorderLayout.CENTER);
			addTabs();
		}
		updateStatus();
	}
	
	void updateStatus() {
		bComplete = true;
		for( ConfigEntryPanel pNext : aEntries) {
			if( pNext.isMandatory() ) {
				if( pNext.getValue() == null || pNext.getValue().trim().length() == 0) {
					bComplete = false;
				}
			}
		}
		String sStatus = null;
		if( bComplete ) sStatus = " (Complete)";
		else sStatus = " (Missing Required Values)";
		setTitle(sTitleBase + sStatus);
	}
	
	protected void cancelConfig() {
		bComplete = true;
		for( ConfigEntryPanel pNext : aEntries) {
			if( pNext.isMandatory() ) {
				if( pNext.getValue() == null || pNext.getValue().trim().length() == 0) {
					bComplete = false;
				}
			}
		}
		setVisible(false);
		bExitOK = false;
		dispose();
	}

	protected void saveConfig() {
		bComplete = true;
		for( ConfigEntryPanel pNext : aEntries) {
			if( pNext.hasChanged() ) {
				props.put(pNext.getName(), pNext.getValue());
			}
			if( pNext.isMandatory() ) {
				if( pNext.getValue() == null || pNext.getValue().trim().length() == 0) {
					bComplete = false;
				}
			}
		}
		if( !CivetConfig.saveProps() ) {
			logger.error("Faild to save Config properties");
		}
		bExitOK = true;
		// In case the user has already entered password, save it
		String sMailUser = MailMan.getDefaultUserID();
		String sMailPass = MailMan.getDefaultPassword();
		CivetConfig.initEmail(false);
		MailMan.setDefaultUserID(sMailUser);
		MailMan.setDefaultPassword(sMailPass);
		setVisible(false);
		dispose();
	}

	private void addTabs() {
		ConfigCsv me = new ConfigCsv();
		String sTab = null;
		while( (sTab = me.nextTab()) != null ) {
			int iRows = me.getCurrentTabSize();
			ConfigTabPanel  pTab = new ConfigTabPanel();
			tpTabs.addTab(sTab, null, pTab, null );
			for( int i = 0; i < iRows; i++ ) {
				if(	me.nextRow() != null ) {
					addRow(me, pTab);
				}
			}
		} 
	}
	
	/**
	 * True if any mandatory fields were blank on last save or cancel.
	 * @return boolean true if ready to use config.
	 */
	public boolean isComplete() {
		return bComplete;
	}
	
	private void addRow( ConfigCsv me, ConfigTabPanel pTab ) {
		final String sName = me.getName();
		final String sType = me.getType();
		final String sHelp = me.getDescription();
		final String sValue = props.getProperty(sName);
		boolean bMandatory = me.isMandatory();
		List<String> lChoices = null;
		ConfigEntry entry = new ConfigEntry( sName, bMandatory, sValue, sType, sHelp );
		if( "Select".equalsIgnoreCase(sType) ) {
			lChoices = me.getChoices();
		}
		ConfigEntryPanel pEntry = new ConfigEntryPanel( this, entry, lChoices );
		aEntries.add(pEntry);
		pTab.addEntry(pEntry);
	}

}
