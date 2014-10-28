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
import java.util.List;

import javax.swing.*;

import org.apache.log4j.*;

public class BulkLoadNineDashThreeCSV implements ThreadListener, AddOn {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
	private DatabaseConnectionFactory factory;
	
	public BulkLoadNineDashThreeCSV() {
	}

	public void import93CSV( Window parent ) {
		if( factory == null )
			factory = InitAddOns.getFactory();
		String sFilePath = "E:\\EclipseJava\\Civet\\NPIP93Data.csv";  //null;
	    JFileChooser fc = new JFileChooser();
	    fc.setCurrentDirectory(new File(CivetConfig.getBulkLoadDirPath()));
	    fc.setDialogTitle("Open NPIP 9-3 CSV File");
	    fc.setFileFilter( new CSVFilter() );
	    int returnVal = fc.showOpenDialog(parent);
	    if (returnVal == JFileChooser.APPROVE_OPTION) {
	      File file = fc.getSelectedFile();
	      sFilePath = file.getAbsolutePath();
	    }
	    else {
	    	return;
	    }
		importNineDashThreeCSVFile( parent, sFilePath );

	}
	
	private void importNineDashThreeCSVFile( Window parent, String sFilePath ) {
		ProgressDialog prog = new ProgressDialog(parent, "Civet", "Loading CVIs");
		prog.setAuto(false);
		prog.setVisible(true);
		TWork93CSV tWork = new TWork93CSV( prog, sFilePath, parent );
		tWork.start();
	}

	// Also create TWorkAddSpecies and TWorkAddPage
	class TWork93CSV extends Thread {
		String sFilePath;
		ProgressDialog prog;
		Window parent;
		
		public TWork93CSV( ProgressDialog prog, String sFilePath, Window parent ) {
			this.prog = prog;
			this.sFilePath = sFilePath;
			this.parent = parent;
		}
		
		private String formatMessage( int iRow, int iMax ) {
			String sOut = String.format("Record %d of %d imported", iRow, iMax);
			return sOut;
		}
		
