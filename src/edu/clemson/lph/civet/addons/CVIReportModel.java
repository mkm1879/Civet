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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.apache.log4j.*;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.CivetConfig;
import edu.clemson.lph.db.*;


@SuppressWarnings("serial")
public class CVIReportModel extends AbstractTableModel implements ThreadListener {
	private DatabaseConnectionFactory factory;
	private ArrayList<String> aColNames;
	private ArrayList<ArrayList<String>> aRows;
	private static final String[] aSpecies = {"BOV","OVI","CAP","EQU","POR","POU","CAM"};
	private java.sql.Date dStart;
	private java.sql.Date dEnd;
	private ArrayList<ThreadListener> aListeners = new ArrayList<ThreadListener>();
	private  static final Logger logger = Logger.getLogger(Civet.class.getName());
	
	public CVIReportModel( DatabaseConnectionFactory factory, java.util.Date dStart, java.util.Date dEnd ) {
		this.factory = factory;
		this.dStart = new java.sql.Date(dStart.getTime());
		this.dEnd = new java.sql.Date(dEnd.getTime());
		aColNames = new ArrayList<String>();
		aColNames.add("Item to Measure");
		aColNames.add("Cattle");
		aColNames.add("Sheep");
		aColNames.add("Goats");
		aColNames.add("Horses");
		aColNames.add("Swine");
		aColNames.add("Poultry");
		aColNames.add("Camelids");
		aRows = new ArrayList<ArrayList<String>>();
		aRows.add(new ArrayList<String>());
		aRows.get(0).add("ICVIs Created");
		aRows.add(new ArrayList<String>());
		aRows.get(1).add("ICVIs Received");
		aRows.add(new ArrayList<String>());
		aRows.get(2).add("Animals permitted to be moved in");
		aRows.add(new ArrayList<String>());
		aRows.get(3).add("Animals permitted to be moved out");
		aRows.add(new ArrayList<String>());
		aRows.get(4).add("States Shipped From");
		aRows.add(new ArrayList<String>());
		aRows.get(5).add("States Shipped To");
	}

	@Override
	public int getColumnCount() {
		int iRet = 0;
		if( aColNames != null )
			iRet = aColNames.size();
		return iRet;
	}

	@Override
	public int getRowCount() {
		int iRet = 0;
		if( aRows != null )
			iRet = aRows.size();
		return iRet;
	}
	
	public void addThreadListener( ThreadListener threadListener ) {
		aListeners.add( threadListener );
	}

	@Override
	public Object getValueAt(int arg0, int arg1) {
		String sRet = "";
		if( arg0 >= 0 && arg0 < getRowCount() && arg1 >= 0 && arg1 < aRows.get(arg0).size() )
			sRet = aRows.get(arg0).get(arg1);
		return sRet;
	}
	javax.swing.table.TableModel mod;
	public String getColumnName( int iColIndex ) {
		String sRet = "";
		if( iColIndex >= 0 && iColIndex < aColNames.size() )
			sRet = aColNames.get(iColIndex);
		return sRet;
	}
	
	public Thread refresh() {
		DoWork work = new DoWork();
		work.start();
		return work;
	}
	
