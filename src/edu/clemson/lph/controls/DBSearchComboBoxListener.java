package edu.clemson.lph.controls;
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


public interface DBSearchComboBoxListener{
  /**
   * Create a new item for this comboBox's list, refresh the box content
   * and set the selection to the new item.
   * @param cbSource DBComboBox
   */
  public void newItem( DBSearchComboBox cbSource );
  /**
   * Edit the currently selected item.
   * @param cbSource DBComboBox
   */
  public void editItem( DBSearchComboBox cbSource );
  /**
   * Used as an alternative to the built-in search table. Leave or set
   * search query to null to prevent double search logic.  Set comboBox
   * selection to chosen item.
   * @param cbSource DBComboBox
   */
  public void findItem( DBSearchComboBox cbSource );

} // End interface DBSearchComboBoxListener

