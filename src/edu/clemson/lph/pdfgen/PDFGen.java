package edu.clemson.lph.pdfgen;
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
import java.io.*;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import edu.clemson.lph.civet.Civet;

public class PDFGen implements CodeSource{
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	private static final Font.FontFamily fTimes = Font.FontFamily.TIMES_ROMAN;
	private static final Font fBold = new Font(fTimes, 12, Font.BOLD);
	private static final Font fNormal = new Font(fTimes, 12, Font.NORMAL);
	private static final Font fUnderline = new Font(fTimes,12,Font.UNDERLINE);
	private static final Font fItalic = new Font(fTimes,12,Font.ITALIC);
	private static final Font fSmallItalic = new Font(fTimes,8,Font.ITALIC);

    private CodeSource codeSource = null;
    private String sSourceFile = null;
    private OutputStream osDest = null;

    public void setCodeSource( CodeSource codeSource ) {
    	this.codeSource = codeSource;
    }

    public void setSourceFile( String sSourceFile ) { this.sSourceFile = sSourceFile; }
    public void setDestStream( OutputStream osDest ) { this.osDest = osDest; }

	/**
	 * main is here for testing.
	 * @param args
	 */
	public static void main(String[] args) {
		PDFGen inst = new PDFGen();
		inst.setCodeSource(inst);
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			FileOutputStream fos = new FileOutputStream("PDFGen.pdf");
//			inst.printDoc("OutboundLetterTemplate.txt", bos);
			inst.printDoc("ErrorLetterTemplate.txt", bos);
			fos.write(bos.toByteArray());  // This byte array is first to merge
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void printDoc( String sSourceFile, OutputStream osDest  ) {
		this.sSourceFile = sSourceFile;
		this.osDest = osDest;
		printDoc();
	}

	private void printDoc() {
		if( sSourceFile == null || osDest == null ) {
			logger.error( "Cannot print nothing" );
			return;
		}
		boolean bBold = false;
		boolean bCenter = false;
		boolean bItalic = false;
		boolean bSmallItalic = false;
		boolean bUnderline = false;
		try {
			 Document doc = new Document ();
			 float fCorr = doc.getPageSize().getWidth()/8.5f;
			 doc.setMargins(1.0f*fCorr,1.0f*fCorr,1.0f*fCorr,1.0f*fCorr);

			 PdfWriter.getInstance (doc, osDest);
			 doc.open ();
			 BufferedReader br = new BufferedReader( new FileReader( sSourceFile ) );
			 String sLine = br.readLine();
			 while( sLine != null ) {
				bBold = false;
				bCenter = false;
				 if( sLine.startsWith(".") ) {
					 String sRest = sLine.substring(1);
					 String sCodes = sRest.substring(0,sRest.indexOf('.'));
					 sLine = sRest.substring(sRest.indexOf('.')+1);
					 if( "image".equals(sCodes) ) {
							String sFileName = sLine;
							com.itextpdf.text.Image image =
									com.itextpdf.text.Image.getInstance(sFileName);
							image.setAlignment (Element.ALIGN_CENTER);
							doc.add(image);
							sLine = br.readLine();
							continue;
					 }
					 else if( "himage".equals(sCodes) ) {
							String sFileName = sLine;
							com.itextpdf.text.Image image =
									com.itextpdf.text.Image.getInstance(sFileName);
							image.scaleToFit(500, 40);
							image.setAlignment (Element.ALIGN_CENTER);
							doc.add(image);
							Paragraph p = new Paragraph(" ");
							doc.add(p);
							sLine = br.readLine();
							continue;
					 }
					 else if( "fimage".equals(sCodes) ) {
						 int iBlanks = 9; // How do I figure out how many to get to end?
						 for( int i = 0; i < iBlanks; i++ ) {
							Paragraph p = new Paragraph(" ");
							doc.add(p);
						 }
							String sFileName = sLine;
							com.itextpdf.text.Image image =
									com.itextpdf.text.Image.getInstance(sFileName);
							image.scaleToFit(500,40);
							image.setAlignment (Element.ALIGN_CENTER);
							doc.add(image);
							sLine = br.readLine();
							continue;
					 }
					 else if( "list".equals(sCodes) ) {
						 String sFullLine = doSub(sLine);
						 StringTokenizer tok = new StringTokenizer( sFullLine, "\n" );
						 List list = new List(List.UNORDERED);
						 while( tok.hasMoreTokens() ) {
							 String sNextLine = tok.nextToken();
							 ListItem listItem = new ListItem(sNextLine, fNormal);
							 list.add(listItem);
						 }
						 doc.add(list);
						 sLine = br.readLine();
						 continue;
					 }
					 if( sCodes.contains("b") ) bBold = true;
					 if( sCodes.contains("c") ) bCenter = true;
					 if( sCodes.contains("i") ) bItalic = true;
					 if( sCodes.contains("si") ) bSmallItalic = true;
					 if( sCodes.contains("u") ) bUnderline = true;
				 }
				 if( sLine.trim().length() == 0 ) sLine = " ";
				 
				 String sFullLine = doSub(sLine);
				 Paragraph p = new Paragraph ();
				 if( bBold ) p.setFont(fBold);
				 else if (bSmallItalic) p.setFont(fSmallItalic);
				 else if (bItalic) p.setFont(fItalic);
				 else if (bUnderline) p.setFont(fUnderline);
				 else p.setFont(fNormal);
				 if( bCenter ) {
					 p.setAlignment (Element.ALIGN_CENTER);
				 }
				 else {
					 p.setAlignment (Element.ALIGN_LEFT);
				 }
				 p.add(sFullLine);
				 doc.add (p);
				 sLine = br.readLine();
			 }
			 br.close();
			 doc.close ();
		} catch (FileNotFoundException e) {
			logger.error("Could not find source file " + sSourceFile + " or destination", e);
		} catch ( IOException e) {
			logger.error("Could not read file " + sSourceFile, e);
		} catch (DocumentException e) {
			logger.error("Error creating iText Document", e);
		}
	}

	private String doSub( String sLine ) {
		if( sLine == null ) return "";
		StringBuilder sbOut = new StringBuilder();
		StringBuffer sb = new StringBuffer();
		for( int i = 0; i < sLine.length(); i++ ) {
			char cNext = sLine.charAt(i);
			if( cNext == '{' && i < sLine.length() - 2 && sLine.charAt(i+1)=='{' && sLine.charAt(i+2)=='{' ) {
				// Write what we have to here.
				sbOut.append(sb.toString());
				sb.setLength(0);
				i+=3;
				StringBuffer sbCode = new StringBuffer();
				cNext = sLine.charAt(i);
				while( cNext != '}' ) {
					sbCode.append(cNext);
					cNext = sLine.charAt(++i);
				}
				String sPhrase = codeSource.lookupCode(sbCode.toString());
				if( sPhrase == null ) {
					logger.error( "No code " + sbCode );
					return "";
				}
				else {
					sbOut.append(sPhrase);
				}
				if( i < sLine.length() - 2 && sLine.charAt(i+1)=='}' && sLine.charAt(i+2)=='}' ) {
					i+=2;
				}
			}
			else {
				if( cNext == '\t' ) sb.append("   ");
				else sb.append(cNext);
			}
		}
		// Write the rest
		sbOut.append(sb.toString());
		return sbOut.toString();
	}

	public String lookupCode( String sCode ) {
		return "Your code = " + sCode;
	}

}
