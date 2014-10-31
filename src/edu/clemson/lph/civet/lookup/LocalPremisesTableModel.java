package edu.clemson.lph.civet.lookup;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;






import java.util.Arrays;
import java.util.List;
import java.util.zip.DataFormatException;

import javax.swing.event.TableModelListener;

import org.apache.log4j.Logger;

import edu.clemson.lph.civet.Civet;
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
import edu.clemson.lph.civet.webservice.PremisesTableModel;
import edu.clemson.lph.utils.CSVWriter;
import edu.clemson.lph.utils.LabeledCSVParser;

public class LocalPremisesTableModel implements PremisesTableModel {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	private static PremisesLocalStore dataStore = null;
	private ArrayList<PremisesLocalStore.PremRecord> rows = null;
	private int iCurrentRow = -1;
	
	private ArrayList<TableModelListener> listeners = new ArrayList<TableModelListener>();

	public LocalPremisesTableModel( String sPhone ) {
		if( dataStore == null ) readData();
		rows = dataStore.getPremisesByPhone(sPhone);
		if( rows == null ) {
			rows = new ArrayList<PremisesLocalStore.PremRecord>(); 
			iCurrentRow = -1;
		}
		else
			iCurrentRow = 0;
	}
	
	public LocalPremisesTableModel( String sFederalPremId, String sStatePremId ) {
		if( dataStore == null ) readData();
		rows = new ArrayList<PremisesLocalStore.PremRecord>();
		String sPremId = sFederalPremId;
		if( sPremId == null || sPremId.trim().length() == 0 )
			sPremId = sStatePremId;
		if( sPremId != null && sPremId.trim().length() > 0 ) {
			PremisesLocalStore.PremRecord r = dataStore.getPremisesByPin(sPremId);
			if( r != null ) {
				rows.add(r);
				iCurrentRow = 0;
			}
			else
				iCurrentRow = -1;
		}
	}
	
	public LocalPremisesTableModel( String sAddress, String sCity, String sStateCode, String sZipCode, String sPhone ) {
		if( dataStore == null ) readData();
		rows = dataStore.getPremisesByPhone(sPhone);
		if( rows == null ) 
			rows = dataStore.getPremisesByAddressCity(sAddress, sCity);
		if( rows == null ) 
			rows = dataStore.getPremisesByAddressZip(sAddress, sZipCode);
		if( rows == null ) {
			rows = new ArrayList<PremisesLocalStore.PremRecord>(); 
			iCurrentRow = -1;
		}
		else
			iCurrentRow = 0;
	}
	
