package edu.clemson.lph.civet;
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
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;
import org.apache.log4j.Logger;
import edu.clemson.lph.civet.xml.StdeCviXmlModel;
import edu.clemson.lph.civet.xml.elements.Animal;
import edu.clemson.lph.civet.xml.elements.AnimalTag;
import edu.clemson.lph.civet.xml.elements.SpeciesCode;
import edu.clemson.lph.dialogs.MessageDialog;
import edu.clemson.lph.utils.IDTypeGuesser;

@SuppressWarnings("serial")
public class AnimalIDListTableModel extends AbstractTableModel {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
	private StdeCviXmlModel xmlModel;
	private ArrayList<Animal> deletedAnimals;  // Stored as native data compatible with XML
	private ArrayList<AnimalIDRecord> rows;  // Stored in table model friendly format.
	
	public AnimalIDListTableModel( StdeCviXmlModel xmlModel ) {
		this.xmlModel = xmlModel;
		deletedAnimals = new ArrayList<Animal>();
		if( xmlModel != null ) {
			ArrayList<Animal>animals = xmlModel.getAnimals();
			rows = new ArrayList<AnimalIDRecord>();
			for( Animal animal : animals ) {
				AnimalIDRecord row = new AnimalIDRecord( animal );
				rows.add(row);
			}
		}
		else {
			rows = new ArrayList<AnimalIDRecord>();
		}
	}
	
	public void save() {
		for( Animal animal : deletedAnimals ) {
			xmlModel.removeAnimal(animal);
		}
		for( AnimalIDRecord record : rows ) {
			Animal animalIn = record.animal;
			if( animalIn.eAnimal == null ) // don't try to add one we read from model in the first place
				animalIn.eAnimal = xmlModel.addAnimal(animalIn); // Now in model so track as element from now on
			else 
				xmlModel.editAnimal(animalIn);
		}
	}
	
	/**
	 * The primary way to add an identified animal to the XML data.
	 * @param animalIn
	 */
	public void addRow( Animal animalIn ) {
		AnimalIDRecord r = new AnimalIDRecord( animalIn );
		if( rows.contains(r) ) {
			MessageDialog.showMessage(null, "Civet: Duplicate ID", "ID " + animalIn.getFirstOfficialID() + 
					                        " is already in the list for species " + animalIn.speciesCode.text);
		}
		else {
			rows.add(r);
			fireTableDataChanged();
		}
	}	
	
	public void addRow( String sSpeciesCode, String sTag ) {
		SpeciesCode sCode = new SpeciesCode( sSpeciesCode );
		// Assume if we are entering here, they are official guess but use OtherOfficial otherwise.
		AnimalTag.Types type = IDTypeGuesser.getTagType(sTag);
		if( type == null || type == AnimalTag.Types.ManagementID )
			type = AnimalTag.Types.OtherOfficialID;
		addRow( new Animal( sCode, type , sTag ) );
	}
	
	public void deleteRow( AnimalIDRecord rowToDel ) {
		rows.remove(rowToDel);
		deletedAnimals.add(rowToDel.animal);
		fireTableDataChanged();
	}
	
	public void deleteRows( int tableRows[] ) {
		ArrayList<AnimalIDRecord> aDelRows = new ArrayList<AnimalIDRecord>();
		for( int i = 0; i < tableRows.length; i++ ) {
			aDelRows.add(rows.get(tableRows[i]));
		}
		for( AnimalIDRecord r : aDelRows )
			deleteRow( r );
	}
	
	public final ArrayList<AnimalIDRecord> getRows() {
		final ArrayList<AnimalIDRecord> rowsOut = rows;
		return rowsOut;
	}
	
	/**
	 * Currently ONLY used by 9-3
	 * @return
	 */
	public final ArrayList<AnimalIDRecord> cloneRows() {
		final ArrayList<AnimalIDRecord> rowsOut = new ArrayList<AnimalIDRecord>();
		for( AnimalIDRecord record : rows ) {
			rowsOut.add(record);
		}
		return rowsOut;
	}
	
	// Table model stuff.
	@Override
	public String getColumnName( int arg0 ) {
		if( arg0 == 0 )
			return "Row";
		else if( arg0 == 1 )
			return "Species";
		else if( arg0 == 2 )
			return "Animal ID";
		else 
			return null;
	}
	
	@Override
	public Class<?> getColumnClass( int column ) {
		switch (column) {
		case 0:
			return Integer.class;
		case 1:
			return String.class;
		case 2:
			return String.class;
		default:
			return String.class;
		}
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public int getRowCount() {
		return rows.size();
	}

	@Override
	public Object getValueAt(int iRow, int iCol) {
		if( rows != null &&  iCol >=0 && iCol <= 2 && iRow >=0 && iRow < rows.size() ) {
			if( iCol == 0 )
				return iRow + 1; // Integer.toString(iRow + 1);
			if( iCol == 1 )
				return rows.get(iRow).animal.speciesCode.code;
			if( iCol == 2)
				return rows.get(iRow).animal.getFirstOfficialID();
			else
				return null;
		}
		else
			return null;
	}

	public String getSpeciesCodeAt(int iRow) {
		if( iRow >=0 && iRow < rows.size() ) {
			return rows.get(iRow).animal.speciesCode.code;
		}
		else
			return null;
	}

	public int getRowIDAt(int iRow) {
		if( iRow >=0 && iRow < rows.size() ) {
			return rows.get(iRow).iRowID;
		}
		else
			return -1;
	}
}
