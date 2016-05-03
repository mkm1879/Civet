package edu.clemson.lph.civet.webservice;

import javax.swing.table.TableModel;

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
public interface PremisesTableModel extends TableModel {

	public abstract String getPremNameAt(int iRow);
	public abstract String getPremIdAt(int iRow);
	public abstract String getPhoneAt(int iRow);
	public abstract String getAddressAt(int iRow);
	public abstract String getCityAt(int iRow);
	public abstract String getCountyAt(int iRow);
	public abstract String getStateCodeAt(int iRow);
	public abstract String getZipCodeAt(int iRow);
	public abstract String getPremClassAt(int iRow);
	
	public abstract String getPremName();
	public abstract String getPremId();
	public abstract String getPhone();
	public abstract String getAddress();
	public abstract String getCity();
	public abstract String getCounty();
	public abstract String getStateCode();
	public abstract String getZipCode();
	public abstract String getPremClass();

	public abstract void clear();
	public abstract boolean first();
	public abstract boolean next();
	public abstract int getColumnCount();
	public abstract String getColumnName(int columnIndex);
	public abstract int getRowCount();
	public abstract Object getValueAt(int rowIndex, int columnIndex);
	public abstract boolean isCellEditable(int rowIndex, int columnIndex);

}