package edu.clemson.lph.civet.addons;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.clemson.lph.civet.AnimalIDListTableModel;
import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.prefs.CivetConfig;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EtchedBorder;
import javax.swing.table.TableColumnModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

@SuppressWarnings("serial")
public class NineDashThreeDialog extends JDialog {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
//	static {
//	     logger.setLevel(CivetConfig.getLogLevel());
//	}

	private JTextField jtfCVINo;
	private JTextField jtfDate;
	AnimalIDListTableModel idModel = new AnimalIDListTableModel();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			PropertyConfigurator.configure("CivetConfig.txt");
			CivetConfig.checkAllConfig();
			logger.setLevel(CivetConfig.getLogLevel());
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			NineDashThreeDialog dialog = new NineDashThreeDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public NineDashThreeDialog() {
		setBounds(100, 100, 700, 450);
		setTitle("Civet: NPIP 9-3 Entry Form");
		getContentPane().setLayout(new BorderLayout());
		{
			JPanel pCertPane = new JPanel();
			GridLayout bCertLayout = new GridLayout();
			bCertLayout.setRows(1);
			bCertLayout.setColumns(2);
			bCertLayout.setHgap(5);
			bCertLayout.setVgap(5);
			pCertPane.setLayout(bCertLayout);
			
			JPanel pCertificate = new JPanel();
			pCertificate.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
			getContentPane().add(pCertPane, BorderLayout.NORTH);
			pCertPane.add(pCertificate);
			pCertificate.setPreferredSize(new Dimension(650,150));
			GridBagLayout gbl_pCertificate = new GridBagLayout();
			gbl_pCertificate.columnWidths = new int[] {89, 90, 120, 30, 30};
			gbl_pCertificate.rowHeights = new int[] {10, 0, 20, 22, 22, 0, 0};
			gbl_pCertificate.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
			gbl_pCertificate.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
			pCertificate.setLayout(gbl_pCertificate);
			{
				JLabel label = new JLabel("");
				GridBagConstraints gbc_label = new GridBagConstraints();
				gbc_label.insets = new Insets(0, 0, 5, 5);
				gbc_label.gridx = 5;
				gbc_label.gridy = 1;
				pCertificate.add(label, gbc_label);
			}
			{
				JLabel lblNewLabel = new JLabel("CVI#:");
				lblNewLabel.setHorizontalAlignment(SwingConstants.RIGHT);
				GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
				gbc_lblNewLabel.fill = GridBagConstraints.HORIZONTAL;
				gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
				gbc_lblNewLabel.gridx = 0;
				gbc_lblNewLabel.gridy = 2;
				pCertificate.add(lblNewLabel, gbc_lblNewLabel);
			}
			{
				jtfCVINo = new JTextField();
				GridBagConstraints gbc_jtfCVINo = new GridBagConstraints();
				gbc_jtfCVINo.gridwidth = 3;
				gbc_jtfCVINo.anchor = GridBagConstraints.NORTH;
				gbc_jtfCVINo.fill = GridBagConstraints.HORIZONTAL;
				gbc_jtfCVINo.insets = new Insets(0, 0, 5, 5);
				gbc_jtfCVINo.gridx = 1;
				gbc_jtfCVINo.gridy = 2;
				pCertificate.add(jtfCVINo, gbc_jtfCVINo);
				jtfCVINo.setColumns(10);
			}
			{
				JLabel lblNewLabel_4 = new JLabel("New label");
				GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
				gbc_lblNewLabel_4.insets = new Insets(0, 0, 5, 5);
				gbc_lblNewLabel_4.gridx = 3;
				gbc_lblNewLabel_4.gridy = 2;
				pCertificate.add(lblNewLabel_4, gbc_lblNewLabel_4);
			}
			{
				JLabel lblNewLabel_1 = new JLabel("Date:");
				lblNewLabel_1.setHorizontalAlignment(SwingConstants.RIGHT);
				GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
				gbc_lblNewLabel_1.fill = GridBagConstraints.HORIZONTAL;
				gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
				gbc_lblNewLabel_1.gridx = 0;
				gbc_lblNewLabel_1.gridy = 3;
				pCertificate.add(lblNewLabel_1, gbc_lblNewLabel_1);
			}
			{
				jtfDate = new JTextField();
				GridBagConstraints gbc_jtfDate = new GridBagConstraints();
				gbc_jtfDate.fill = GridBagConstraints.HORIZONTAL;
				gbc_jtfDate.anchor = GridBagConstraints.NORTHWEST;
				gbc_jtfDate.insets = new Insets(0, 0, 5, 5);
				gbc_jtfDate.gridx = 1;
				gbc_jtfDate.gridy = 3;
				pCertificate.add(jtfDate, gbc_jtfDate);
				jtfDate.setColumns(10);
			}
			{
				JLabel lblNewLabel_2 = new JLabel("Species:");
				lblNewLabel_2.setHorizontalAlignment(SwingConstants.RIGHT);
				GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
				gbc_lblNewLabel_2.fill = GridBagConstraints.HORIZONTAL;
				gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
				gbc_lblNewLabel_2.gridx = 0;
				gbc_lblNewLabel_2.gridy = 4;
				pCertificate.add(lblNewLabel_2, gbc_lblNewLabel_2);
			}
			{
				JComboBox<String> cbSpecies = new JComboBox<String>();
				GridBagConstraints gbc_cbSpecies = new GridBagConstraints();
				gbc_cbSpecies.gridwidth = 3;
				gbc_cbSpecies.anchor = GridBagConstraints.NORTH;
				gbc_cbSpecies.fill = GridBagConstraints.HORIZONTAL;
				gbc_cbSpecies.insets = new Insets(0, 0, 5, 5);
				gbc_cbSpecies.gridx = 1;
				gbc_cbSpecies.gridy = 4;
				pCertificate.add(cbSpecies, gbc_cbSpecies);
			}
			{
				JLabel lblProduct = new JLabel("Product:");
				lblProduct.setHorizontalAlignment(SwingConstants.RIGHT);
				GridBagConstraints gbc_lblProduct = new GridBagConstraints();
				gbc_lblProduct.fill = GridBagConstraints.HORIZONTAL;
				gbc_lblProduct.insets = new Insets(0, 0, 0, 5);
				gbc_lblProduct.gridx = 0;
				gbc_lblProduct.gridy = 5;
				pCertificate.add(lblProduct, gbc_lblProduct);
			}
			{
				JComboBox<String> cbProduct = new JComboBox<String>();
				GridBagConstraints gbc_cbProduct = new GridBagConstraints();
				gbc_cbProduct.insets = new Insets(0, 0, 0, 5);
				gbc_cbProduct.gridwidth = 3;
				gbc_cbProduct.anchor = GridBagConstraints.NORTH;
				gbc_cbProduct.fill = GridBagConstraints.HORIZONTAL;
				gbc_cbProduct.gridx = 1;
				gbc_cbProduct.gridy = 5;
				pCertificate.add(cbProduct, gbc_cbProduct);
			}
			JPanel pIDs = new JPanel();
			{
				JLabel lblNewLabel_3 = new JLabel("IDs:");
				lblNewLabel_3.setBounds(80, 39, 20, 14);
				lblNewLabel_3.setHorizontalAlignment(SwingConstants.RIGHT);
				pIDs.add(lblNewLabel_3);
			}
			{
				JButton btnNewButton = new JButton("Add IDs");
				btnNewButton.setBounds(27, 76, 73, 23);
				pIDs.add(btnNewButton);
			}
			{
				pIDs.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
				pIDs.setLayout(null);
				pCertPane.add(pIDs);
				{
					idModel.addRow("CHI", "Chicken", "scnpip12345");
					idModel.addRow("CHI", "Chicken", "scnpip12346");
					idModel.addRow("CHI", "Chicken", "scnpip12347");
					idModel.addRow("CHI", "Chicken", "scnpip12348");
				}
			}
			JScrollPane spIDs = new JScrollPane();
			spIDs.setBounds(110, 11, 223, 128);
			pIDs.add(spIDs);
			JTable tblIDs = new JTable();
			tblIDs.setModel(idModel);
			tblIDs.setPreferredSize(new Dimension(200,120));
			TableColumnModel tcm = tblIDs.getColumnModel();
			tcm.removeColumn(tcm.getColumn(0));
			tcm.removeColumn(tcm.getColumn(0));
			spIDs.setViewportView(tblIDs);
			{
				JPanel pParticipants = new JPanel();
				GridLayout gl_pParticipants = new GridLayout();
				gl_pParticipants.setHgap(5);
				gl_pParticipants.setVgap(5);
				gl_pParticipants.setColumns(2);
				gl_pParticipants.setRows(0);
				pParticipants.setLayout(gl_pParticipants);
				getContentPane().add(pParticipants, BorderLayout.CENTER);
				{
					ParticipantPanel pConsignor = new ParticipantPanel();
					pConsignor.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
					pParticipants.add(pConsignor);
				}
				{
					ParticipantPanel pConsignee = new ParticipantPanel();
					pConsignee.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
					pParticipants.add(pConsignee);
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
				}
				{
					JButton cancelButton = new JButton("Cancel");
					cancelButton.setActionCommand("Cancel");
					buttonPane.add(cancelButton);
				}
			}
		}
	}

}
