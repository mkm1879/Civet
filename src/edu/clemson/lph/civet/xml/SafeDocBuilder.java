/**
 * Copyright Sep 12, 2018 Michael K Martin
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.clemson.lph.civet.xml;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;
import edu.clemson.lph.civet.Civet;

/**
 * 
 */
public class SafeDocBuilder {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	private static DocumentBuilderFactory factory = null;
	
	public static DocumentBuilder getSafeDocBuilder() {
		DocumentBuilder builder = null;
		if( factory == null )
			factory = DocumentBuilderFactory.newInstance();
	    String FEATURE = null;
	     try {
	       // This is the PRIMARY defense. If DTDs (doctypes) are disallowed, almost all XML entity attacks are prevented
	       // Xerces 2 only - http://xerces.apache.org/xerces2-j/features.html#disallow-doctype-decl
	       FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";
	       factory.setFeature(FEATURE, true);
	 
	       // If you can't completely disable DTDs, then at least do the following:
	       // Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-general-entities
	       // Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-general-entities
	       // JDK7+ - http://xml.org/sax/features/external-general-entities    
	       FEATURE = "http://xml.org/sax/features/external-general-entities";
	       factory.setFeature(FEATURE, false);
	 
	       // Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-parameter-entities
	       // Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-parameter-entities
	       // JDK7+ - http://xml.org/sax/features/external-parameter-entities    
	       FEATURE = "http://xml.org/sax/features/external-parameter-entities";
	       factory.setFeature(FEATURE, false);
	 
	       // Disable external DTDs as well
	       FEATURE = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
	       factory.setFeature(FEATURE, false);
	 
	       // and these as well, per Timothy Morgan's 2014 paper: "XML Schema, DTD, and Entity Attacks"
	       factory.setXIncludeAware(false);
	       factory.setExpandEntityReferences(false);
	       builder = factory.newDocumentBuilder();
	       } catch (ParserConfigurationException e) {
	             // This should catch a failed setFeature feature
	             logger.error("ParserConfigurationException was thrown. The feature '" +
	                 FEATURE + "' is probably not supported by your XML processor.",e);
	       }
		return builder;
	}
}
