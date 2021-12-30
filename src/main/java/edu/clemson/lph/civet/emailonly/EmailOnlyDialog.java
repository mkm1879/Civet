package edu.clemson.lph.civet.emailonly;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
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

import edu.clemson.lph.logging.Logger;
import org.jpedal.PdfDecoder;
import org.jpedal.objects.PdfPageData;

import edu.clemson.lph.civet.CivetInbox;
import edu.clemson.lph.civet.lookup.States;
import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.controls.DBComboBox;
import edu.clemson.lph.dialogs.ProgressDialog;
import edu.clemson.lph.utils.FileUtils;


@SuppressWarnings("serial")
public class EmailOnlyDialog extends JDialog {
      private static Logger logger = Logger.getLogger();

	private final JPanel contentPanel = new JPanel();
		@SuppressWarnings("unused")
		private CivetInbox parent;
		private String viewerTitle = "Civet: Email Only: ";
		private PdfDecoder pdfDecoder;
		private EmailOnlyFileController controller;
		private float fScale = 1.0f;
		private int iRotation;
		JScrollPane display;
		JPanel altDisplay;
		JPanel pView;
		DBComboBox cbState;
		private JButton bSaveNext;

	/**
	 * Create the dialog.
	 */
	public EmailOnlyDialog(CivetInbox parent) {
		this.parent = parent;
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		controller = new EmailOnlyFileController( this ); 
		
		initializeDisplay();
	}
	
	private void initializeDisplay() {
		setBounds(100, 100, 800, 600);
		make80Percent();
		setTitle("Civet: Email Only ");
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(new FlowLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			
			iRotation = CivetConfig.getRotation();
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
			
			JButton btnZoomOut = new JButton("-");
			btnZoomOut.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					fScale = fScale / 1.1f;
					pdfDecoder.setPageParameters(fScale,controller.getCurrentPageNo(),iRotation);
					pdfDecoder.invalidate();
					display.setViewportView(pdfDecoder);
					repaint();
				}
			});
			buttonPane.add(btnZoomOut);
			
			JButton btnZoomIn = new JButton("+");
			btnZoomIn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					fScale = fScale * 1.1f;
					pdfDecoder.setPageParameters(fScale,controller.getCurrentPageNo(),iRotation);
					pdfDecoder.invalidate();
					display.setViewportView(pdfDecoder);
					repaint();
				}
			});
			buttonPane.add(btnZoomIn);
			
			buttonPane.add(bRotate);
			
			cbState = new DBComboBox();
			cbState.setModel( new States() );
			cbState.setBlankDefault(true);
			cbState.addFocusListener( new FocusListener() {
				@Override
				public void focusGained(FocusEvent arg0) {
				}

				@Override
				public void focusLost(FocusEvent arg0) {
					bSaveNext.requestFocus();
				}
				
			});
			cbState.refresh();
			
			buttonPane.add(cbState);
			
			bSaveNext = new JButton("Save-Next");
			bSaveNext.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					doSaveNext();
				}
			});
			buttonPane.add(bSaveNext);
			setupViewPanel();
		}
		cbState.requestFocus();
	}
	
	private void doSaveNext() {
		String sFromState = CivetConfig.getHomeStateAbbr();
		String sState = cbState.getSelectedCode();
		if( sState != null && sState.trim().length() > 0 ) {
			byte[] fileBytes = controller.extractPagesToNewPDF();
			String sFileName = sFromState + "_to_" + sState + "_" + FileUtils.getRoot(controller.getCurrentFileName());
			int iPage = controller.getCurrentPageNo();
			if( iPage > 1 )
				sFileName += "(" + iPage + ")";
			sFileName += ".pdf";
			EmailOnlySaveFileThread saveThread = new EmailOnlySaveFileThread( this, fileBytes, sFileName );
			saveThread.start();
			if( !controller.pageForward() ) {
				setVisible(false);
				doSend();
			}
		}
		cbState.requestFocus();
	}
	
	private void doSend() {
		if( !CivetConfig.initEmail(true) )
			return;
		ProgressDialog prog = new ProgressDialog(this, "Civet", "Emailing Email Only CVIs");
		prog.setAuto(true);
		prog.setVisible(true);
		EmailOnlySendFilesThread tThread = new EmailOnlySendFilesThread(this, prog);
		tThread.start();
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
		File fDir = new File( CivetConfig.getEmailOnlyPath() );
		JFileChooser open = new JFileChooser( fDir );
		open.setDialogTitle("Civet: Open multiple PDF and Image Files");
		open.setFileSelectionMode(JFileChooser.FILES_ONLY);
		open.setFileFilter(new FileNameExtensionFilter(
		        "Image, PDF, and Civet Files", "jpg", "png", "pdf", "jpeg", "gif", "bmp"));
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
	
	void setupForm( String sFileName ) {
		setTitle(getViewerTitle() + sFileName);
	}
}
