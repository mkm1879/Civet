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
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.log4j.Logger;

class AddOnLoader {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	
	private AddOnLoader() {
	}
	
	/**
	 * Create menu items in a given JMenu for each AddOn class in the edu.clemson.lph.civet.addons package
	 * @param wParent
	 * @param menuAddOns
	 */
	static void populateMenu( Window wParent, JMenu menuAddOns ) {
		ArrayList<Class<AddOn>> addOnClasses = listAddonClasses();
		final Window parent = wParent;
		menuAddOns.setEnabled(false);
		for( Class<AddOn> addOnClass : addOnClasses ) {
			try {
				final AddOn thisAddOn = addOnClass.getConstructor().newInstance();
				JMenuItem itemAddOn = new JMenuItem();
				itemAddOn.setText( thisAddOn.getMenuText() );
				itemAddOn.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						thisAddOn.execute(parent);
					}
				});
				menuAddOns.add(itemAddOn);
				menuAddOns.setEnabled(true);
			} catch (ReflectiveOperationException e) {
				logger.error("Unable to instantiate AddOn " + addOnClass.getName(), e);
			}
		}
	}
	  /**
	   * List all subclasses of AddOn in the package containing
	   * tray icons.
	   * @return ArrayList of Class objects
	   */
	  @SuppressWarnings("unchecked")
	private static ArrayList<Class<AddOn>> listAddonClasses() {
		  ArrayList<Class<AddOn>> addOnClasses = new ArrayList<Class<AddOn>>();
		  try {
			  String aAllClasses[] = getResourceListing( AddOnLoader.class, "edu/clemson/lph/civet/addons/" );
			  for( String sItem: aAllClasses ) {
				  if( sItem != null && sItem.trim().length() > 0 && !sItem.contains("$") ) {
					  int iIndexOfName = sItem.lastIndexOf('.');
					  if( iIndexOfName < 0 )
						  continue;
					  sItem = "edu.clemson.lph.civet.addons." + sItem.substring(0,iIndexOfName);
					  try {
						  @SuppressWarnings("rawtypes")
						  Class nextClass = Class.forName(sItem);
						  if( AddOn.class.isAssignableFrom(nextClass)
								  && !AddOn.class.equals(nextClass) ) {
							  addOnClasses.add(nextClass);
						  }
					  } catch (ClassNotFoundException e) {
						  logger.error( "Could not Find Class " + sItem, e);
					  }
				  }
			  }
		  } catch (URISyntaxException e) {
			  logger.error(e);;
		  } catch (IOException e) {
			  logger.error(e);
		  }
		  return addOnClasses;
	  }

		/**
	   * List directory contents for a resource folder. Not recursive.
	   * This is basically a brute-force implementation.
	   * Works for regular files and also JARs.
	   * 
	   * @author Greg Briggs
	   * @param clazz Any java class that lives in the same place as the resources you want.
	   * @param path Should end with "/", but not start with one.
	   * @return Just the name of each member item, not the full paths.
	   * @throws URISyntaxException 
	   * @throws IOException 
	   */
	  private static String[] getResourceListing(@SuppressWarnings("rawtypes") Class clazz, String path) throws URISyntaxException, IOException {
	      URL dirURL = clazz.getClassLoader().getResource(path);
	      if (dirURL != null && dirURL.getProtocol().equals("file")) {
	        /* A file path: easy enough */
	        return new File(dirURL.toURI()).list();
	      } 

	      if (dirURL == null) {
	        /* 
	         * In case of a jar file, we can't actually find a directory.
	         * Have to assume the same jar as clazz.
	         */
	        String me = clazz.getName().replace(".", "/")+".class";
	        dirURL = clazz.getClassLoader().getResource(me);
	      }
	      
	      if (dirURL.getProtocol().equals("jar")) {
	        /* A JAR path */
	        String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
	        JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
	        Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
	        Set<String> result = new HashSet<String>(); //avoid duplicates in case it is a subdirectory
	        while(entries.hasMoreElements()) {
	          String name = entries.nextElement().getName();
	          if (name.startsWith(path)) { //filter according to the path
	            String entry = name.substring(path.length());
	            int checkSubdir = entry.indexOf("/");
	            if (checkSubdir >= 0) {
	              // if it is a subdirectory, we just return the directory name
	              entry = entry.substring(0, checkSubdir);
	            }
	            result.add(entry);
	          }
	        }
	        jar.close();
	        return result.toArray(new String[result.size()]);
	      } 	        
	      throw new UnsupportedOperationException("Cannot list files for URL "+dirURL);
	  }
	  
}
