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
import java.awt.Window;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.CivetConfig;
import edu.clemson.lph.db.DatabaseConnectionFactory;
import edu.clemson.lph.dialogs.MessageDialog;
import edu.clemson.lph.dialogs.ProgressDialog;

public class InsertVspsCviThread extends Thread {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
	Window parent;
	VspsCviFile cviFile;
	ProgressDialog prog;
	DatabaseConnectionFactory factory;

	public InsertVspsCviThread(Window parent, DatabaseConnectionFactory factory,
			VspsCviFile cviFile ) {
		this.parent = parent;
		this.factory = factory;
		this.cviFile = cviFile;
		prog = new ProgressDialog(parent, "Civet", "Saving VSPS CVI");
		prog.setAuto(true);
		prog.setVisible(true);

	}

	@Override
	public void run() {
		String sQuery = null;
		Connection newConn = factory.makeDBConnection();
		if( newConn == null ) {
			logger.error("Null newConn in openDBRecord");
			MessageDialog.showMessage(parent, "Civet: Database Error", "Could not connect to database");
			return;
		}
		VspsCvi cvi;
		try {
			while( (cvi = cviFile.nextCVI() ) != null ) {
				if( cvi.getStatus().equals("SAVED") )  // Ignore Saved but not issued CVIs
					continue;
				if( (cvi.getOrigin() == null || cvi.getOrigin().getState() == null) &&  (cvi.getConsignor() == null || cvi.getConsignor().getState() == null))
					continue;
				if( (cvi.getDestination() == null || cvi.getDestination().getState() == null) &&  (cvi.getConsignee() == null || cvi.getConsignee().getState() == null))
					continue;
				insertCVI ( newConn, cvi );
			}
		} catch (IOException e) {
			logger.error(e);
		}
		catch (SQLException ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\nError in query " + sQuery );
		}
	    finally {
	    	try {
				if( newConn != null && !newConn.isClosed() )
					newConn.close();
			} catch (SQLException e) {
				// Oh well, we tried.
			}
	    	SwingUtilities.invokeLater(new Runnable() {
	    		public void run() {
	    			prog.setVisible(false);
	    			prog.dispose();
	    		}
	    	});
	    }
	}

