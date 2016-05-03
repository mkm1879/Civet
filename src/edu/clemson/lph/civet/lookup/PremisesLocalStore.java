package edu.clemson.lph.civet.lookup;
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
import java.util.HashMap;
import org.apache.log4j.Logger;
import edu.clemson.lph.civet.Civet;

public class PremisesLocalStore {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	private ArrayList<PremRecord> aPrems = null;
	private HashMap<String,PremRecord> hPinMap = null;
	private HashMap<String,ArrayList<PremRecord>> hPhoneMap = null;
	private HashMap<StringPair,ArrayList<PremRecord>> hAddressMap = null;

	public PremisesLocalStore() {
		aPrems = new ArrayList<PremRecord>();
		hPinMap = new HashMap<String,PremRecord>();
		hPhoneMap = new HashMap<String,ArrayList<PremRecord>>();
		hAddressMap = new HashMap<StringPair,ArrayList<PremRecord>>();
	}
	
	public synchronized void addPremises( String sPremisesId, String sPremisesName, String sAddress, String sCity,			
								String sCounty, String sState, String sZip, String sPhone ) {
		try {
		PremRecord rNew = new PremRecord( sPremisesId, sPremisesName, sAddress, sCity, sCounty, sState, sZip, sPhone );
		PremRecord rPrem = containedIn( aPrems, rNew );
		if( rPrem != null ) {
			rPrem.updatePremRecord(rNew);
		}		
		else {
			rPrem = rNew;
			aPrems.add(rPrem);
		}
		if( sPremisesId != null && sPremisesId.trim().length() > 0 ) {
			PremRecord rFound = getPremisesByPin(sPremisesId);
			if( rFound == null ) {
				hPinMap.put(sPremisesId, rPrem);
			}
		}
		if( sPhone != null && sPhone.trim().length() > 0 ) {
			ArrayList<PremRecord> aPrems = hPhoneMap.get(sPhone);
			if( aPrems != null ) {
				PremRecord rFound = containedIn(aPrems, rPrem);
				if( rFound == null ) {
					aPrems.add(rPrem);
				}
			}
			else {
				aPrems = new ArrayList<PremRecord>();
				aPrems.add(rPrem);
				hPhoneMap.put(sPhone, aPrems);
			}
		}
		if( sAddress != null && sAddress.trim().length() > 0 && sCity != null && sCity.trim().length() > 0 ) {
			StringPair aKey = new StringPair();
			aKey.s1 = sAddress;
			aKey.s2 = sCity;
			ArrayList<PremRecord> aPrems = hAddressMap.get(aKey);
			if( aPrems != null ) {
				PremRecord rFound = containedIn(aPrems, rPrem);
				if( rFound == null ) {
					aPrems.add(rPrem);
				}
			}
			else {
				aPrems = new ArrayList<PremRecord>();
				aPrems.add(rPrem);
				hAddressMap.put(aKey, aPrems);
			}
		}
		if( sAddress != null && sAddress.trim().length() > 0 && sZip != null && sZip.trim().length() > 0 ) {
			StringPair aKey = new StringPair();
			aKey.s1 = sAddress;
			aKey.s2 = sZip;
			ArrayList<PremRecord> aPrems = hAddressMap.get(aKey);
			if( aPrems != null ) {
				PremRecord rFound = containedIn(aPrems, rPrem);
				if( rFound == null ) {
					aPrems.add(rPrem);
				}
			}
			else {
				aPrems = new ArrayList<PremRecord>();
				aPrems.add(rPrem);
				hAddressMap.put(aKey, aPrems);
			}
		}
		} catch( Exception e ) {
			logger.error(e);
		}
	}
	
	public ArrayList<PremRecord> getAllRows() {
		return aPrems;
	}
	
	
	
	public PremRecord getPremisesByPin( String sPin ) {
		PremRecord pOut = hPinMap.get(sPin);
		return pOut;
	}
	
	public ArrayList<PremRecord> getPremisesByPhone( String sPhone ) {
		if( sPhone == null || sPhone.trim().length() == 0 ) return null;
		ArrayList<PremRecord> apOut = hPhoneMap.get(sPhone);
		return apOut;
	}
	
	public ArrayList<PremRecord> getPremisesByAddressCity( String sAddress, String sCity ) {
		if( sAddress == null || sAddress.trim().length() == 0 ||  sCity == null || sCity.trim().length() == 0) return null;
		StringPair aKey = new StringPair();
		aKey.s1 = sAddress;
		aKey.s2 = sCity;
		ArrayList<PremRecord> apOut = hAddressMap.get(aKey);
		return apOut;
	}
	
