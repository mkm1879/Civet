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

	public String getPremNameAt(int iRow);
	public String getPremIdAt(int iRow);
	public String getPhoneAt(int iRow);
	public String getAddressAt(int iRow);
	public String getCityAt(int iRow);
	public String getCountyAt(int iRow);
	public String getStateCodeAt(int iRow);
	public String getZipCodeAt(int iRow);
	public String getPremClassAt(int iRow);
	
	public String getPremName();
	public String getPremId();
	public String getPhone();
	public String getAddress();
	public String getCity();
	public String getCounty();
	public String getStateCode();
	public String getZipCode();
	public String getPremClass();

	public void clear();
	public boolean first();
	public boolean next();
	public int getColumnCount();
	public String getColumnName(int columnIndex);
	public int getRowCount();
	public Object getValueAt(int rowIndex, int columnIndex);
	public boolean isCellEditable(int rowIndex, int columnIndex);

}