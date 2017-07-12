package edu.clemson.lph.civet;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jpedal.PdfDecoder;
import org.jpedal.objects.PdfPageData;

import edu.clemson.lph.civet.lookup.States;
import edu.clemson.lph.controls.DBComboBox;
import edu.clemson.lph.utils.FileUtils;


@SuppressWarnings("serial")
public class EmailOnlyDialog extends JDialog {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
	static {
		// BasicConfigurator replaced with PropertyConfigurator.
	     PropertyConfigurator.configure("CivetConfig.txt");
	     logger.setLevel(Level.INFO);
	}

	private final JPanel contentPanel = new JPanel();
		@SuppressWarnings("unused")
		private Window parent;
		private String viewerTitle = "Civet: Email Only: ";
		private PdfDecoder pdfDecoder;
		private EmailOnlyFileController controller;
		private int iFile;
		private int iFiles;
		private float fScale;
		private int iRotation;
		private static int iCount = 1;
		JScrollPane display;
		JPanel altDisplay;
		JPanel pView;
		DBComboBox cbState;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			EmailOnlyDialog dialog = new EmailOnlyDialog((Window)null);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.selectFiles();
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public EmailOnlyDialog(Window parent) {
		this.parent = parent;
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		controller = new EmailOnlyFileController( this ); 
		
		initializeDisplay();
//		initializeDBComponents();
	}
	
	private void initializeDisplay() {
		setBounds(100, 100, 800, 600);
		make80Percent();
		setTitle(getViewerTitle());
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(new FlowLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			
			JButton bRotate = new JButton("Rotate");
			bRotate.setIcon( new ImageIcon(getClass().getResource("/edu/clemson/lph/civet/res/rotate.gif")));
			bRotate.setToolTipText("Rotate Viewer");
			bRotate.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					iRotation = (iRotation + 90) % 360;
					pdfDecoder.setPageParameters(fScale,controller.getCurrentPageNo(),iRotation);
					pdfDecoder.invalidate();
					display.setViewportView(pdfDecoder);
					repaint();
				}
			});
			buttonPane.add(bRotate);
			
			JButton btnPrev = new JButton("Prev");
			buttonPane.add(btnPrev);
			
			cbState = new DBComboBox();
			cbState.setModel( new States() );
			cbState.setBlankDefault(true);
			cbState.refresh();

			buttonPane.add(cbState);
			
			JButton bSaveNext = new JButton("Save-Next");
			bSaveNext.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					doSaveNext();
				}
			});
			buttonPane.add(bSaveNext);
			setupViewPanel();
		}
	}
	
	private void doSaveNext() {
		byte[] fileBytes = controller.getCurrentPdfBytes();
		String sFileName = cbState.getSelectedCode() + "_" + controller.getCurrentFileName();
		EmailOnlySaveFileThread saveThread = new EmailOnlySaveFileThread( this, fileBytes, sFileName );
		saveThread.start();
	}
	
	public void saveComplete() {
		if( !controller.pageForward() )
			setVisible(false);

	}
	
	private void setupViewPanel() {	
		pdfDecoder = new PdfDecoder();
		//ensure non-embedded font map to sensible replacements
		//    PdfDecoder.setFontReplacements(pdfDecoder);
		pView = new JPanel();
		pView.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		getContentPane().add(pView, BorderLayout.CENTER);
		pView.setLayout(new BorderLayout(0, 0));
		display = new JScrollPane();
		display.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		display.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		// This is the magic.  We simply put the pdfDecoder in the viewport of the scroll pane.
		display.setViewportView(pdfDecoder);
		pView.add(display, BorderLayout.CENTER);

		altDisplay = new JPanel();
		altDisplay.setLayout(new BorderLayout(0, 0));
		altDisplay.setBorder(new EmptyBorder(20,20,20,20));
		JLabel lNoPDF = new JLabel("No PDF Attached");

		lNoPDF.setFont(new Font("Tahoma", Font.BOLD, 16));
		altDisplay.add(lNoPDF, BorderLayout.CENTER);
		altDisplay.setVisible(false);
	}
	
	private void make80Percent() {
	    // Center the window (will take effect when normalized)
	    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	    int height = (int)(screenSize.height * 0.80);
	    int width = (int)(screenSize.width * 0.80);
	    this.setSize(width,height);
	    this.setLocation((screenSize.width - width) / 2, (screenSize.height - height) / 2);
	}
	
	
	public PdfDecoder getPdfDecoder() { return pdfDecoder; }
	/**
	 * Open previously selected files.
	 * @param selectedFiles
	 */
	public void openFiles(File selectedFiles[] ) {
		controller.setCurrentFiles(selectedFiles);
	}

	/**
	 * opens a chooser and allows user to select One or more pdf or jpg files and opens a pdf created from them.
	 */
	public void selectFiles() {
		File fDir = new File( "."  ); //CivetConfig.getInputDirPath() );
		JFileChooser open = new JFileChooser( fDir );
		open.setDialogTitle("Civet: Open multiple PDF and Image Files");
		open.setFileSelectionMode(JFileChooser.FILES_ONLY);
		open.setFileFilter(new FileNameExtensionFilter(
		        "Image, PDF, and Civet Files", "jpg", "png", "pdf", "jpeg", "gif", "bmp", "cvi"));
		open.setMultiSelectionEnabled(true);

		int resultOfFileSelect = JFileChooser.ERROR_OPTION;
		while(resultOfFileSelect==JFileChooser.ERROR_OPTION){

			resultOfFileSelect = open.showOpenDialog(this);

			if(resultOfFileSelect==JFileChooser.ERROR_OPTION) {
				logger.error("JFileChooser error");
			}

			if(resultOfFileSelect==JFileChooser.APPROVE_OPTION){
				File selectedFiles[] = open.getSelectedFiles();
				openFiles(selectedFiles);
			}
		}
	}
	
	// Callbacks for Threads to set values in counter panel.
	void setPage( int iPageNo ) {
//		pCounters.setPage(iPageNo);
	}

	void setPages( int iPages ) {
//		pCounters.setPages(iPages);
	}
	
	void setFile( int iFileNo ) {
//		pCounters.setFile(iFileNo); // currentFiles is 0 indexed array
	}

	void setFiles( int iFiles ) {
//		pCounters.setFiles(iFiles);
	}
	public void updatePdfDisplay() {
		pdfDecoder.setPageParameters(getScale(),
				controller.getCurrentPageNo(),
				getRotation()); //values scaling (1=100%). page number, rotation + 180
		pdfDecoder.waitForDecodingToFinish();
		pdfDecoder.invalidate();
		pdfDecoder.updateUI();
		pdfDecoder.validate();
	}
	public EmailOnlyFileController getController() { return controller; }
	public float getScale() { return fScale; }
	public int getRotation() { 
		return iRotation; 
	}
	// Actual value has to be 180 off of that actually displayed in Acrobat, etc.
	public void setRotation( int iRotation ) { 
		PdfPageData pd = pdfDecoder.getPdfPageData();
		int iThisPage = controller.getCurrentPageNo();
		int iPageRotation = pd.getRotation(iThisPage);
		this.iRotation = ( iPageRotation + iRotation ) % 360; 
	}
	public String getViewerTitle() { return viewerTitle; }
	
	void setupForm( String sFileName, int iPageNo, int iPagesInFile, int iFileNo, int iFiles, boolean bPageComplete ) {
		setTitle(getViewerTitle() + sFileName);
		setPage(iPageNo);
		setPages(iPagesInFile);
		setFile(iFileNo);
		setFiles(iFiles);
	}
}
