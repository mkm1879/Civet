package edu.clemson.lph.civet;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.clemson.lph.civet.lookup.SpeciesLookup;
import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.controls.DBComboBox;

import java.awt.GridLayout;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JComboBox;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

@SuppressWarnings("serial")
public class ChangeSpeciesDialog extends JDialog {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());

	private final JPanel contentPanel = new JPanel();
	private CivetEditDialog dParent;
	private DBComboBox cbToSpecies;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			PropertyConfigurator.configure("CivetConfig.txt");
			ChangeSpeciesDialog dialog = new ChangeSpeciesDialog(null);
			CivetConfig.checkAllConfig();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public ChangeSpeciesDialog(CivetEditDialog dParent) {
		this.dParent = dParent;
		setupGUI();
		setupDB();
		
	}
	
	private void setupGUI() {
		setTitle("Civet: Change Species");
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new GridLayout(1, 2, 0, 2));
		{
			JPanel fromPanel = new JPanel();
			contentPanel.add(fromPanel);
			GridBagLayout gbl_fromPanel = new GridBagLayout();
			gbl_fromPanel.columnWidths = new int[]{0, 0, 0};
			gbl_fromPanel.rowHeights = new int[]{0, 0, 0};
			gbl_fromPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			gbl_fromPanel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
			fromPanel.setLayout(gbl_fromPanel);
			{
				JLabel lblFromSpecies = new JLabel("From Species");
				GridBagConstraints gbc_lblFromSpecies = new GridBagConstraints();
				gbc_lblFromSpecies.insets = new Insets(0, 0, 5, 0);
				gbc_lblFromSpecies.gridx = 1;
				gbc_lblFromSpecies.gridy = 0;
				fromPanel.add(lblFromSpecies, gbc_lblFromSpecies);
			}
			{
				JComboBox<String> cbFromSpecies = new JComboBox<String>();
				GridBagConstraints gbc_cbFromSpecies = new GridBagConstraints();
				gbc_cbFromSpecies.fill = GridBagConstraints.HORIZONTAL;
				gbc_cbFromSpecies.gridx = 1;
				gbc_cbFromSpecies.gridy = 1;
				fromPanel.add(cbFromSpecies, gbc_cbFromSpecies);
			}
		}
		{
			JPanel toPanel = new JPanel();
			contentPanel.add(toPanel);
			GridBagLayout gbl_toPanel = new GridBagLayout();
			gbl_toPanel.columnWidths = new int[]{0, 0, 0};
			gbl_toPanel.rowHeights = new int[]{0, 0, 0};
			gbl_toPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			gbl_toPanel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
			toPanel.setLayout(gbl_toPanel);
			{
				JLabel lblToSpecies = new JLabel("To Species");
				GridBagConstraints gbc_lblToSpecies = new GridBagConstraints();
				gbc_lblToSpecies.insets = new Insets(0, 0, 5, 0);
				gbc_lblToSpecies.gridx = 1;
				gbc_lblToSpecies.gridy = 0;
				toPanel.add(lblToSpecies, gbc_lblToSpecies);
			}
			{
				cbToSpecies = new DBComboBox();
				GridBagConstraints gbc_cbToSpecies = new GridBagConstraints();
				gbc_cbToSpecies.fill = GridBagConstraints.HORIZONTAL;
				gbc_cbToSpecies.gridx = 1;
				gbc_cbToSpecies.gridy = 1;
				toPanel.add(cbToSpecies, gbc_cbToSpecies);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
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
	
	private void setupDB() {
		cbToSpecies.setModel( new SpeciesLookup() );
		cbToSpecies.setBlankDefault(true);
		cbToSpecies.refresh();

	}

}