	public ArrayList<PremRecord> getPremisesByAddressZip( String sAddress, String sZip ) {
		if( sAddress == null || sAddress.trim().length() == 0 ||  sZip == null || sZip.trim().length() == 0) return null;
		StringPair aKey = new StringPair();
		aKey.s1 = sAddress;
		aKey.s2 = sZip;
		ArrayList<PremRecord> apOut = hAddressMap.get(aKey);
		return apOut;
	}
	
	public void print() {
		System.out.println("Keys:");
		for( StringPair key : hAddressMap.keySet() ) {
			System.out.println( key.s1 + " : " + key.s2 );
		}
		System.out.println("Records");
		for( PremRecord r: aPrems) r.print();
		System.out.println("Records by Pin");
		for( String sKey : hPinMap.keySet() ) {
			PremRecord r = hPinMap.get(sKey);
			r.print();
		}
		System.out.println("Records by Address");
		for( StringPair sKey : hAddressMap.keySet() ) {
			ArrayList<PremRecord> ar = hAddressMap.get(sKey);
			if( ar != null )
				for( PremRecord r : ar) r.print();
		}
		System.out.println("Records by Phone");
		for( String sKey : hPhoneMap.keySet() ) {
			ArrayList<PremRecord> ar = hPhoneMap.get(sKey);
			if( ar != null )
				for( PremRecord r : ar) r.print();
		}
	}
	
	private PremRecord containedIn( ArrayList<PremRecord> aPrems, PremRecord rPrem) {
		for( PremRecord rNext : aPrems ) {
			if( rNext.sameAs(rPrem) ) return rNext;
		}
		return null;
	}
	
	public static class PremRecord {
		public String sPremisesId = null;
		public String sPremisesName = null;
		public String sAddress;
		public String sCity;
		public String sCounty;
		public String sState;
		public String sZip;
		public String sPhone;
		
		public PremRecord( String sPremisesId, String sPremisesName, String sAddress, String sCity,
							String sCounty, String sState, String sZip, String sPhone ) {
			this.sPremisesId = sPremisesId;
			this.sPremisesName = sPremisesName;
			this.sAddress = sAddress;
			this.sCity = sCity;
			this.sCounty = sCounty;
			this.sState = sState;
			this.sZip = sZip;
			this.sPhone = sPhone;
		}
		
		public void print() {
			System.out.println(sPremisesId + ":" + sAddress + ":" + sCity + ":" + sState + ":" + sZip + ":" + sPhone );			
		}
		
		public void updatePremRecord( PremRecord r ) {
			if( r.sPremisesId != null && !r.sPremisesId.equalsIgnoreCase(this.sPremisesId) )
				this.sPremisesId = r.sPremisesId;
			if( r.sPremisesName != null && !r.sPremisesName.equalsIgnoreCase(this.sPremisesName) )
				this.sPremisesName = r.sPremisesName;
			if( r.sAddress != null && !r.sAddress.equalsIgnoreCase(this.sAddress) )
				this.sAddress = r.sAddress;
			if( r.sCity != null && !r.sCity.equalsIgnoreCase(this.sCity) )
				this.sCity = r.sCity;
			if( sState != null && !r.sState.equalsIgnoreCase(this.sState) )
				this.sState = r.sState;
			if( r.sZip != null && !r.sZip.equalsIgnoreCase(this.sZip) )
				this.sZip = r.sZip;
			if( r.sPhone != null && !r.sPhone.equalsIgnoreCase(this.sPhone) )
				this.sPhone = r.sPhone;
		}
		
		/**
		 * This is only used in containedIn.  Only the hashcode of the keys is used for lookup.
		 * @param r
		 * @return
		 */
		public boolean sameAs( PremRecord r ) {
			// PIN match trumps
			if( sPremisesId != null && r.sPremisesId != null && sPremisesId.equalsIgnoreCase(r.sPremisesId) ) return true;
			// Any mismatch means not equal
			if( sAddress != null && r.sAddress != null && !sAddress.equalsIgnoreCase(r.sAddress) ) return false;
			if( sCity != null && r.sCity != null && !sCity.equalsIgnoreCase(r.sCity) ) return false;
			if( sState != null && r.sState != null && !sState.equalsIgnoreCase(r.sState) ) return false;
			if( sZip != null && r.sZip != null && !sZip.equalsIgnoreCase(r.sZip) ) return false;
			if( sPhone != null && r.sPhone != null && !sPhone.equalsIgnoreCase(r.sPhone) ) return false;
			return true;
		}

	}
	
	public static class StringPair {
		public String s1;
		public String s2;	
		
		@Override
		public boolean equals( Object o ) {
			if( !(o instanceof StringPair) ) return false;
			StringPair sIn = (StringPair)o;
			if( sIn.s1 == null || sIn.s2 == null ) return false;
			return (sIn.s1.equals(s1) && sIn.s2.equals(s2));
		}
		
		@Override
		public int hashCode() {
			return s1.hashCode() + s2.hashCode();
		}
	}

}
