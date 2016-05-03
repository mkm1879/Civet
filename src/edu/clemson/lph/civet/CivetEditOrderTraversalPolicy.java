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
import java.awt.Component;
import java.awt.Container;
import java.awt.ContainerOrderFocusTraversalPolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import edu.clemson.lph.controls.DBComboBox;

/**
 * This class uses the files listed below to lookup local preferences for which 
 * fields to traverse in what order.  Main is followed most of the time, 
 * Alt is used when all fields sticky is on and data already exist.
 * @author mmarti5
 *
 */
@SuppressWarnings("serial")
public class CivetEditOrderTraversalPolicy extends ContainerOrderFocusTraversalPolicy {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
	public static final int PRIMARY_MAP = 1;
	public static final int ALTERNATE_MAP = 2;
	public static final String PRIMARY_FILE = "CivetTabOrderMap.txt";
	public static final String ALTERNATE_FILE = "CivetAltTabOrderMap.txt";
	private ArrayList<Component> aComponents = new ArrayList<Component>();
	private HashMap<String, Integer> hComponentIndexes = new HashMap<String, Integer>();
	private HashMap<Component, Component> hMap = null; // Will point to one of the following
	private HashMap<Component, Component> hMainMap = new HashMap<Component, Component>();
	private HashMap<Component, Component> hAltMap = new HashMap<Component, Component>();
	// Have to have a First component whether user sets it or not so we use a different model 
	// from other properties like pPremisesFound
	private Component cFirst = null;
	private Component cMainFirst = null;
	private Component cAltFirst = null;
	
	private Properties props;
	private Properties mainProps;
	private Properties altProps;

	public CivetEditOrderTraversalPolicy( Container parent ) {
		synchronized( parent.getTreeLock() ) {
			for( Component c : getAllComponents(parent) ) {
				if( ( c instanceof JTextField || c instanceof JComboBox  || c instanceof DBComboBox
						|| c instanceof JButton || c instanceof JCheckBox || c instanceof JRadioButton ) 
					&& !( c instanceof javax.swing.plaf.basic.BasicArrowButton )
					&& c.isFocusable() && c.isEnabled() ) {
					if( c instanceof JTextField && !((JTextField)c).isEditable() )
						continue;
					aComponents.add(c);
				}
			}
		}
		cMainFirst = aComponents.get(0);
		cAltFirst = aComponents.get(0);
		populateControlNameMap();
		selectMainMap();
//   // Keep this code around in case I need to check enumeration keys
//		for( int i = 0; i < aComponents.size(); i++ )
//			System.out.println( "Component i=" + i + " is a " + aComponents.get(i).getClass().getName() );
	}
	
	public void loadComponentOrderMaps() {
		loadComponentOrderMap( PRIMARY_MAP );
		loadFirstComponent( PRIMARY_MAP );
		loadComponentOrderMap( ALTERNATE_MAP );
		loadFirstComponent( ALTERNATE_MAP );
	}
	

	public void resetComponentOrder() {
		hMainMap.clear();
		hAltMap.clear();
	}
	
	public void addComponentOrder( Component cFrom, Component cTo ) {
		if( cFrom != cTo )
			hMainMap.put(cFrom, cTo);
	}
	
	public void setFirstComponent( Component cFirst ) {
		this.cMainFirst = cFirst;
	}
	
	public void addComponentOrder( String sFrom, String sTo ) {
		Integer iFrom =  hComponentIndexes.get(sFrom);
		if( iFrom == null )
			logger.error("Non existent control name used in addComponentOrder From: " + sFrom);
		Integer iTo =  hComponentIndexes.get(sTo);
		if( iTo == null )
			logger.error("Non existent control name used in addComponentOrder To: " + sTo);
		if( iFrom != null && iTo != null ) {
			Component cFrom = aComponents.get( iFrom );
			Component cTo = aComponents.get( iTo );
			if( cFrom != cTo )
				hMainMap.put(cFrom, cTo);
		}
	}
	
	public void setFirstComponent( String sFirst ) {
		Integer iFirst =  hComponentIndexes.get(sFirst);
		if( iFirst == null )
			logger.error("Non existent control name used in setFirstComponent: " + sFirst);
		else {
			cFirst = aComponents.get( hComponentIndexes.get(sFirst) );
			this.cMainFirst = cFirst;
		}
	}
	
	
	public void addAltComponentOrder( Component cFrom, Component cTo ) {
		if( cFrom != cTo )
			hAltMap.put(cFrom, cTo);
	}
	
	public void setAltFirstComponent( Component cFirst ) {
		this.cAltFirst = cFirst;
	}
	
	public void addAltComponentOrder( String sFrom, String sTo ) {
		Integer iFrom =  hComponentIndexes.get(sFrom);
		if( iFrom == null )
			logger.error("Non existent control name used in addComponentOrder From: " + sFrom);
		Integer iTo =  hComponentIndexes.get(sTo);
		if( iTo == null )
			logger.error("Non existent control name used in addComponentOrder To: " + sTo);
		if( iFrom != null && iTo != null ) {
			Component cFrom = aComponents.get( iFrom );
			Component cTo = aComponents.get( iTo );
			if( cFrom != cTo )
				hAltMap.put(cFrom, cTo);
		}
	}
	
