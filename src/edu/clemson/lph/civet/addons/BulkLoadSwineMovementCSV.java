package edu.clemson.lph.civet.addons;
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
import edu.clemson.lph.civet.AddOn;
import edu.clemson.lph.civet.CSVDataFile;
import edu.clemson.lph.civet.CSVFilter;
import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.CivetConfig;
import edu.clemson.lph.db.*;
import edu.clemson.lph.dialogs.*;
import edu.clemson.lph.utils.PremCheckSum;

import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.*;

import org.apache.log4j.*;

public class BulkLoadSwineMovementCSV implements ThreadListener, AddOn {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
	JFrame fParent = null;
	private DatabaseConnectionFactory factory;
	private static final boolean bRequireBothPINs = true;
	
	public BulkLoadSwineMovementCSV() {
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SCDatabaseConnectionFactory factory = new SCDatabaseConnectionFactory();
		ThreeLineQuestionDialog dlg = new ThreeLineQuestionDialog( "DB Login", "DB Name", "UserName", "Password", true);
		dlg.setPassword(true);
		dlg.setVisible(true);
		if( dlg.isExitOK() ) {
		String DBName = dlg.getAnswerOne();
		String Username = dlg.getAnswerTwo();
		String Password = dlg.getAnswerThree();
		factory.setServerName(DBName);
		factory.setUserName( Username );
		factory.setPassword( Password );
		BulkLoadSwineMovementCSV me = new BulkLoadSwineMovementCSV();
		me.doImportCSV(null); 
		}
	}
	
	public void doImportCSV(Window parent) {
		if( factory == null )
			factory = InitAddOns.getFactory();
		if( parent instanceof JFrame ) 
			fParent = (JFrame)parent;
		String sFilePath = null;
	    JFileChooser fc = new JFileChooser();
	    fc.setCurrentDirectory(new File(CivetConfig.getBulkLoadDirPath()));
	    fc.setDialogTitle("Open Swine Movement Permit CSV File");
	    fc.setFileFilter( new CSVFilter() );
	    int returnVal = fc.showOpenDialog(fParent);
	    if (returnVal == JFileChooser.APPROVE_OPTION) {
	      File file = fc.getSelectedFile();
	      sFilePath = file.getAbsolutePath();
	    }
	    else {
	    	return;
	    }
		importSwineCSVFile( sFilePath, fParent );

	}
	
	private void importSwineCSVFile( String sFilePath, JFrame fParent ) {
		ProgressDialog prog = new ProgressDialog(fParent, "Civet", "Loading CVI");
		prog.setAuto(true);
		prog.setVisible(true);
		TWorkCSV tWork = new TWorkCSV( prog, sFilePath, fParent );
		tWork.start();
	}

