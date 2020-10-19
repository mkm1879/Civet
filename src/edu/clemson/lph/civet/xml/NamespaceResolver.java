package edu.clemson.lph.civet.xml;

import java.util.Iterator;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import org.w3c.dom.Document;

public class NamespaceResolver implements NamespaceContext 
{
    //Store the source document to search the namespaces
    private Document sourceDocument;
 
    public NamespaceResolver(Document document) {
        sourceDocument = document;
    }
 
    //The lookup for the namespace uris is delegated to the stored document.
    public String getNamespaceURI(String prefix) {
    	if( prefix == null || prefix.equals("ns0") ) {
    		return "http://www.usaha.org/xmlns/ecvi2";
    	}
    	else if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
            return "http://www.usaha.org/xmlns/ecvi2";
        } else {
            return sourceDocument.lookupNamespaceURI(prefix);
        }
    }
 
    public String getPrefix(String namespaceURI) {
        return sourceDocument.lookupPrefix(namespaceURI);
    }
 
    @SuppressWarnings("rawtypes")
    public Iterator getPrefixes(String namespaceURI) {
        return null;
    }
}