	public void setAltFirstComponent( String sFirst ) {
		Integer iFirst =  hComponentIndexes.get(sFirst);
		if( iFirst == null )
			logger.error("Non existent control name used in setFirstComponent: " + sFirst);
		else {
			Component cFirst = aComponents.get( hComponentIndexes.get(sFirst) );
			this.cAltFirst = cFirst;
		}
	}
	
	public void selectMainMap() {
		hMap = hMainMap;
		cFirst = cMainFirst;
		props = mainProps;
	}
	
	public void selectAltMap() {
		hMap = hAltMap;
		cFirst = cAltFirst;
		props = altProps;
	}
	
	public String getProperty( String sKey ) {
		if( props == null ) return null;
		return props.getProperty(sKey);
	}
	
	public Component getComponentByName( String sName ) {
		if( sName == null || sName.trim().length() == 0 ) return null;
		int iIndex = hComponentIndexes.get(sName);
		return aComponents.get(iIndex);
	}
	
	/** 
	 * Explicitly get altProperty  ?Need explicitely get main property?
	 * @param sKey
	 * @return
	 */
	public String getAltProperty( String sKey ) {
		if( altProps == null && props == null ) return null;
		else if ( altProps == null ) return props.getProperty(sKey);
		else return altProps.getProperty(sKey);
	}
	
	/**
	 * Returns the Component that should receive the focus after aComponent.
	 */
	@Override
	public Component getComponentAfter(Container aContainer, Component aComponent) {
		Component cNext = hMap.get(aComponent);
		if( cNext != null )
			return cNext;
		else {
			int iIndex = aComponents.indexOf(aComponent);
			// Note: above returns -1 if not found so increments to zero or first component.  A logical failsafe.
			iIndex++;
			iIndex = iIndex % aComponents.size();
			cNext = aComponents.get(iIndex);
			while( !cNext.isVisible() || !cNext.isEnabled() ) {
				if( cNext == aComponent ) {
					logger.info("getComponentAfter() looped around to itself");
					return null;  // This should never happen means we looped.
				}
				iIndex++;
				iIndex = iIndex % aComponents.size();
				cNext = aComponents.get(iIndex);	
				if( cNext == aComponent )
					cNext = super.getComponentAfter(aContainer, aComponent);
			}
			return cNext;
		}
	}
	
	
	/**
	 * This version explicitly uses the alternate tab mapping
	 * Returns the Component that should receive the focus after aComponent.
	 */
	public Component getAltComponentAfter(Container aContainer, Component aComponent) {
		Component cNext = hAltMap.get(aComponent);
		if( cNext != null )
			return cNext;
		else {
			int iIndex = aComponents.indexOf(aComponent);
			// Note: above returns -1 if not found so increments to zero or first component.  A logical failsafe.
			iIndex++;
			iIndex = iIndex % aComponents.size();
			cNext = aComponents.get(iIndex);
			while( !cNext.isVisible() || !cNext.isEnabled() ) {
				if( cNext == aComponent ) {
					logger.info("getComponentAfter() looped around to itself");
					return null;  // This should never happen means we looped.
				}
				iIndex++;
				iIndex = iIndex % aComponents.size();
				cNext = aComponents.get(iIndex);				
				if( cNext == aComponent )
					cNext = super.getComponentAfter(aContainer, aComponent);
			}
			return cNext;
		}
	}

	
	/** 
	 * Returns the Component that should receive the focus before aComponent.
	 */
	@Override
	public Component getComponentBefore(Container aContainer, Component aComponent) {
		Component cNext = aComponent;
		int iIndex = aComponents.indexOf(aComponent);
		// Note: above returns -1 if not found.  Decrement is not a good solution
		if( iIndex < 0 ) 
			iIndex = 0;
		else if( iIndex == 0 ) 
			iIndex = aComponents.size() - 1;
		else 
			iIndex--;
		cNext = aComponents.get(iIndex);
		while( !cNext.isVisible() || !cNext.isEnabled() ) {
			if( cNext == aComponent ) {
				logger.info("getComponentBefore() looped around to itself");
				return null;  // This should never happen means we looped.
			}
			if( iIndex < 0 ) 
				iIndex = 0;
			else if( iIndex == 0 ) 
				iIndex = aComponents.size() - 1;
			else 
				iIndex--;
			cNext = aComponents.get(iIndex);
			if( cNext == aComponent )
				cNext = super.getComponentBefore(aContainer, aComponent);
		}
		return cNext;
	}
	
	/**
	 * Returns the default Component to focus.
	 */
	@Override
	public Component getDefaultComponent(Container aContainer) {
		return getFirstComponent( aContainer );
//		if( cFirst != null )
//			return cFirst;
//		else
//			return super.getDefaultComponent(aContainer);
	}
	
