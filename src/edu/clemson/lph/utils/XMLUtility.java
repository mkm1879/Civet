package edu.clemson.lph.utils;
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
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import edu.clemson.lph.civet.Civet;

public class XMLUtility {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	private static final String sXmlDateFmt = "yyyy-MM-dd";

	public static Document stringToDom( String sXML ) throws ParserConfigurationException, SAXException, IOException	{
		if( sXML == null || sXML.trim().length() == 0 ) return null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		StringReader reader =  new StringReader( sXML );
		InputSource source = new InputSource( reader );
		return builder.parse( source );
	}
	
	public static String domToString( Document dom ) {
		return domToString( dom, false, null);
	}
	
	public static String domToString( Document dom, boolean bOmitDeclaration) {
		return domToString(dom, bOmitDeclaration, null );
	}
		
	public static String domToString( Document dom , boolean bOmitDeclaration, String sEncoding ) {
		String sOmit = (bOmitDeclaration ? "yes" : "no");
		try {
		TransformerFactory transFactory = TransformerFactory.newInstance();
		Transformer transformer = transFactory.newTransformer();
		if( sEncoding != null )
			transformer.setOutputProperty(OutputKeys.ENCODING, sEncoding);
		StringWriter buffer = new StringWriter();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, sOmit);
			transformer.transform(new DOMSource(dom),
			      new StreamResult(buffer));
			return buffer.toString();
		} catch (TransformerException e) {
			logger.error("Transform failure", e);
		}
		return null;
	}

	public static String printElements( Document dom ) {
		if( dom == null ) return null;
		StringBuffer sb = new StringBuffer();
		walkNodes( dom, sb, "");
		return sb.toString();
	}
	
	public static void walkNodes( Node nodeIn, StringBuffer sb, String sPad ) {
		if( nodeIn == null ) return;
		NamedNodeMap map = nodeIn.getAttributes();
		if( map != null )
			for( int i = 0; i < map.getLength(); i++ ) {
				Node n = map.item(i);
				if( n.getNodeType() == Node.ATTRIBUTE_NODE ) {
					sb.append(sPad + "   Attribute:" + n.getNodeName() + " = " + n.getNodeValue() + '\n' );
				}
			}
		NodeList nodes = nodeIn.getChildNodes();
		for( int i = 0; i < nodes.getLength(); i++ ) {
			Node n = nodes.item(i);
			if( n.getNodeType() == Node.ELEMENT_NODE ) {
				sb.append(sPad + "Element: " + n.getNodeName() + '\n');
			}
			if( n.getNodeType() == Node.TEXT_NODE ) {
				sb.append(sPad + "   Value: = " + n.getNodeValue() + '\n' );
			}
			if( n.getNodeType() == Node.ELEMENT_NODE ) {
				walkNodes( n, sb, sPad + "   " );
			}
		}
	}
	
	public static Set<String> getUniqueElementNames( Document dom ) {
		HashSet<String> hNames = new HashSet<String>();
		Node docNode = dom.getDocumentElement();
		walkNodes( docNode, hNames );
		return hNames;
	}
	
	private static void walkNodes( Node nodeIn, HashSet<String> hElements ) {
		if( nodeIn == null ) return;
		NodeList nodes = nodeIn.getChildNodes();
		for( int i = 0; i < nodes.getLength(); i++ ) {
			Node n = nodes.item(i);
			if( n.getNodeType() == Node.ELEMENT_NODE ) {
				String sNodeName = n.getNodeName();
				if( !hElements.contains(sNodeName) )
					hElements.add(sNodeName);
				walkNodes( n, hElements );
			}
		}
	}
		
	public static Element findFirstChildElementByName( Node node, String sName ) {
		if( node == null || sName == null ) return null;
		if( node.getNodeType() == Node.ELEMENT_NODE ) {
			if( sName.equals(node.getNodeName() ) ) {
				return (Element)node;
			}
			else {
				NodeList nodes = node.getChildNodes();
				for( int i = 0; i < nodes.getLength(); i++ ) {
					Node n = nodes.item(i);
					if( n.getNodeType() == Node.ELEMENT_NODE ) {
						if( sName.equals(n.getNodeName() ) ) {
							return (Element)n;
						}
						else {
							Element nextNode = findFirstChildElementByName( n, sName );
							if( nextNode != null )
								return nextNode;
						}
					}
				}
			}
		}
		// Should only reach here if a non-Element node is passed in
		return null;
	}
	
	/**
	 * List all child nodes with name sName
	 * NOTE: we assume no same name nodes are nested.
	 * @param node
	 * @param sName
	 * @return Element
	 */
	public static ArrayList<Element> listChildElementsByName( Node node, String sName ) {
		ArrayList<Element> aNodes = new ArrayList<Element>();
		NodeList nl = node.getChildNodes();
		for( int i = 0; i < nl.getLength(); i++ ) {
			Node n = nl.item(i);
			if( n.getNodeType() == Node.ELEMENT_NODE ) {
				if( sName.equals(n.getNodeName() ) ) {
					aNodes.add((Element)n);
				}
				else {
					ArrayList<Element> nextNodes = listChildElementsByName( n, sName );
					if( nextNodes != null )
						aNodes.addAll(nextNodes);
				}
			}
			// Don't search anything but elements
		}
		return aNodes;
	}
	
	public static String dateToXmlDate( java.util.Date dIn ) {
		final SimpleDateFormat xmlDateFmt = new SimpleDateFormat( sXmlDateFmt );
		if( dIn == null ) return null;
		return xmlDateFmt.format(dIn);
	}
	
	public static java.util.Date xmlDateToDate( String sDate ) {
		final SimpleDateFormat xmlDateFmt = new SimpleDateFormat( sXmlDateFmt );
		java.util.Date dRet = null;
		if( sDate == null ) return null;
		try {
			dRet = xmlDateFmt.parse(sDate);
		} catch (ParseException e) {
			logger.error("Invalid date format " + sDate, e);
		}
		return dRet;
	}

	
	public static String getXMLString(Document dom, boolean bOmitDeclaration) {
		return getXMLString(dom, bOmitDeclaration, null );
	}
		
	public static String getXMLString(Document dom, boolean bOmitDeclaration, String sEncoding ) {
		String sOmit = (bOmitDeclaration ? "yes" : "no");
		try {
		TransformerFactory transFactory = TransformerFactory.newInstance();
		Transformer transformer = transFactory.newTransformer();
		if( sEncoding != null )
			transformer.setOutputProperty(OutputKeys.ENCODING, sEncoding);
		StringWriter buffer = new StringWriter();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, sOmit);
			transformer.transform(new DOMSource(dom),
			      new StreamResult(buffer));
			return buffer.toString();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return null;
	}

}

