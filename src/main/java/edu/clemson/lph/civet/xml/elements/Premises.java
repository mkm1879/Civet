/*
Copyright 2014-2018 Michael K Martin

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

package edu.clemson.lph.civet.xml.elements;
public class Premises {
	public String premid;
	public String premName;
	public Address address;
	public String personName;
	public NameParts personNameParts;
	public String personPhone;
	public String personEmail;
	
	public Premises() {
		this.premid = null;
		this.premName = null;
		this.address = null;
		this.personName = null;
		this.personPhone = null;
		this.personEmail = null;
	}
	
	public Premises(String premid, String premName, Address address ) {
		this.premid = premid;
		this.premName = premName;
		this.address = address;
		this.personName = null;
		this.personPhone = null;
		this.personEmail = null;
	}
	public Premises(String premid, String premName, String sState ) {
		this.premid = premid;
		this.premName = premName;
		this.address = new Address(null, null, null, null, sState, null, null, null, null);
		this.personName = null;
		this.personPhone = null;
		this.personEmail = null;
	}
	public Premises(String premid, String premName, Address address, String personName, String personPhone, String personEmail ) {
		this.premid = premid;
		this.premName = premName;
		this.address = address;
		this.personName = personName;
		this.personPhone = personPhone;
		this.personEmail = personEmail;
	}
	
	public Premises(String premid, String premName, Address address, NameParts personNameParts, String personPhone, String personEmail ) {
		this.premid = premid;
		this.premName = premName;
		this.address = address;
		this.personNameParts = personNameParts;
		this.personPhone = personPhone;
		this.personEmail = personEmail;
	}
}