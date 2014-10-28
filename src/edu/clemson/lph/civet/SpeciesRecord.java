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

public class SpeciesRecord {
  public int iSpeciesKey;
  public int iNumber;
  public SpeciesRecord( int iSpeciesKey, int num ) {
    this.iSpeciesKey = iSpeciesKey; this.iNumber = num;
  }
  @Override
  public boolean equals( Object o ) {
	  if( !( o instanceof SpeciesRecord ) ) return false;
	  SpeciesRecord rOther = (SpeciesRecord)o;
	  return rOther.iSpeciesKey == this.iSpeciesKey;
  }
  
  @Override
  public int hashCode() {
	  return iSpeciesKey;
  }
}