	public static void readData() {
		dataStore = new PremisesLocalStore();
		File fIn = new File("LocalPremList.csv"); 
		if( fIn.exists() && fIn.isFile() ) {

			try {
				LabeledCSVParser parser = new LabeledCSVParser(fIn);
				List<String> lLine = parser.getNext();
				while( lLine != null ) {
					dataStore.addPremises(lLine.get(0), lLine.get(1), lLine.get(2), lLine.get(3), lLine.get(4), lLine.get(5), lLine.get(6));
					lLine = parser.getNext();
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				logger.error(e1);
			}
		}
	}
	
	public static void saveData() {
		CSVWriter writer = new CSVWriter();
		try {
			writer.setHeader( new ArrayList<String>(Arrays.asList("PIN","Name","Address","City","State","Zip","Phone") ) );
			ArrayList<PremisesLocalStore.PremRecord> rows = dataStore.getAllRows();
			for( PremisesLocalStore.PremRecord row : rows ) {
				ArrayList<Object> fields = new ArrayList<Object>();
				fields.add(row.sPremisesId);
				fields.add(row.sPremisesName);
				fields.add(row.sAddress);
				fields.add(row.sCity);
				fields.add(row.sState);
				fields.add(row.sZip);
				fields.add(row.sPhone);
				writer.addRow(fields);
				File fOut = new File("LocalPremList.csv");
				writer.write(fOut);
			}
			
		} catch (DataFormatException e) {
			logger.error(e);
		} catch (FileNotFoundException e) {
			logger.error("File: LocalPremList.csv not found", e);
		}
		
	}
	
	public static PremisesLocalStore getLocalStore() {
		if( dataStore == null ) readData();
		return dataStore;
	}

	@Override
	public String getPremNameAt(int iRow) {
		if( iRow < 0 || iRow >= rows.size() ) return null;
		PremisesLocalStore.PremRecord rPrem = rows.get(iRow);
		return rPrem.sPremisesName;
	}

	@Override
	public String getPremIdAt(int iRow) {
		if( iRow < 0 || iRow >= rows.size() ) return null;
		PremisesLocalStore.PremRecord rPrem = rows.get(iRow);
		return rPrem.sPremisesId;
	}

	@Override
	public String getPhoneAt(int iRow) {
		if( iRow < 0 || iRow >= rows.size() ) return null;
		PremisesLocalStore.PremRecord rPrem = rows.get(iRow);
		return rPrem.sPhone;
	}

	@Override
	public String getAddressAt(int iRow) {
		if( iRow < 0 || iRow >= rows.size() ) return null;
		PremisesLocalStore.PremRecord rPrem = rows.get(iRow);
		return rPrem.sAddress;
	}

	@Override
	public String getCityAt(int iRow) {
		if( iRow < 0 || iRow >= rows.size() ) return null;
		PremisesLocalStore.PremRecord rPrem = rows.get(iRow);
		return rPrem.sCity;
	}

	@Override
	public String getStateCodeAt(int iRow) {
		if( iRow < 0 || iRow >= rows.size() ) return null;
		PremisesLocalStore.PremRecord rPrem = rows.get(iRow);
		return rPrem.sState;
	}

	@Override
	public String getZipCodeAt(int iRow) {
		if( iRow < 0 || iRow >= rows.size() ) return null;
		PremisesLocalStore.PremRecord rPrem = rows.get(iRow);
		return rPrem.sZip;
	}
	
	@Override
	public String getPremName() {
		if( iCurrentRow < 0 || iCurrentRow >= rows.size() ) return null;
		PremisesLocalStore.PremRecord rPrem = rows.get(iCurrentRow);
		return rPrem.sPremisesName;
	}

	@Override
	public String getPremId() {
		if( iCurrentRow < 0 || iCurrentRow >= rows.size() ) return null;
		PremisesLocalStore.PremRecord rPrem = rows.get(iCurrentRow);
		return rPrem.sPremisesId;
	}

	@Override
	public String getPhone() {
		if( iCurrentRow < 0 || iCurrentRow >= rows.size() ) return null;
		PremisesLocalStore.PremRecord rPrem = rows.get(iCurrentRow);
		return rPrem.sPhone;
	}

	@Override
	public String getAddress() {
		if( iCurrentRow < 0 || iCurrentRow >= rows.size() ) return null;
		PremisesLocalStore.PremRecord rPrem = rows.get(iCurrentRow);
		return rPrem.sAddress;
	}

	@Override
	public String getCity() {
		if( iCurrentRow < 0 || iCurrentRow >= rows.size() ) return null;
		PremisesLocalStore.PremRecord rPrem = rows.get(iCurrentRow);
		return rPrem.sCity;
	}

	@Override
	public String getStateCode() {
		if( iCurrentRow < 0 || iCurrentRow >= rows.size() ) return null;
		PremisesLocalStore.PremRecord rPrem = rows.get(iCurrentRow);
		return rPrem.sState;
	}

	@Override
	public String getZipCode() {
		if( iCurrentRow < 0 || iCurrentRow >= rows.size() ) return null;
		PremisesLocalStore.PremRecord rPrem = rows.get(iCurrentRow);
		return rPrem.sZip;
	}


	@Override
	public void clear() {
		rows.clear();
	}

	@Override
	public boolean first() {
		if( rows.size() > 0 ) {
			iCurrentRow = -1;
			return true;
		}
		else
			return false;
	}

	@Override
	public boolean next() {
		if( rows.size() > 0 && iCurrentRow < rows.size() ) {
			iCurrentRow++;
			return true;
		}
		else
			return false;
	}

	@Override
	public int getColumnCount() {
		return 7;
	}

	@Override
	public String getColumnName(int columnIndex) {
		switch( columnIndex ) {
		case 0: return "PIN";
		case 1: return "Name";
		case 2: return "Address";
		case 3: return "City";
		case 4: return "State";
		case 5: return "Zip";
		case 6: return "Phone";
		}
		return null;
	}

	@Override
	public int getRowCount() {
		return rows.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if( rowIndex < 0 || rowIndex >= rows.size() ) return null;
		PremisesLocalStore.PremRecord row = rows.get(rowIndex);
		
		switch( columnIndex ) {
		case 0: return row.sPremisesId;
		case 1: return row.sPremisesName;
		case 2: return row.sAddress;
		case 3: return row.sCity;
		case 4: return row.sState;
		case 5: return row.sZip;
		case 6: return row.sPhone;
		}
		return null;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	@Override
	public void addTableModelListener(TableModelListener arg0) {
		listeners.add(arg0);
	}

	@Override
	public Class<?> getColumnClass(int arg0) {
		return String.class;
	}

	@Override
	public void removeTableModelListener(TableModelListener arg0) {
		listeners.remove(arg0);
	}

	@Override
	public void setValueAt(Object arg0, int arg1, int arg2) {
		return;
	}

}
