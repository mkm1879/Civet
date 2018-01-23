package edu.clemson.lph.civet.lookup;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.prefs.CivetConfig;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class StateVetTableEditDlg extends JDialog {
	private final JPanel pSelectState = new JPanel();
	private ImageIcon appIcon;
	private JComboBox<String> cbSelectedState;
	private JTextField jtfPrefix;
	private JTextField jtfFirstName;
	private JTextField jtfLastName;
	private JTextField jtfAddress;
	private JTextField jtfCity;
	private JTextField jtfStateCode;
	private JTextField jtfState;
	private JTextField jtfZipCode;
	private JTextField jtfEmail;
	private JTextField jtfCVIEmail;
	private JTextField jtfCVIErrorEmail;
	private JComboBox<String> cbFileType;
	private StateVetLookup lu;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			PropertyConfigurator.configure("CivetConfig.txt");
			CivetConfig.checkAllConfig();

			StateVetTableEditDlg dialog = new StateVetTableEditDlg();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public StateVetTableEditDlg() {
		this.setModal(true);
		initGui();
		initDB();
	}
	
	private void initGui() {
		setBounds(100, 100, 450, 488);
		setTitle("Civet: State Veterinarian Table Editor");
		appIcon = new ImageIcon(getClass().getResource("/edu/clemson/lph/civet/res/civet32.png"));
		this.setIconImage(appIcon.getImage());
		getContentPane().setLayout(new BorderLayout());
		{
			getContentPane().add(pSelectState, BorderLayout.NORTH);
			{
				JLabel lblState = new JLabel("State:");
				lblState.setHorizontalAlignment(SwingConstants.RIGHT);
				pSelectState.add(lblState);
			}
			{
				cbSelectedState = new JComboBox<String>();
				cbSelectedState.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						doUpdateData();
						doFillForm();
					}
				});
				{
					JButton bPrev = new JButton("<");
					bPrev.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							doPrev();
						}
					});
					pSelectState.add(bPrev);
				}
				pSelectState.add(cbSelectedState);
			}
			{
				JButton bNext = new JButton(">");
				bNext.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						doNext();
					}
				});
				pSelectState.add(bNext);
			}
			{
			JScrollPane spContent = new JScrollPane();
			getContentPane().add(spContent, BorderLayout.CENTER);
			{
				JPanel pContent = new JPanel();
				Border border = new EmptyBorder(10,10,10,10);
				pContent.setBorder(border);
				spContent.setViewportView(pContent);
				GridBagLayout gbl_pContent = new GridBagLayout();
				gbl_pContent.columnWidths = new int[]{0, 0, 0, 0};
				gbl_pContent.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
				gbl_pContent.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
				gbl_pContent.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
				pContent.setLayout(gbl_pContent);
				{
					JLabel lblState = new JLabel("State:");
					GridBagConstraints gbc_lblState = new GridBagConstraints();
					gbc_lblState.anchor = GridBagConstraints.EAST;
					gbc_lblState.insets = new Insets(0, 0, 5, 5);
					gbc_lblState.gridx = 1;
					gbc_lblState.gridy = 1;
					pContent.add(lblState, gbc_lblState);
				}
				{
					jtfState = new JTextField();
					GridBagConstraints gbc_jtfState = new GridBagConstraints();
					gbc_jtfState.insets = new Insets(0, 0, 5, 0);
					gbc_jtfState.fill = GridBagConstraints.HORIZONTAL;
					gbc_jtfState.gridx = 2;
					gbc_jtfState.gridy = 1;
					pContent.add(jtfState, gbc_jtfState);
					jtfState.setColumns(10);
					jtfState.setEditable(false);
				}
				{
					JLabel lblPrefix = new JLabel("Prefix:");
					GridBagConstraints gbc_lblPrefix = new GridBagConstraints();
					gbc_lblPrefix.anchor = GridBagConstraints.EAST;
					gbc_lblPrefix.insets = new Insets(0, 0, 5, 5);
					gbc_lblPrefix.gridx = 1;
					gbc_lblPrefix.gridy = 2;
					pContent.add(lblPrefix, gbc_lblPrefix);
				}
				{
					jtfPrefix = new JTextField();
					GridBagConstraints gbc_jtfPrefix = new GridBagConstraints();
					gbc_jtfPrefix.insets = new Insets(0, 0, 5, 0);
					gbc_jtfPrefix.fill = GridBagConstraints.HORIZONTAL;
					gbc_jtfPrefix.gridx = 2;
					gbc_jtfPrefix.gridy = 2;
					pContent.add(jtfPrefix, gbc_jtfPrefix);
					jtfPrefix.setColumns(10);
				}
				{
					JLabel lblLastName = new JLabel("Last Name:");
					GridBagConstraints gbc_lblLastName = new GridBagConstraints();
					gbc_lblLastName.anchor = GridBagConstraints.EAST;
					gbc_lblLastName.insets = new Insets(0, 0, 0, 5);
					gbc_lblLastName.gridx = 1;
					gbc_lblLastName.gridy = 3;
					pContent.add(lblLastName, gbc_lblLastName);
				}
				{
					jtfLastName = new JTextField();
					GridBagConstraints gbc_jtfLastName = new GridBagConstraints();
					gbc_jtfLastName.fill = GridBagConstraints.HORIZONTAL;
					gbc_jtfLastName.insets = new Insets(0, 0, 5, 0);
					gbc_jtfLastName.gridx = 2;
					gbc_jtfLastName.gridy = 3;
					pContent.add(jtfLastName, gbc_jtfLastName);
					jtfLastName.setColumns(10);
				{
					JLabel lblFirstName = new JLabel("First Name:");
					GridBagConstraints gbc_lblFirstName = new GridBagConstraints();
					gbc_lblFirstName.anchor = GridBagConstraints.EAST;
					gbc_lblFirstName.insets = new Insets(0, 0, 0, 5);
					gbc_lblFirstName.gridx = 1;
					gbc_lblFirstName.gridy = 4;
					pContent.add(lblFirstName, gbc_lblFirstName);
				}
				{
					jtfFirstName = new JTextField();
					GridBagConstraints gbc_jtfFirstName = new GridBagConstraints();
					gbc_jtfFirstName.insets = new Insets(0, 0, 5, 0);
					gbc_jtfFirstName.fill = GridBagConstraints.HORIZONTAL;
					gbc_jtfFirstName.gridx = 2;
					gbc_jtfFirstName.gridy = 4;
					pContent.add(jtfFirstName, gbc_jtfFirstName);
					jtfFirstName.setColumns(10);
				}
//				private JTextField jtfAddress;
				{
					JLabel lblAddress = new JLabel("Address:");
					GridBagConstraints gbc_lblAddress = new GridBagConstraints();
					gbc_lblAddress.anchor = GridBagConstraints.EAST;
					gbc_lblAddress.insets = new Insets(0, 0, 0, 5);
					gbc_lblAddress.gridx = 1;
					gbc_lblAddress.gridy = 5;
					pContent.add(lblAddress, gbc_lblAddress);
				}
				{
					jtfAddress = new JTextField();
					GridBagConstraints gbc_jtfAddress = new GridBagConstraints();
					gbc_jtfAddress.insets = new Insets(0, 0, 5, 0);
					gbc_jtfAddress.fill = GridBagConstraints.HORIZONTAL;
					gbc_jtfAddress.gridx = 2;
					gbc_jtfAddress.gridy = 5;
					pContent.add(jtfAddress, gbc_jtfAddress);
					jtfAddress.setColumns(10);
				}

//				private JTextField jtfCity;
				{
					JLabel lblCity = new JLabel("City:");
					GridBagConstraints gbc_lblCity = new GridBagConstraints();
					gbc_lblCity.anchor = GridBagConstraints.EAST;
					gbc_lblCity.insets = new Insets(0, 0, 0, 5);
					gbc_lblCity.gridx = 1;
					gbc_lblCity.gridy = 6;
					pContent.add(lblCity, gbc_lblCity);
				}
				{
					jtfCity = new JTextField();
					GridBagConstraints gbc_jtfCity = new GridBagConstraints();
					gbc_jtfCity.insets = new Insets(0, 0, 5, 0);
					gbc_jtfCity.fill = GridBagConstraints.HORIZONTAL;
					gbc_jtfCity.gridx = 2;
					gbc_jtfCity.gridy = 6;
					pContent.add(jtfCity, gbc_jtfCity);
					jtfCity.setColumns(10);
				}
//				private JTextField jtfStateCode;
				{
					JLabel lblStateCode = new JLabel("StateCode:");
					GridBagConstraints gbc_lblStateCode = new GridBagConstraints();
					gbc_lblStateCode.anchor = GridBagConstraints.EAST;
					gbc_lblStateCode.insets = new Insets(0, 0, 0, 5);
					gbc_lblStateCode.gridx = 1;
					gbc_lblStateCode.gridy = 7;
					pContent.add(lblStateCode, gbc_lblStateCode);
				}
				{
					jtfStateCode = new JTextField();
					GridBagConstraints gbc_jtfStateCode = new GridBagConstraints();
					gbc_jtfStateCode.insets = new Insets(0, 0, 5, 0);
					gbc_jtfStateCode.fill = GridBagConstraints.HORIZONTAL;
					gbc_jtfStateCode.gridx = 2;
					gbc_jtfStateCode.gridy = 7;
					pContent.add(jtfStateCode, gbc_jtfStateCode);
					jtfStateCode.setColumns(10);
					jtfStateCode.setEditable(false);
				}
//				private JTextField jtfZipCode;
				{
					JLabel lblZipCode = new JLabel("Zip Code:");
					GridBagConstraints gbc_lblZipCode = new GridBagConstraints();
					gbc_lblZipCode.anchor = GridBagConstraints.EAST;
					gbc_lblZipCode.insets = new Insets(0, 0, 0, 5);
					gbc_lblZipCode.gridx = 1;
					gbc_lblZipCode.gridy = 8;
					pContent.add(lblZipCode, gbc_lblZipCode);
				}
				{
					jtfZipCode = new JTextField();
					GridBagConstraints gbc_jtfZipCode = new GridBagConstraints();
					gbc_jtfZipCode.insets = new Insets(0, 0, 5, 0);
					gbc_jtfZipCode.fill = GridBagConstraints.HORIZONTAL;
					gbc_jtfZipCode.gridx = 2;
					gbc_jtfZipCode.gridy = 8;
					pContent.add(jtfZipCode, gbc_jtfZipCode);
					jtfZipCode.setColumns(10);
				}
//				private JTextField jtfEmail;
				{
					JLabel lblEmail = new JLabel("EMail:");
					GridBagConstraints gbc_lblEmail = new GridBagConstraints();
					gbc_lblEmail.anchor = GridBagConstraints.EAST;
					gbc_lblEmail.insets = new Insets(0, 0, 0, 5);
					gbc_lblEmail.gridx = 1;
					gbc_lblEmail.gridy = 9;
					pContent.add(lblEmail, gbc_lblEmail);
				}
				{
					jtfEmail = new JTextField();
					GridBagConstraints gbc_jtfEmail = new GridBagConstraints();
					gbc_jtfEmail.insets = new Insets(0, 0, 5, 0);
					gbc_jtfEmail.fill = GridBagConstraints.HORIZONTAL;
					gbc_jtfEmail.gridx = 2;
					gbc_jtfEmail.gridy = 9;
					pContent.add(jtfEmail, gbc_jtfEmail);
					jtfEmail.setColumns(10);
				}
//				private JTextField jtfCVIEmail;
				{
					JLabel lblCVIEmail = new JLabel("CVI Email:");
					GridBagConstraints gbc_lblCVIEmail = new GridBagConstraints();
					gbc_lblCVIEmail.anchor = GridBagConstraints.EAST;
					gbc_lblCVIEmail.insets = new Insets(0, 0, 0, 5);
					gbc_lblCVIEmail.gridx = 1;
					gbc_lblCVIEmail.gridy = 10;
					pContent.add(lblCVIEmail, gbc_lblCVIEmail);
				}
				{
					jtfCVIEmail = new JTextField();
					GridBagConstraints gbc_jtfCVIEmail = new GridBagConstraints();
					gbc_jtfCVIEmail.insets = new Insets(0, 0, 5, 0);
					gbc_jtfCVIEmail.fill = GridBagConstraints.HORIZONTAL;
					gbc_jtfCVIEmail.gridx = 2;
					gbc_jtfCVIEmail.gridy = 10;
					pContent.add(jtfCVIEmail, gbc_jtfCVIEmail);
					jtfCVIEmail.setColumns(10);
				}
//				private JTextField jtfCVIErrorEmail;
				{
					JLabel lblCVIErrorEmail = new JLabel("CVI Error Email:");
					GridBagConstraints gbc_lblCVIErrorEmail = new GridBagConstraints();
					gbc_lblCVIErrorEmail.anchor = GridBagConstraints.EAST;
					gbc_lblCVIErrorEmail.insets = new Insets(0, 0, 0, 5);
					gbc_lblCVIErrorEmail.gridx = 1;
					gbc_lblCVIErrorEmail.gridy = 11;
					pContent.add(lblCVIErrorEmail, gbc_lblCVIErrorEmail);
				}
				{
					jtfCVIErrorEmail = new JTextField();
					GridBagConstraints gbc_jtfCVIErrorEmail = new GridBagConstraints();
					gbc_jtfCVIErrorEmail.insets = new Insets(0, 0, 5, 0);
					gbc_jtfCVIErrorEmail.fill = GridBagConstraints.HORIZONTAL;
					gbc_jtfCVIErrorEmail.gridx = 2;
					gbc_jtfCVIErrorEmail.gridy = 11;
					pContent.add(jtfCVIErrorEmail, gbc_jtfCVIErrorEmail);
					jtfCVIErrorEmail.setColumns(10);
				}
//				private JTextField jtfFileType;
				{
					JLabel lblFileType = new JLabel("File Type:");
					GridBagConstraints gbc_lblFileType = new GridBagConstraints();
					gbc_lblFileType.anchor = GridBagConstraints.EAST;
					gbc_lblFileType.insets = new Insets(0, 0, 0, 5);
					gbc_lblFileType.gridx = 1;
					gbc_lblFileType.gridy = 12;
					pContent.add(lblFileType, gbc_lblFileType);
				}
				{
					cbFileType = new JComboBox<String>();
					cbFileType.addItem("PDF");
					cbFileType.addItem("CVI");
					GridBagConstraints gbc_jtfFileType = new GridBagConstraints();
					gbc_jtfFileType.insets = new Insets(0, 0, 5, 0);
					gbc_jtfFileType.fill = GridBagConstraints.HORIZONTAL;
					gbc_jtfFileType.gridx = 2;
					gbc_jtfFileType.gridy = 12;
					pContent.add(cbFileType, gbc_jtfFileType);
				}
				}
				}
		}
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener( new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						doUpdateData();
						doSave();
						setVisible(false);
					}
				});
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener( new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						setVisible(false);
					}
				});
				buttonPane.add(cancelButton);
			}
		}
	}
	
	private void initDB() {
		lu = new StateVetLookup();
		for(String sState : lu.listStates() ) {
			cbSelectedState.addItem(sState);
		}
	}
	
	private void doPrev() {
		int iNew;
		int iNow = cbSelectedState.getSelectedIndex();
		if( iNow > 0 )
			iNew = iNow - 1;
		else
			iNew = cbSelectedState.getItemCount() - 1;
		cbSelectedState.setSelectedIndex(iNew);
	}
	
	private void doNext() {
		int iNew;
		int iNow = cbSelectedState.getSelectedIndex();
		if( iNow < cbSelectedState.getItemCount() - 1 )
			iNew = iNow + 1;
		else
			iNew = 0;
		cbSelectedState.setSelectedIndex(iNew);
	}
	
	private void doUpdateData() {
		lu.setPrefix(jtfPrefix.getText());
		lu.setLastName(jtfLastName.getText());
		lu.setFirstName(jtfFirstName.getText());
		lu.setAddress(jtfAddress.getText());
		lu.setCity(jtfCity.getText());
		lu.setStateCode(jtfStateCode.getText());
		lu.setZipCode(jtfZipCode.getText());
		lu.setEmail(jtfEmail.getText());
		lu.setCVIEmail(jtfCVIEmail.getText());
		lu.setCVIErrorEmail(jtfCVIErrorEmail.getText());
		lu.setFileType((String)cbFileType.getSelectedItem());

	}
	
	private void doFillForm() {
		String sState = (String)cbSelectedState.getSelectedItem();
		lu = new StateVetLookup(sState);
		jtfState.setText(lu.getState());
		jtfPrefix.setText(lu.getPrefix());
		jtfLastName.setText(lu.getLastName());
		jtfFirstName.setText(lu.getFirstName());
		jtfAddress.setText(lu.getAddress());
		jtfCity.setText(lu.getCity());
		jtfStateCode.setText(lu.getStateCode());
		jtfZipCode.setText(lu.getZipCode());
		jtfEmail.setText(lu.getEmail());
		
		if( lu.getCVIEmail() != null && (lu.getEmail() == null || (!lu.getCVIEmail().equalsIgnoreCase(lu.getEmail()))))
			jtfCVIEmail.setText(lu.getCVIEmail());
		else 
			jtfCVIEmail.setText("");
		
		if( lu.getCVIErrorEmail() != null && (lu.getCVIEmail() == null || (!lu.getCVIErrorEmail().equalsIgnoreCase(lu.getCVIEmail()))))
			jtfCVIErrorEmail.setText(lu.getCVIErrorEmail());
		else 
			jtfCVIErrorEmail.setText("");
		
		cbFileType.setSelectedItem(lu.getFileType());
	}
	
	private void doSave() {
		lu.doSave();
	}

}
