package edu.clemson.lph.civet.addons;

import javax.swing.JPanel;

import org.apache.log4j.Logger;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.prefs.CivetConfig;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import java.awt.Font;
import java.awt.Insets;
import javax.swing.SwingConstants;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.JComboBox;

@SuppressWarnings("serial")
public class ParticipantPanel extends JPanel {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	static {
	     logger.setLevel(CivetConfig.getLogLevel());
	}
	JTextField jtfPIN;
	JTextField jtfBusiness;
	JTextField jtfName;
	JTextField jtfAddress;
	JTextField jtfCity;
	JTextField jtfZip;
	JComboBox<String> cbState;
	

	/**
	 * Create the panel.
	 */
	public ParticipantPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] {30, 0, 0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblConsignor = new JLabel("Consignor");
		lblConsignor.setFont(new Font("Tahoma", Font.PLAIN, 15));
		GridBagConstraints gbc_lblConsignor = new GridBagConstraints();
		gbc_lblConsignor.gridwidth = 2;
		gbc_lblConsignor.anchor = GridBagConstraints.WEST;
		gbc_lblConsignor.insets = new Insets(0, 0, 5, 5);
		gbc_lblConsignor.gridx = 1;
		gbc_lblConsignor.gridy = 1;
		add(lblConsignor, gbc_lblConsignor);
		
		JLabel lblNewLabel = new JLabel("Sticky?");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 3;
		gbc_lblNewLabel.gridy = 1;
		add(lblNewLabel, gbc_lblNewLabel);
		
		JCheckBox chkSticky = new JCheckBox("");
		GridBagConstraints gbc_chkSticky = new GridBagConstraints();
		gbc_chkSticky.anchor = GridBagConstraints.NORTH;
		gbc_chkSticky.insets = new Insets(0, 0, 5, 0);
		gbc_chkSticky.gridx = 4;
		gbc_chkSticky.gridy = 1;
		add(chkSticky, gbc_chkSticky);
		
		JLabel lblPin = new JLabel("PIN:");
		lblPin.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblPin = new GridBagConstraints();
		gbc_lblPin.anchor = GridBagConstraints.EAST;
		gbc_lblPin.insets = new Insets(0, 0, 5, 5);
		gbc_lblPin.gridx = 1;
		gbc_lblPin.gridy = 3;
		add(lblPin, gbc_lblPin);
		
		jtfPIN = new JTextField();
		GridBagConstraints gbc_jtfPIN = new GridBagConstraints();
		gbc_jtfPIN.anchor = GridBagConstraints.NORTH;
		gbc_jtfPIN.insets = new Insets(0, 0, 5, 5);
		gbc_jtfPIN.fill = GridBagConstraints.HORIZONTAL;
		gbc_jtfPIN.gridx = 2;
		gbc_jtfPIN.gridy = 3;
		add(jtfPIN, gbc_jtfPIN);
		jtfPIN.setColumns(8);
		
		JLabel lblBusiness = new JLabel("Business:");
		lblBusiness.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblBusiness = new GridBagConstraints();
		gbc_lblBusiness.anchor = GridBagConstraints.EAST;
		gbc_lblBusiness.insets = new Insets(0, 0, 5, 5);
		gbc_lblBusiness.gridx = 1;
		gbc_lblBusiness.gridy = 4;
		add(lblBusiness, gbc_lblBusiness);
		
		jtfBusiness = new JTextField();
		GridBagConstraints gbc_jtfBusiness = new GridBagConstraints();
		gbc_jtfBusiness.gridwidth = 3;
		gbc_jtfBusiness.insets = new Insets(0, 0, 5, 5);
		gbc_jtfBusiness.fill = GridBagConstraints.HORIZONTAL;
		gbc_jtfBusiness.gridx = 2;
		gbc_jtfBusiness.gridy = 4;
		add(jtfBusiness, gbc_jtfBusiness);
		jtfBusiness.setColumns(10);
		
		JLabel lblName = new JLabel("Name:");
		lblName.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.anchor = GridBagConstraints.EAST;
		gbc_lblName.insets = new Insets(0, 0, 5, 5);
		gbc_lblName.gridx = 1;
		gbc_lblName.gridy = 5;
		add(lblName, gbc_lblName);
		
		jtfName = new JTextField();
		GridBagConstraints gbc_jtfName = new GridBagConstraints();
		gbc_jtfName.gridwidth = 3;
		gbc_jtfName.insets = new Insets(0, 0, 5, 5);
		gbc_jtfName.fill = GridBagConstraints.HORIZONTAL;
		gbc_jtfName.gridx = 2;
		gbc_jtfName.gridy = 5;
		add(jtfName, gbc_jtfName);
		jtfName.setColumns(10);
		
		JLabel lblAddress = new JLabel("Address:");
		lblAddress.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblAddress = new GridBagConstraints();
		gbc_lblAddress.anchor = GridBagConstraints.EAST;
		gbc_lblAddress.insets = new Insets(0, 0, 5, 5);
		gbc_lblAddress.gridx = 1;
		gbc_lblAddress.gridy = 6;
		add(lblAddress, gbc_lblAddress);
		
		jtfAddress = new JTextField();
		GridBagConstraints gbc_jtfAddress = new GridBagConstraints();
		gbc_jtfAddress.gridwidth = 3;
		gbc_jtfAddress.insets = new Insets(0, 0, 5, 5);
		gbc_jtfAddress.fill = GridBagConstraints.HORIZONTAL;
		gbc_jtfAddress.gridx = 2;
		gbc_jtfAddress.gridy = 6;
		add(jtfAddress, gbc_jtfAddress);
		jtfAddress.setColumns(10);
		
		JLabel lblCity = new JLabel("City:");
		lblCity.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblCity = new GridBagConstraints();
		gbc_lblCity.anchor = GridBagConstraints.EAST;
		gbc_lblCity.insets = new Insets(0, 0, 5, 5);
		gbc_lblCity.gridx = 1;
		gbc_lblCity.gridy = 7;
		add(lblCity, gbc_lblCity);
		
		jtfCity = new JTextField();
		GridBagConstraints gbc_jtfCity = new GridBagConstraints();
		gbc_jtfCity.gridwidth = 3;
		gbc_jtfCity.insets = new Insets(0, 0, 5, 5);
		gbc_jtfCity.fill = GridBagConstraints.HORIZONTAL;
		gbc_jtfCity.gridx = 2;
		gbc_jtfCity.gridy = 7;
		add(jtfCity, gbc_jtfCity);
		jtfCity.setColumns(10);
		
		JLabel lblState = new JLabel("State:");
		lblState.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblState = new GridBagConstraints();
		gbc_lblState.anchor = GridBagConstraints.EAST;
		gbc_lblState.insets = new Insets(0, 0, 5, 5);
		gbc_lblState.gridx = 1;
		gbc_lblState.gridy = 8;
		add(lblState, gbc_lblState);
		
		cbState = new JComboBox<String>();
		GridBagConstraints gbc_cbState = new GridBagConstraints();
		gbc_cbState.gridwidth = 3;
		gbc_cbState.insets = new Insets(0, 0, 5, 5);
		gbc_cbState.fill = GridBagConstraints.HORIZONTAL;
		gbc_cbState.gridx = 2;
		gbc_cbState.gridy = 8;
		add(cbState, gbc_cbState);
		
		JLabel lblZip = new JLabel("Zip:");
		lblZip.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblZip = new GridBagConstraints();
		gbc_lblZip.anchor = GridBagConstraints.EAST;
		gbc_lblZip.insets = new Insets(0, 0, 5, 5);
		gbc_lblZip.gridx = 1;
		gbc_lblZip.gridy = 9;
		add(lblZip, gbc_lblZip);
		
		jtfZip = new JTextField();
		GridBagConstraints gbc_jtfZip = new GridBagConstraints();
		gbc_jtfZip.insets = new Insets(0, 0, 5, 5);
		gbc_jtfZip.fill = GridBagConstraints.HORIZONTAL;
		gbc_jtfZip.gridx = 2;
		gbc_jtfZip.gridy = 9;
		add(jtfZip, gbc_jtfZip);
		jtfZip.setColumns(10);
		
		JLabel lblCounty = new JLabel("County:");
		lblCounty.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblCounty = new GridBagConstraints();
		gbc_lblCounty.insets = new Insets(0, 0, 0, 5);
		gbc_lblCounty.anchor = GridBagConstraints.EAST;
		gbc_lblCounty.gridx = 1;
		gbc_lblCounty.gridy = 10;
		add(lblCounty, gbc_lblCounty);
		
		JComboBox cbCounty = new JComboBox();
		GridBagConstraints gbc_cbCounty = new GridBagConstraints();
		gbc_cbCounty.gridwidth = 3;
		gbc_cbCounty.insets = new Insets(0, 0, 0, 5);
		gbc_cbCounty.fill = GridBagConstraints.HORIZONTAL;
		gbc_cbCounty.gridx = 2;
		gbc_cbCounty.gridy = 10;
		add(cbCounty, gbc_cbCounty);

	}

}