	/**
	 * Returns the first Component in the traversal cycle.
	 */
	@Override
	public Component getFirstComponent(Container aContainer) {
		if( cFirst != null )
			return cFirst;
		else
			return super.getFirstComponent(aContainer);
	}
	
	private List<Component> getAllComponents(final Container c) {
		Component[] comps = c.getComponents();
		List<Component> compList = new ArrayList<Component>();
		for (Component comp : comps) {
			compList.add(comp);
			if (comp instanceof Container) {
				compList.addAll(getAllComponents((Container) comp));
			}
		}
		return compList;
	}
	
	// Components don't know what we call them so I have to set up 
	// names somewhere to go with the config file entries.
	private void populateControlNameMap() {
		hComponentIndexes.put("ZoomPlusButton",0 );
		hComponentIndexes.put("ZoomMinusButton",1 );
		hComponentIndexes.put("RotateButton",2 );
		hComponentIndexes.put("ViewPageInAcrobatButton",3 );
		hComponentIndexes.put("ViewInAcrobatButton",4 );
		hComponentIndexes.put("PreviousFileButton",5 );
		hComponentIndexes.put("PreviousPageButton",6 );
		hComponentIndexes.put("NextPageButton",7 );
		hComponentIndexes.put("NextFileButton",8 );
		hComponentIndexes.put("GotoPageButton",9 );
		hComponentIndexes.put("AllValuesSticky",10 );
		hComponentIndexes.put("EditLastButton",11 );
		hComponentIndexes.put("Inbound",12 );
		hComponentIndexes.put("Outbound",13 );
		hComponentIndexes.put("InState",14 );
		hComponentIndexes.put("OtherState",15 );
		hComponentIndexes.put("OtherName",16 );
		hComponentIndexes.put("OtherAddress",17 );
		hComponentIndexes.put("OtherCity",18 );
		hComponentIndexes.put("OtherZipcode",19 );
		hComponentIndexes.put("OtherCounty",20 );
		hComponentIndexes.put("ThisPhone",21 );
		hComponentIndexes.put("ThisPIN",22 );
		hComponentIndexes.put("ThisName",23 );
		hComponentIndexes.put("ThisAddress",24 );
		hComponentIndexes.put("ThisCity",25 );
		hComponentIndexes.put("ThisZipcode",26 );
		hComponentIndexes.put("Species",27 );
		hComponentIndexes.put("Number",28 );
		hComponentIndexes.put("InspectedDate",29 );
		hComponentIndexes.put("ReceivedDate",30 );
		hComponentIndexes.put("CertificateNumber",31 );
		hComponentIndexes.put("ThisIssuedBy",32 );
		hComponentIndexes.put("OtherIssuedBy",33 );
		hComponentIndexes.put("ShowAllVets",34 );
		hComponentIndexes.put("Purpose",35 );
		hComponentIndexes.put("ErrorsButton",36 );
		hComponentIndexes.put("AddPageButton",37 );
		hComponentIndexes.put("AddAnimalIDsButton",38 );
		hComponentIndexes.put("SaveNextButton",39 );

	}
	
	private void loadFirstComponent( int iMap ) {
		if( iMap == PRIMARY_MAP ) {
			Component c = getComponentByName(mainProps.getProperty("pFirstControl"));
			cMainFirst = c;
		}
		else if( iMap == ALTERNATE_MAP ) {
			Component c = getComponentByName(altProps.getProperty("pFirstControl"));
			cAltFirst = c;
		}
		else {
			logger.error("Unknown tab order map: " + iMap);
			return;
		}
	}
	
	private void loadComponentOrderMap( int iMap ) {
		String sFile = null;
		Properties props = new Properties();
		if( iMap == PRIMARY_MAP ) {
			sFile = PRIMARY_FILE;
			mainProps = props;
		}
		else if( iMap == ALTERNATE_MAP ) {
			sFile = ALTERNATE_FILE;
			altProps = props;
		}
		else {
			logger.error("Unknown tab order map: " + iMap);
			return;
		}
		File file = new File( sFile );
		if( file.exists() && file.isFile() ) {
			FileInputStream input;
			try {
				input = new FileInputStream( file );
				props.load( input );
				input.close();
			} catch (FileNotFoundException e) {
				logger.error("File " + sFile + " not found in load after checking!", e);
				return;
			} catch (IOException e) {
				logger.error("Could not read file " + sFile);
				return;
			}
			for( String sFrom : props.stringPropertyNames() ) {
				if( sFrom.startsWith("p") )
					continue;
				String sTo = props.getProperty(sFrom);
				if( sTo != null && sTo.trim().length() > 0 ) {
					if( iMap == PRIMARY_MAP ) {
						addComponentOrder(sFrom, sTo.trim());
					}
					else if( iMap == ALTERNATE_MAP ) {
						addAltComponentOrder(sFrom, sTo.trim());
					}
				}
			}
		}
	}
	
}