	class DoWork extends Thread {
		@Override
		public void run() {
			String sQuery = null;
			Connection conn = null;
			try {
				conn = factory.makeDBConnection();
				if( conn == null ) {
					return;
				}
				String sQueryCVIsIn = "select count(*) \n" + 
                                      "from "+CivetConfig.getDbDatabaseName()+"."+CivetConfig.getDbCivetSchemaName()+".vw_CVISummary c  \n" + 
                                      "where c.InspectionDate between ? and ? \n" + 
                                      "and c.USDACode = ? and c.Import = 'Y'";
	            sQuery = sQueryCVIsIn;
	            PreparedStatement psCVIsIn = conn.prepareStatement(sQuery);
	            String sQueryCVIsOut = "select count(*) \n" + 
                                       "from "+CivetConfig.getDbDatabaseName()+"."+CivetConfig.getDbCivetSchemaName()+".vw_CVISummary c  \n" + 
                                       "where c.InspectionDate between ? and ? \n" + 
                                       "and c.USDACode = ? and c.Import = 'N' and c.DestinationState <> 'SC'";
	            sQuery = sQueryCVIsOut;
	            PreparedStatement psCVIsOut = conn.prepareStatement(sQuery);
	            String sQueryAnimalsIn = "select sum(c.Number) \n" + 
                        "from "+CivetConfig.getDbDatabaseName()+"."+CivetConfig.getDbCivetSchemaName()+".vw_CVISummary c  \n" + 
                        "where c.InspectionDate between ? and ? \n" + 
                        "and c.USDACode = ? and c.Import = 'Y'";
	            sQuery = sQueryAnimalsIn;
	            PreparedStatement psAnimalsIn = conn.prepareStatement(sQuery);
	            String sQueryAnimalsOut = "select sum(c.Number) \n" + 
                        "from "+CivetConfig.getDbDatabaseName()+"."+CivetConfig.getDbCivetSchemaName()+".vw_CVISummary c  \n" + 
                        "where c.InspectionDate between ? and ? \n" + 
                        "and c.USDACode = ? and c.Import = 'N'";
	            sQuery = sQueryAnimalsOut;
	            PreparedStatement psAnimalsOut = conn.prepareStatement(sQuery);
	            String sQueryStatesIn = "select count(distinct c.OriginState) \n" + 
                        "from "+CivetConfig.getDbDatabaseName()+"."+CivetConfig.getDbCivetSchemaName()+".vw_CVISummary c  \n" + 
                        "where c.InspectionDate between ? and ? \n" + 
                        "and c.USDACode = ? and c.Import = 'Y'";
	            sQuery = sQueryStatesIn;
	            PreparedStatement psStatesIn = conn.prepareStatement(sQuery);
	            String sQueryStatesOut = "select count(distinct c.DestinationState) \n" + 
                        "from "+CivetConfig.getDbDatabaseName()+"."+CivetConfig.getDbCivetSchemaName()+".vw_CVISummary c  \n" + 
                        "where c.InspectionDate between ? and ? \n" + 
                        "and c.USDACode = ? and c.Import = 'N' and c.DestinationState <> 'SC'";
	            sQuery = sQueryStatesOut;
	            PreparedStatement psStatesOut = conn.prepareStatement(sQuery);
				ResultSet rs;
				for( String sSpecies : aSpecies ) {
					psCVIsIn.setDate(1, dStart);
					psCVIsIn.setDate(2, dEnd);
					psCVIsIn.setString(3, sSpecies);
					rs = psCVIsIn.executeQuery();
					if( rs.next() ) {
						aRows.get(0).add(rs.getString(1));
					}
					else {
						aRows.get(0).add("");						
					}
					psCVIsIn.close();
					psCVIsOut.setDate(1, dStart);
					psCVIsOut.setDate(2, dEnd);
					psCVIsOut.setString(3, sSpecies);
					rs = psCVIsOut.executeQuery();
					if( rs.next() ) {
						aRows.get(1).add(rs.getString(1));
					}
					else {
						aRows.get(1).add("");						
					}
					psCVIsOut.close();
					psAnimalsIn.setDate(1, dStart);
					psAnimalsIn.setDate(2, dEnd);
					psAnimalsIn.setString(3, sSpecies);
					rs = psAnimalsIn.executeQuery();
					if( rs.next() ) {
						aRows.get(2).add(rs.getString(1));
					}
					else {
						aRows.get(2).add("");						
					}
					psAnimalsIn.close();
					psAnimalsOut.setDate(1, dStart);
					psAnimalsOut.setDate(2, dEnd);
					psAnimalsOut.setString(3, sSpecies);
					rs = psAnimalsOut.executeQuery();
					if( rs.next() ) {
						aRows.get(3).add(rs.getString(1));
					}
					else {
						aRows.get(3).add("");						
					}
					psAnimalsOut.close();
					psStatesIn.setDate(1, dStart);
					psStatesIn.setDate(2, dEnd);
					psStatesIn.setString(3, sSpecies);
					rs = psStatesIn.executeQuery();
					if( rs.next() ) {
						aRows.get(4).add(rs.getString(1));
					}
					else {
						aRows.get(4).add("");						
					}
					psStatesIn.close();
					psStatesOut.setDate(1, dStart);
					psStatesOut.setDate(2, dEnd);
					psStatesOut.setString(3, sSpecies);
					rs = psStatesOut.executeQuery();
					if( rs.next() ) {
						aRows.get(5).add(rs.getString(1));
					}
					else {
						aRows.get(5).add("");						
					}
					psStatesOut.close();
				}
			} catch (SQLException e) {
				logger.error( e.getMessage() + "\nError in query\n" + sQuery);
			}
			finally {
				try {
					if( conn != null )
						conn.close();
				} catch (SQLException e) {
				}
			}
			// Then let GUI update itself in main thread.
			SwingUtilities.invokeLater( new Runnable() {
				@Override
				public void run() {
					for( ThreadListener listener : aListeners ) {
						listener.onThreadComplete( DoWork.this );
					}
					CVIReportModel.this.fireTableStructureChanged();
				}
			});
		}
	}

	  File csvFile = null;
	  public String exportCSV( File csvFile ) {
	    String sRet = "Exporting file " + csvFile.getPath();
	    this.csvFile = csvFile;
	    Thread t = new Thread( new Runnable() {
	      public void run() {
	        runExport();
	      }
	    } );
	    t.start();
	    return sRet;
	  }

	  public void runExport() {
	    StringBuffer sb = new StringBuffer();
	    try {
	        BufferedWriter bw = new BufferedWriter(new FileWriter(csvFile));
	        // Write the column names
	        boolean bFirst = true;
	        for( String sNext : aColNames ) {
	          if( !bFirst ) sb.append(',');
	          else bFirst = false;
	          sb.append(sNext);
	        }
	        bw.write(sb.toString());
	        bw.newLine();
	        for( ArrayList<String> aRow : aRows ) {
	          sb.setLength(0);
	          int iCol = 0;
	          for( String sVal : aRow ) {
	            if( iCol++ > 0 ) sb.append(',');
	            sb.append( sVal );
	          }
	          bw.write(sb.toString());
	          bw.newLine();
	        }
	        bw.flush();
	        bw.close();
	      }
	      catch (IOException ioe) {
	        logger.error(ioe.getMessage() + "\nError writing export file");
	      }
	  }

	@Override
	public void onThreadComplete(Thread thread) {
	};

}
