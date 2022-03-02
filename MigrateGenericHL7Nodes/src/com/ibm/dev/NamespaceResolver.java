/*******************************************************************************
 * 
 * (C) Copyright IBM Corp. 2021, 2022. All rights reserved
 * SPDX-License-Identifier: MIT
 * Contributors:
 *     IBM Corporation - initial implementation
 *     
 *******************************************************************************/

package com.ibm.dev;

import java.util.Iterator;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import org.w3c.dom.Document;

public class NamespaceResolver implements NamespaceContext {
	
	private Document sourceDoc;
	
	public NamespaceResolver(Document doc) {
		sourceDoc = doc;
	}
	
	@Override
	public String getNamespaceURI(String prefix) {
		if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
			return sourceDoc.lookupNamespaceURI(null);
		} else {
			return sourceDoc.lookupNamespaceURI(prefix);
		}
	}
	
	@Override
	public String getPrefix(String namespaceURI) {
		return sourceDoc.lookupPrefix(namespaceURI);
	}
	
	@SuppressWarnings("rawtypes")
	public Iterator getPrefixes(String arg0) {
		return null;
	}
	
}