	// Also create TWorkAddSpecies and TWorkAddPage
	class TWorkCSV extends Thread {
		String sFilePath;
		ProgressDialog prog;
		JFrame fParent;
		public TWorkCSV( ProgressDialog prog, String sFilePath, JFrame fParent ) {
			this.prog = prog;
			this.sFilePath = sFilePath;
			this.fParent = fParent;
		}
		public void run() {
			// Create CSVDataFile object from CSV file
			CSVDataFile data;
			try {
				data = new CSVDataFile( sFilePath );
				String sUserID = factory.getUserName();
				String sQuery = null;
				// So we only notify once per vet
				ArrayList<String> lMissingVets = new ArrayList<String>();
				Connection newConn = factory.makeDBConnection();
				if( newConn == null ) {
					logger.error("Null newConn in openDBRecord");
					MessageDialog.messageLater((Window)null, "Civet: Database Error", "Could not connect to database");
					return;
				}
				try {
					// Iterate over the CSV file
					while( data.nextRow() ) {
						String sImport = data.isInbound()?"Y":"N";
						String sDate = (new SimpleDateFormat( "MM/dd/yyyy")).format( data.getDate() );
						java.util.Date dDateIssued = new java.util.Date( data.getDate().getTime() );
						java.util.Date dDateReceived = new java.util.Date( new java.util.Date().getTime() );
						String sVet = data.getVet();
						boolean bAccredited = false;
						int iIssuedBy = getVetKey( sVet );
						if( iIssuedBy > 0 ) bAccredited = true;
						iIssuedBy = getVetKey( sVet, false );
						if( iIssuedBy <= 0 && !lMissingVets.contains(sVet) ) {
							MessageDialog.messageLater(fParent, "Veterinarian Not Found", 
									"Cannot find veterinarian: " + sVet);
							lMissingVets.add(sVet);
						}
						if( !bAccredited && !lMissingVets.contains(sVet) ) {
							MessageDialog.messageLater(fParent, "Accredited Veterinarian Not Found", 
									"Cannot find accreditation record for veterinarian: " + sVet);
							lMissingVets.add(sVet);
						}
						String sSpecies = "POR";
						int iNumber = data.getNumber();
						String sCompany = data.getCompany();
						String sCoPrefix = null;
						// This is horribly over-fit to our data but adding a lookup table
						// and accompanying logic is over-kill for now
						if( "Murphy Brown".equalsIgnoreCase(sCompany) ) sCoPrefix = "MB_";
						if( "MurphyBrown".equalsIgnoreCase(sCompany) ) sCoPrefix = "MB_";
						if( "Prestage".equalsIgnoreCase(sCompany) ) sCoPrefix = "PR_";
						
						boolean bValid = true;
						String sSourcePIN = data.getSourcePin();
						try {
							if( !PremCheckSum.isValid(sSourcePIN) ) {
								sSourcePIN = null;
							}
						} catch (Exception e1) {
							sSourcePIN = null;
						}
						String sDestinationPIN = data.getDestPin();
						try {
							if( !PremCheckSum.isValid(sDestinationPIN) ) {
								sDestinationPIN = null;
							}
						} catch (Exception e1) {
							sDestinationPIN = null;
						}
						if( !bValid && bRequireBothPINs ) {
							MessageDialog.messageLater(fParent, "Civet: Missing PIN",
									"Both source and destination PINs are required for bulk shipment records.\n" +
											"Source PIN = " + sSourcePIN + "\n" +
											"Dest PIN = " + sDestinationPIN + "\n" +
											"Ship Date = " + sDate );
							continue;
						}
						// Based on company, read values for each of the required columns (Arrays of ints?)
						// Validate source and destination prem ids
						// Lookup source and destingation prem ids to confirm direction of move
						// Lookup vet based on vet name string in CSV file (tricky)
						// Build and run query
						// Inbound, CertificateNbr, OtherState, OtherPIN, ThisPIN, IssuedDate, SpeciesCode, Number
						// example exec CivetConfig.getSchemaName().getCVIDuplicate 'Y', 'MB_13091', 'NC', '00DR0S4', '00D1DVC', '05/02/2011', 'POR',350
						// Returns any found CVIKey as one row result set
						String sDupeCheckQuery = 
								"EXEC " + CivetConfig.getDbDatabaseName()+"."+CivetConfig.getDbCivetSchemaName()+".getCVIDuplicate ?, ?, ?, ?, ?, ?, ?, ?, ?";
						sQuery = sDupeCheckQuery;
						PreparedStatement ps = newConn.prepareStatement(sQuery);
						ps.setString(1, sImport);
						ps.setString(2, sCoPrefix);
						// This is UGLY.  One SP uses the code and the other the key
						ps.setInt(3, getStateKey(data.getSourceState()));
						ps.setString(4, data.getSourcePin());
						ps.setInt(5, getStateKey(data.getDestState()));
						ps.setString(6, data.getDestPin());
						ps.setDate(7, new java.sql.Date( dDateIssued.getTime() ));
						ps.setString(8, sSpecies);
						ps.setInt(9, iNumber);
	// Debugging output
	logger.info(sImport +","+ sCoPrefix +","+ data.getSourceState() +","+ 
		data.getSourcePin() +","+ data.getDestState() +","+ data.getDestPin() +","+ 
		dDateIssued +","+ sSpecies +","+ iNumber );

						ResultSet rs = ps.executeQuery();
						if( rs.next() ) {
							MessageDialog.messageLater(fParent, "Civet: Duplicate Insert",
									"A bulk shipment appears to be a duplicate.  Skipping.\n" +
									"Source PIN = " + data.getSourcePin() + "\n" +
									"Dest PIN = " + data.getDestPin() + "\n" +
									"Number = " + iNumber + "\n" +
									"Ship Date = " + sDate );
							continue;
						}
						String sInsCVIQuery = 
								"{ CALL " +CivetConfig.getDbDatabaseName()+"."+CivetConfig.getDbCivetSchemaName()+".InsCVI( " +
								"@import = ?, " +
								"@cviNumber = ?, " +
								"@originState = ?, " +
								"@originPIN = ?, " +
								"@originFarm = ?, " +
								"@destinationState = ?, " +
								"@destinationPIN = ?, " +
								"@destinationFarm = ?, " +
								"@issuedDate = ?, " +
								"@receivedDate = ?, " +
								"@VetKey = ?, " +
								"@companyPrefix = ?, " +
								"@bulkLoad = ?," +
								"@CVIKey = ? ) } ";
						sQuery = sInsCVIQuery;
						CallableStatement cs = newConn.prepareCall(sInsCVIQuery);
						cs = newConn.prepareCall(sQuery);
						cs.setString(1, sImport);
						cs.setNull(2, java.sql.Types.VARCHAR); // Explicit null to force assignment
						cs.setString(3, data.getSourceState());
						cs.setString(4, data.getSourcePin());
						cs.setString(5, data.getSourceFarm());
						cs.setString(6, data.getDestState());
						cs.setString(7, data.getDestPin());
						cs.setString(8, data.getDestFarm());
						cs.setDate(9, new java.sql.Date(dDateIssued.getTime()));
						cs.setDate(10, new java.sql.Date(dDateReceived.getTime()));
						cs.setInt(11, iIssuedBy);
						cs.setString(12, sCoPrefix);
						cs.setString(13, "Y"); // Bulk load
						cs.registerOutParameter(14, java.sql.Types.INTEGER);
						cs.execute();
						int iCurrentCVIKey = cs.getInt(14);
						if( iCurrentCVIKey > 0 ) {
							// If and when we select from HERDS species use " +CivetConfig.getDbDatabaseName()+"."+CivetConfig.getDbSchemaName()+".InsCVIUSDASpecies with 
							// AnimalClassHierarchyKey in place of usda species code
							// CVIKey, USDA Species Code, Number, Username
							// Call as executeUpdate
							String sInsSpeciesQuery = "EXEC " +CivetConfig.getDbDatabaseName()+"."+CivetConfig.getDbCivetSchemaName()+".InsCVIUSDASpecies ?, ?, ?, ?, ?";
							sQuery = sInsSpeciesQuery;
							ps = newConn.prepareStatement(sQuery);
							ps.setInt(1, iCurrentCVIKey);
							ps.setString(2, sSpecies);
							ps.setNull(3, java.sql.Types.VARCHAR);
							ps.setInt(4, iNumber);
							ps.setString(5, sUserID);
							ps.executeUpdate();
						}
						ps.close();
					} // Next Row
				}
				catch (SQLException ex) {
					logger.error(ex);
				}
			    finally {
			    	try {
						if( newConn != null && !newConn.isClosed() )
							newConn.close();
					} catch (SQLException e) {
						// Oh well, we tried.
					}
			    }
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						prog.setVisible(false);
						prog.dispose();
						onThreadComplete(TWorkCSV.this);
					}
				});
			} catch (IOException e2) {
				// File Error on the data file to be read
				logger.error(e2);
			}
		}

	}// end inner class TWorkSave


	public int getVetKey( String sVet ) {
		return getVetKey( sVet, true );
	}

	public int getVetKey( String sVet, boolean bCheckAccred ) {
		StringTokenizer tok = new StringTokenizer( sVet, " ," );
		String sFirst = tok.nextToken();
		String sLast = tok.nextToken();
//		System.out.println(":" + sFirst + ":" +sLast + ":");
		int iVet = -1;
		String sQuery = "SELECT v.VetKey \n" +
                        "FROM "+CivetConfig.getDbDatabaseName()+"."+CivetConfig.getDbHerdsSchemaName()+".Vets v \n" +
                        "JOIN "+CivetConfig.getDbDatabaseName()+"."+CivetConfig.getDbHerdsSchemaName()+".Accounts a ON a.AccountKey = v.AccountKey \n" +
                        "LEFT JOIN "+CivetConfig.getDbDatabaseName()+"."+CivetConfig.getDbHerdsSchemaName()+".VetCertificates vc ON vc.VetKey = v.VetKey\n" +
                        "JOIN "+CivetConfig.getDbDatabaseName()+"."+CivetConfig.getDbHerdsSchemaName()+".VetCertificateTypes vct ON vct.VetCertificateTypeKey = vc.VetCertificateTypeKey \n" +
                        "WHERE a.FirstName = ? AND a.LastName = ?";
		if( bCheckAccred ) {
            sQuery += "\n  AND vct.Description IN ('USDA Level II Accreditation','USDA Level I Accreditation')";
		}
		Connection newConn = factory.makeDBConnection();
		if( newConn == null ) {
			logger.error("Null newConn in openDBRecord");
			MessageDialog.showMessage((Window)null, "Civet: Database Error", "Could not connect to database");
			return -1;
		}
        try {
			PreparedStatement ps = newConn.prepareStatement(sQuery);
			ps.setString(1, sFirst);
			ps.setString(2, sLast);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				iVet = rs.getInt(1);
			}
		}
		catch (SQLException ex) {
			logger.error( ex.getMessage() + "\nError in query\n" + sQuery );
		}
        finally {
        	try {
    			if( newConn != null && !newConn.isClosed() )
    				newConn.close();
    		} catch (SQLException e) {
    			// Oh well, we tried.
    		}
        }
		return iVet;
	}

	@Override
	public void onThreadComplete( Thread thread ) {
		// TODO Auto-generated method stub
		// Do what needs to be done in GUI thread.
		if( thread instanceof TWorkCSV ) {
			System.out.println("Import Complete");
			//System.exit(1);
		}
	}
	
	private int getStateKey( String sStateCode ) {
		int iStateKey = -1;
		String sQuery = "SELECT top 1 StateKey FROM "+CivetConfig.getDbDatabaseName()+"."+CivetConfig.getDbHerdsSchemaName()+".allCounties \n" +
					    " WHERE StateCode = ? and CountryCode = 'USA'";
		Connection conn = factory.makeDBConnection();
		try {
			PreparedStatement ps = conn.prepareStatement(sQuery);
			ps.setString(1, sStateCode);
			ResultSet rs = ps.executeQuery();
			if( rs.next() ) {
				iStateKey = rs.getInt(1);
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			logger.error(e.getMessage() + "\nError in query\n" + sQuery);
		} finally {
			if( conn != null )
				try {
					conn.close();
				} catch (SQLException e) {
				}
		}
		return iStateKey;
	}

	@Override
	public String getMenuText() {
		return "Import Swine Movement CSV File";
	}

	@Override
	public void execute(Window parent) {
		doImportCSV(parent);
	}

}