		public void run() {
			// Create CSVNineDashThreeDataFile object from CSV file
			CSVNineDashThreeDataFile data;
			try {
				data = new CSVNineDashThreeDataFile( sFilePath );
				int iMax = data.size();
				prog.setMax(iMax);
				int iRow = 0;
				prog.setValue(iRow);
				prog.setMessage( formatMessage( iRow, iMax) );
				
				String sQuery = null;
				Connection newConn = factory.makeDBConnection();
				if( newConn == null ) {
					logger.error("Null newConn in openDBRecord");
					MessageDialog.messageLater((Window)null, "Civet: Database Error", "Could not connect to database");
					exitThread(false);
				}
				try {
					// Iterate over the CSV file
					while( data.nextRow() ) {
						String sCVINumber = data.getCVINumber();
						String sImport = data.isInbound()?"Y":"N";
						String sDate = (new SimpleDateFormat( "MM/dd/yyyy")).format( data.getInspectionDate() );
						String sSpecies = data.getSpecies();
						String sConsignorState = data.getConsignorState();
						if( sConsignorState == null || sConsignorState.trim().length() == 0 )
							sConsignorState = "SC";

						String sSourcePIN = data.getConsignorPIN();
						try {
							if( !PremCheckSum.isValid(sSourcePIN) ) {
								sSourcePIN = null;
							}
						} catch (Exception e1) {
							sSourcePIN = null;
						}
						String sDestinationPIN = data.getConsigneePIN();
						try {
							if( !PremCheckSum.isValid(sDestinationPIN) ) {
								sDestinationPIN = null;
							}
						} catch (Exception e1) {
							sDestinationPIN = null;
						}
						String sDupeCheckQuery = "SELECT * FROM USAHERDS.dbo.CVIs \n" +
								"WHERE CertificateNbr = ? and BureauInternalNote = ?";
						sQuery = sDupeCheckQuery;
						PreparedStatement ps = newConn.prepareStatement(sQuery);
						ps.setString(1, sCVINumber);
						ps.setString(2,  "9-3 Spreadsheet import");
						ResultSet rs = ps.executeQuery();
						if( rs.next() ) {
							MessageDialog.messageLater(parent, "Civet: Duplicate Insert",
											"A 9-3 form appears to be a duplicate.  Skipping.\n" +
											"CVINumber = " + sCVINumber + "\n" +
											"Consignor PIN = " + data.getConsignorPIN() + "\n" +
											"Inspection Date = " + sDate );
							continue;
						}
						ps.close();
						String sSpeciesCheckQuery = "SELECT * FROM USAHERDS.dbo.AnimalClassHierarchy \n" +
								"WHERE CVISpeciesInd = 1 AND CommonName = ?";
						sQuery = sSpeciesCheckQuery;
						ps = newConn.prepareStatement(sQuery);
						ps.setString(1, sSpecies);
						rs = ps.executeQuery();
						if( !rs.next() ) {
							MessageDialog.messageLater(parent, "Civet: Species Missing",
									"Species " + sSpecies + " is not a USAHERDS CVISpecies" );
							continue;
						}
						ps.close();
						String sInsCVIQuery = 
								"{ CALL " +CivetConfig.getDbDatabaseName()+"."+CivetConfig.getDbCivetSchemaName()+".Ins93CVI( " +
										"@import = ?, " +
										"@cviNumber = ?, " +
										"@consignorCountry = ?, " +
										"@consignorState = ?, " +
										"@consignorPIN = ?, " +
										"@consignorName = ?, " +
										"@consignorBusinessName = ?, " +
										"@consignorAddress = ?, " +
										"@consignorCity = ?, " +
										"@consignorZipCode = ?, " +
										"@consigneeCountry = ?, " +
										"@consigneeState = ?, " +
										"@consigneePIN = ?, " +
										"@consigneeName = ?, " +
										"@consigneeBusinessName = ?, " +
										"@consigneeAddress = ?, " +
										"@consigneeCity = ?, " +
										"@consigneeZipCode = ?, " +
										"@issuedDate = ?, " +
										"@species = ?, " +
										"@number = ?, " +
										"@product = ?, " +
										"@CVIKey = ? ) } ";
						sQuery = sInsCVIQuery;
						CallableStatement cs = newConn.prepareCall(sInsCVIQuery);
						cs = newConn.prepareCall(sQuery);
						setStringOrNull(cs, 1, sImport);
						setStringOrNull(cs, 2, sCVINumber);
						setStringOrNull(cs, 3, data.getConsignorCountry());
						setStringOrNull(cs, 4, sConsignorState);
						setStringOrNull(cs, 5, data.getConsignorPIN());
						setStringOrNull(cs, 6, data.getConsignorName());
						setStringOrNull(cs, 7, data.getConsignorBusiness());
						setStringOrNull(cs, 8, data.getConsignorStreet());
						setStringOrNull(cs, 9, data.getConsignorCity());
						setStringOrNull(cs, 10, data.getConsignorZip());
						setStringOrNull(cs, 11, data.getConsigneeCountry());
						setStringOrNull(cs, 12, data.getConsigneeState());
						setStringOrNull(cs, 13, data.getConsigneePIN());
						setStringOrNull(cs, 14, data.getConsigneeName());
						setStringOrNull(cs, 15, data.getConsigneeBusiness());
						setStringOrNull(cs, 16, data.getConsigneeStreet());
						setStringOrNull(cs, 17, data.getConsigneeCity());
						setStringOrNull(cs, 18, data.getConsigneeZip());
						setDateOrNull(cs, 19, data.getInspectionDate());
						setStringOrNull(cs, 20, data.getSpecies());
						setIntOrNull(cs, 21, data.getAnimalCount());
						setStringOrNull(cs, 22, data.getProduct());
						cs.registerOutParameter(23, java.sql.Types.INTEGER);
						cs.execute();
						int iCurrentCVIKey = cs.getInt(23);
						cs.close();
						if( iCurrentCVIKey > 0 ) {
							List<String> lTags = data.listTagIds();
							String sIns93TagQuery = "EXEC " +CivetConfig.getDbDatabaseName()+"."+CivetConfig.getDbCivetSchemaName()+".Ins93CVITag ?, ?, ?";
							sQuery = sIns93TagQuery;
							ps = newConn.prepareStatement(sQuery);
							if( lTags != null) {
								for( String sTag : lTags ) {
									ps.setInt(1, iCurrentCVIKey);
									ps.setString(2, data.getSpecies());
									ps.setString(3, sTag);
									ps.executeUpdate();
								}
							}
							ps.close();
						}
						iRow++;
						if( iRow % 10 == 0 ) {
							prog.setValue(iRow);
							prog.setMessage( formatMessage( iRow, iMax) );
						}
					} // Next Row

				}
				catch (SQLException ex) {
					System.err.println( sQuery );
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
			} catch (IOException e2) {
				// File Error on the data file to be read
				logger.error(e2);
				exitThread(false);
			}
			exitThread(true);
		}

		private void exitThread( boolean bSuccess ) {
			final boolean bDone = bSuccess;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					prog.setVisible(false);
					prog.dispose();
					if( bDone )
						onThreadComplete(TWork93CSV.this);
				}
			});
		}
		
	
	}// end inner class TWorkSave
	
	private void setStringOrNull( CallableStatement cs, int parameterIndex, String sValue ) 
			throws SQLException	{
		if( sValue == null ) {
			cs.setNull( parameterIndex, Types.VARCHAR );
		}
		else {
			cs.setString( parameterIndex, sValue );
		}
	}

	private void setDateOrNull( CallableStatement cs, int parameterIndex, java.util.Date dValue )
			throws SQLException	{
		if( dValue == null ) {
			cs.setNull( parameterIndex, Types.DATE );
		}
		else {
			java.sql.Date dSqlValue = new java.sql.Date( dValue.getTime() );
			cs.setDate( parameterIndex, dSqlValue );
		}
	}

	private void setIntOrNull( CallableStatement cs, int parameterIndex, Integer iValue )
			throws SQLException	{
		if( iValue == null ) {
			cs.setNull( parameterIndex, Types.INTEGER );
		}
		else {
			cs.setInt( parameterIndex, iValue );
		}
	}


	@Override
	public void onThreadComplete( Thread thread ) {
		// TODO Auto-generated method stub
		// Do what needs to be done in GUI thread.
		if( thread instanceof TWork93CSV ) {
			System.out.println("Import Complete");
			//System.exit(1);
		}
	}

	@Override
	public String getMenuText() {
		return "Import NPIP 9-3 CSV File";
	}

	@Override
	public void execute(Window parent) {
		import93CSV(parent);
	}

}