	private void insertCVI( Connection newConn, VspsCvi cvi ) throws SQLException {
		try {
			String sInsCVIQuery = 
					"{ CALL "+CivetConfig.getDbDatabaseName()+"."+CivetConfig.getDbCivetSchemaName()+".InsVspsCVI( " +
							"@cviNumber = ?, " +
							"@issueState = ?, " +
							"@issuedDate = ?, " +
							"@receivedDate = ?, " +
							"@vetFirstName = ?, " +
							"@vetLastName = ?, " +
							"@permitNbr = ?, " +
							"@originPIN = ?, " +
							"@originFarm = ?, " +
							"@originName = ?, " +
							"@originAddress = ?, " +
							"@originCity = ?, " +
							"@originCounty = ?, " +
							"@originState = ?, " +
							"@originZipCode = ?, " +
							"@originPhone = ?, " +
							"@consignorPIN = ?, " +
							"@consignorFarm = ?, " +
							"@consignorName = ?, " +
							"@consignorAddress = ?, " +
							"@consignorCity = ?, " +
							"@consignorCounty = ?, " +
							"@consignorState = ?, " +
							"@consignorZipCode = ?, " +
							"@consignorPhone = ?, " +
							"@destinationPIN = ?, " +
							"@destinationFarm = ?, " +
							"@destinationName = ?, " +
							"@destinationAddress = ?, " +
							"@destinationCity = ?, " +
							"@destinationCounty = ?, " +
							"@destinationZipCode = ?, " +
							"@destinationState = ?, " +
							"@destinationPhone = ?, " +
							"@consigneePIN = ?, " +
							"@consigneeFarm = ?, " +
							"@consigneeName = ?, " +
							"@consigneeAddress = ?, " +
							"@consigneeCity = ?, " +
							"@consigneeCounty = ?, " +
							"@consigneeState = ?, " +
							"@consigneeZipCode = ?, " +
							"@consigneePhone = ?, " +
							"@remarks = ?, " +
							"@CVIKey = ? ) } ";
			String sQuery = sInsCVIQuery;
			CallableStatement cs = newConn.prepareCall(sQuery);
			int iFieldNo = 1;
			setStringOrNull(cs, iFieldNo++, cvi.getCVINumber() );
			setStringOrNull(cs, iFieldNo++, cvi.getOriginState() );
			setDateOrNull(cs, iFieldNo++, cvi.getInspectionDate() );
			setDateOrNull(cs, iFieldNo++, cvi.getCreateDate() );  // used as received date 
			setStringOrNull(cs, iFieldNo++, cvi.getVetFirstName() );
			setStringOrNull(cs, iFieldNo++, cvi.getVetLastName() );
			setStringOrNull(cs, iFieldNo++, cvi.getPermitNumber() );
			VspsCviEntity origin = cvi.getOrigin();
			setStringOrNull(cs, iFieldNo++, origin.getPremisesId() );
			setStringOrNull(cs, iFieldNo++, origin.getBusiness() );
			setStringOrNull(cs, iFieldNo++, origin.getName() );
			setStringOrNull(cs, iFieldNo++, origin.getAddress1() );
			setStringOrNull(cs, iFieldNo++, origin.getCity() );
			setStringOrNull(cs, iFieldNo++, origin.getCounty() );
			setStringOrNull(cs, iFieldNo++, origin.getState() );
			setStringOrNull(cs, iFieldNo++, origin.getPostalCode() );
			setStringOrNull(cs, iFieldNo++, origin.getPhone() );
			VspsCviEntity consignor = cvi.getConsignor();			
			setStringOrNull(cs, iFieldNo++, consignor.getPremisesId() );
			setStringOrNull(cs, iFieldNo++, consignor.getBusiness() );
			setStringOrNull(cs, iFieldNo++, consignor.getName() );
			setStringOrNull(cs, iFieldNo++, consignor.getAddress1() );
			setStringOrNull(cs, iFieldNo++, consignor.getCity() );
			setStringOrNull(cs, iFieldNo++, consignor.getCounty() );
			setStringOrNull(cs, iFieldNo++, consignor.getState() );
			setStringOrNull(cs, iFieldNo++, consignor.getPostalCode() );
			setStringOrNull(cs, iFieldNo++, consignor.getPhone() );
			VspsCviEntity destination = cvi.getDestination();			
			setStringOrNull(cs, iFieldNo++, destination.getPremisesId() );
			setStringOrNull(cs, iFieldNo++, destination.getBusiness() );
			setStringOrNull(cs, iFieldNo++, destination.getName() );
			setStringOrNull(cs, iFieldNo++, destination.getAddress1() );
			setStringOrNull(cs, iFieldNo++, destination.getCity() );
			setStringOrNull(cs, iFieldNo++, destination.getCounty() );
			setStringOrNull(cs, iFieldNo++, destination.getPostalCode() );
			setStringOrNull(cs, iFieldNo++, destination.getState() );
			setStringOrNull(cs, iFieldNo++, destination.getPhone() );
			VspsCviEntity consignee = cvi.getConsignee();			
			setStringOrNull(cs, iFieldNo++, consignee.getPremisesId() );
			setStringOrNull(cs, iFieldNo++, consignee.getBusiness() );
			setStringOrNull(cs, iFieldNo++, consignee.getName() );
			setStringOrNull(cs, iFieldNo++, consignee.getAddress1() );
			setStringOrNull(cs, iFieldNo++, consignee.getCity() );
			setStringOrNull(cs, iFieldNo++, consignee.getCounty() );
			setStringOrNull(cs, iFieldNo++, consignee.getState() );
			setStringOrNull(cs, iFieldNo++, consignee.getPostalCode() );
			setStringOrNull(cs, iFieldNo++, consignee.getPhone() );
			setStringOrNull(cs, iFieldNo++, cvi.getRemarks() );
			cs.registerOutParameter(iFieldNo, java.sql.Types.INTEGER);
			cs.execute();
			int iCurrentCVIKey = cs.getInt(iFieldNo);

			if( iCurrentCVIKey > 0 ) {
				sQuery = "exec "+CivetConfig.getDbDatabaseName()+"."+CivetConfig.getDbCivetSchemaName()+".InsVspsCVISpecies ?, ?, ?, ?";
				PreparedStatement ps = newConn.prepareStatement(sQuery);
				for( List<String> aKey : cvi.getSpecies().keySet() ) {
					Integer iCount = cvi.getSpecies().get(aKey);
					System.out.println( iCount + " " + aKey.get(0) + " (" + aKey.get(1) + ")" );
					iFieldNo = 1;
					ps.setInt(iFieldNo++, iCurrentCVIKey);
					setStringOrNull(ps, iFieldNo++, aKey.get(0));
					setStringOrNull(ps, iFieldNo++, aKey.get(1));
					ps.setInt(iFieldNo++, iCount);
					ps.executeUpdate();
				}
				ps.close();
				sQuery = "exec "+CivetConfig.getDbDatabaseName()+"."+CivetConfig.getDbCivetSchemaName()+".InsVspsCVIAnimal ?, ?, ?, ?, ?, ?, ?";
				ps = newConn.prepareStatement(sQuery);
				for( VspsCviAnimal animal : cvi.getAnimals() ) {
					if( "A single animal".equalsIgnoreCase(animal.getType()) ) {
						iFieldNo = 1;
						ps.setInt(iFieldNo++, iCurrentCVIKey);
						setStringOrNull(ps, iFieldNo++, animal.getSpecies());
						setStringOrNull(ps, iFieldNo++, animal.getGender());
						setStringOrNull(ps, iFieldNo++, animal.getFirstOfficialId());
						setStringOrNull(ps, iFieldNo++, animal.getFirstOtherId());
						setStringOrNull(ps, iFieldNo++, animal.getBreed());
						setDateOrNull(ps, iFieldNo++, animal.getDateOfBirth());
						ps.executeUpdate();
					}
				}
				ps.close();
			}
			else {
				logger.info("Nothing Inserted");
			}
		} catch( IOException e ) {
			logger.error(e);
		}
	}


	void setStringOrNull( PreparedStatement ps, int parameterIndex, String sValue ) 
			throws SQLException	{
		if( sValue == null ) {
			ps.setNull( parameterIndex, Types.VARCHAR );
		}
		else {
			ps.setString( parameterIndex, sValue );
		}
	}

	void setDateOrNull( PreparedStatement ps, int parameterIndex, java.util.Date dValue )
			throws SQLException	{
		if( dValue == null ) {
			ps.setNull( parameterIndex, Types.DATE );
		}
		else {
			ps.setDate( parameterIndex, new java.sql.Date(dValue.getTime()) );
		}
	}

	void setIntOrNull( PreparedStatement ps, int parameterIndex, Integer iValue )
			throws SQLException	{
		if( iValue == null ) {
			ps.setNull( parameterIndex, Types.INTEGER );
		}
		else {
			ps.setInt( parameterIndex, iValue );
		}
	}
	


}
