/*******************************************************************************
 * 
 * (C) Copyright IBM Corp. 2021, 2022. All rights reserved
 * SPDX-License-Identifier: MIT
 * Contributors:
 *     IBM Corporation - initial implementation
 *     
 *******************************************************************************/

package com.ibm.dev;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ibm.broker.config.appdev.*;

public class MigrateGenericHL7Nodes {

	public static void main(String[] args) {
		
		if (args.length == 0) {
			System.out.println("Error - no directory path was provided!");
			System.out.println("Please pass in a single argument providing the name of a directory path.");
			System.out.println("This directory and its descendant subdirectories will be searched for message flows containing the nodes GenericHL7Input or GenericHL7Output.");
		} else {
			if (args.length > 1) {
				System.out.println("Warning - more than one argument was provided. The first argument will be used and the others ignored!");
			}
			String projectDirectoryAbsolutePath = args[0];
			System.out.println("Aiming to seek message flows inside the following directory (and its descendant subdirectories): "+projectDirectoryAbsolutePath);			
			File dir = new File(projectDirectoryAbsolutePath);
			if (dir.isDirectory()) {
				analyseDirectoryContents(dir,projectDirectoryAbsolutePath);
			} else {
				System.out.println("Error - the provided argument could not be interpreted as a directory!");
			}
			System.out.println("Terminating - my work here is done!");
		}
	}
	
	public static void analyseDirectoryContents(File dir, String projectDirectoryAbsolutePath) {
		
		File[] files = dir.listFiles();
		for (File file : files) {
			//System.out.println("In loop and file is "+file.getAbsolutePath());
			if (file.isDirectory()) {
				//Descend deeper as .msgflows may be in subdirectories of the project
				analyseDirectoryContents(file, projectDirectoryAbsolutePath);
			} else {
				if (file.getName().endsWith((".msgflow"))) {
					dealWithMessageFlow(file, projectDirectoryAbsolutePath);
				}
			}
		}		
	}
	
	public static void dealWithMessageFlow(File msgflowFile, String projectDirectoryAbsolutePath) {
		
		try {

		Boolean inputNodeWasUpdated = false;
		Boolean outputNodeWasUpdated = false;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(msgflowFile);
		
		// This next section of code aims to find all instances of GenericHL7Input which are to be migrated to HL7DFDLInput
		
		// ORIGINALLY LOOKS LIKE THIS ...
		// <nodes xmi:type="hl7in_HL7Input.msgflow:FCMComposite_1"  xmi:id="FCMComposite_1_2" location="176,177" CheckDuplicates="false" DuplicateIdentifiersQueue="DUPID" ReportDuplicates="false" connectionDetails="localhost:1111">
		// <translation xmi:type="utility:ConstantString" string="HL7Input"/>
		// </nodes>
				
		// WILL BE CHANGED TO LOOK LIKE THIS ...
		// <nodes xmi:type="hl7dfdlin_HL7DFDLInput.subflow:FCMComposite_1" xmi:id="FCMComposite_1_2" location="176,177" CheckDuplicates="false" DuplicateIdentifiersQueue="DUPID" ReportDuplicates="false" connectionDetails="localhost:1111">
		// <translation xmi:type="utility:ConstantString" string="HL7Input"/>
		// </nodes>
		
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		xpath.setNamespaceContext(new NamespaceResolver(doc));;
		XPathExpression expr = xpath.compile("/ecore:EPackage/eClassifiers/composition/nodes[@xmi:type='hl7in_HL7Input.msgflow:FCMComposite_1']/@*");
		Object result = expr.evaluate(doc,  XPathConstants.NODESET);
		NodeList nodes = (NodeList) result;
		ArrayList<String> xmiids = new ArrayList<String>();
		// nodes will hold all the attributes of the old GenericHL7Input (MRM based) node
		// This code copies them over for now - in future we might want to edit the HL7DFDLInput to have different properties than the GenericHL7Input
		for (int i=0; i < nodes.getLength(); i++) {
			org.w3c.dom.Node node = nodes.item(i);
			if (node.getNodeName().equals("xmi:type")) {
				// This updates the type of the GenericHL7Input to be HL7DFDLInput
				node.setTextContent("hl7dfdlin_HL7DFDLInput.subflow:FCMComposite_1");
				inputNodeWasUpdated = true;
			}
			if (node.getNodeName().equals("xmi:id")) {
				// Record the xmi:id  in an ArrayList so that later we can find its terminal connections and update them
				xmiids.add(node.getNodeValue());
			}
		}
		
		// This next section of code aims to find all instances of GenericHL7Output which are to be migrated to HL7DFDLOutput
		
		// ORIGINALLY LOOKS LIKE THIS ...
		// <nodes xmi:type="hl7out_HL7Output.msgflow:FCMComposite_1" xmi:id="FCMComposite_1_10" location="176,177">
		// <translation xmi:type="utility:ConstantString" string="HL7Output"/>
		// </nodes>

		// WILL BE CHANGED TO LOOK LIKE THIS ...
		// <nodes xmi:type="hl7dfdlout_HL7DFDLOutput.subflow:FCMComposite_1" xmi:id="FCMComposite_1_10" location="176,177">
		// <translation xmi:type="utility:ConstantString" string="HL7Output"/>
		// </nodes>
		
		XPathExpression exprOut = xpath.compile("/ecore:EPackage/eClassifiers/composition/nodes[@xmi:type='hl7out_HL7Output.msgflow:FCMComposite_1']/@*");
		Object resultOut = exprOut.evaluate(doc, XPathConstants.NODESET);
		NodeList nodesOut = (NodeList) resultOut;
		ArrayList<String> xmiidsOut = new ArrayList<String>();
		// nodesOut will hold all the attributes of the old GenericHL7Output (MRM based) node
		// This code copies them over for now - in future we might want to edit the HL7DFDLOutput to have different properties than the GenericHL7Output
		for (int j=0; j < nodesOut.getLength(); j++) {
			org.w3c.dom.Node nodeOut = nodesOut.item(j);
			if (nodeOut.getNodeName().equals("xmi:type")) {
				// This updates the type of the GenericHL7Output to be HL7DFDLOutput
				nodeOut.setTextContent("hl7dfdlout_HL7DFDLOutput.subflow:FCMComposite_1");
				outputNodeWasUpdated = true;
			}
			if (nodeOut.getNodeName().equals("xmi:id")) {
				// Record the xmi:id  in an ArrayList so that later we can find its terminal connections and update them
				xmiidsOut.add(nodeOut.getNodeValue());
			}
		}
		
		if (inputNodeWasUpdated) {
			// We come through here if an instance of GenericHL7Input was located and changed to be HL7DFDLInput
			
			// The terminal connections of the GenericHL7Input are named differently to the HL7DFDLInput
			
			// AN EXAMPLE OF THE ORIGINAL LOOKS LIKE THIS ...
			// <connections xmi:type="eflow:FCMConnection" xmi:id="FCMConnection_3" targetNode="FCMComposite_1_1" sourceNode="FCMComposite_1_2" sourceTerminalName="OutTerminal.Output5" targetTerminalName="InTerminal.Input"/>
			// <connections xmi:type="eflow:FCMConnection" xmi:id="FCMConnection_4" targetNode="FCMComposite_1_1" sourceNode="FCMComposite_1_2" sourceTerminalName="OutTerminal.Output3" targetTerminalName="InTerminal.Input"/>
			// <connections xmi:type="eflow:FCMConnection" xmi:id="FCMConnection_5" targetNode="FCMComposite_1_3" sourceNode="FCMComposite_1_2" sourceTerminalName="OutTerminal.Output4" targetTerminalName="InTerminal.Input"/>
			
			// WHICH WOULD BE CHANGED TO LOOK LIKE THIS ...
			// <connections xmi:type="eflow:FCMConnection" xmi:id="FCMConnection_3" targetNode="FCMComposite_1_1" sourceNode="FCMComposite_1_2" sourceTerminalName="OutTerminal.Catch" targetTerminalName="InTerminal.Input"/>
			// <connections xmi:type="eflow:FCMConnection" xmi:id="FCMConnection_4" targetNode="FCMComposite_1_1" sourceNode="FCMComposite_1_2" sourceTerminalName="OutTerminal.Failure" targetTerminalName="InTerminal.Input"/>
			// <connections xmi:type="eflow:FCMConnection" xmi:id="FCMConnection_5" targetNode="FCMComposite_1_3" sourceNode="FCMComposite_1_2" sourceTerminalName="OutTerminal.Out" targetTerminalName="InTerminal.Input"/>
			
			// In other words, the terminal naming mapping from GenericHL7Input >> HL7DFDLInput is as follows
			// OutTerminal.Output3 >> OutTerminal.Failure
			// OutTerminal.Output4 >> OutTerminal.Out
			// OutTerminal.Output5 >> OutTerminal.Catch
			
			for (int k=0; k<xmiids.size(); k++) {
				String xmiidNodeIdentifier = xmiids.get(k);
				XPathExpression exprConnections = xpath.compile("/ecore:EPackage/eClassifiers/composition/connections[@sourceNode='"+xmiidNodeIdentifier+"']/@sourceTerminalName");
				NodeList connectionsSourceTerminals = (NodeList) exprConnections.evaluate(doc, XPathConstants.NODESET);
				// exprConnections will hold all the relevant <connections> entries which need to be updated 
				for (int l=0; l < connectionsSourceTerminals.getLength(); l++) {
					org.w3c.dom.Node connectionSourceTerminal = connectionsSourceTerminals.item(l);
					if (connectionSourceTerminal.getNodeValue().equals("OutTerminal.Output3")) {
						//System.out.println("Found the Failure terminal - resetting it!");
						connectionSourceTerminal.setTextContent("OutTerminal.Failure");
					}
					if (connectionSourceTerminal.getNodeValue().equals("OutTerminal.Output4")) {
						//System.out.println("Found the Out terminal - resetting it!");
						connectionSourceTerminal.setTextContent("OutTerminal.Out");
					}
					if (connectionSourceTerminal.getNodeValue().equals("OutTerminal.Output5")) {
						//System.out.println("Found the Catch terminal - resetting it!");
						connectionSourceTerminal.setTextContent("OutTerminal.Catch");
					}
				}				
			}
		}
		if (outputNodeWasUpdated) {
			// We come through here if an instance of GenericHL7Output was located and changed to be HL7DFDLOutput
			
			// The terminal connections (both input and output!) of the GenericHL7Output are named differently to the HL7DFDLOutput!
			
			// AN EXAMPLE OF THE ORIGINAL OUTPUT TERMINALS LOOKS LIKE THIS ...
			// <connections xmi:type="eflow:FCMConnection" xmi:id="FCMConnection_3" targetNode="FCMComposite_1_11" sourceNode="FCMComposite_1_10" sourceTerminalName="OutTerminal.Output" targetTerminalName="InTerminal.Input"/>
			// <connections xmi:type="eflow:FCMConnection" xmi:id="FCMConnection_4" targetNode="FCMComposite_1_12" sourceNode="FCMComposite_1_10" sourceTerminalName="OutTerminal.Output1" targetTerminalName="InTerminal.Input"/>
			// <connections xmi:type="eflow:FCMConnection" xmi:id="FCMConnection_5" targetNode="FCMComposite_1_13" sourceNode="FCMComposite_1_10" sourceTerminalName="OutTerminal.Output2" targetTerminalName="InTerminal.Input"/>
			// AN EXAMPLE OF THE ORIGINAL INPUT TERMINALS LOOKS LIKE THIS ...
			// <connections xmi:type="eflow:FCMConnection" xmi:id="FCMConnection_6" targetNode="FCMComposite_1_10" sourceNode="FCMComposite_1_9" sourceTerminalName="OutTerminal.Output" targetTerminalName="InTerminal.Input"/>			
			
			// WHICH WOULD BE CHANGED TO LOOK LIKE THIS FOR OUTPUT TERMINALS ...
			// <connections xmi:type="eflow:FCMConnection" xmi:id="FCMConnection_3" targetNode="FCMComposite_1_11" sourceNode="FCMComposite_1_10" sourceTerminalName="OutTerminal.Failure" targetTerminalName="InTerminal.Input"/>
			// <connections xmi:type="eflow:FCMConnection" xmi:id="FCMConnection_4" targetNode="FCMComposite_1_12" sourceNode="FCMComposite_1_10" sourceTerminalName="OutTerminal.Out" targetTerminalName="InTerminal.Input"/>
			// <connections xmi:type="eflow:FCMConnection" xmi:id="FCMConnection_5" targetNode="FCMComposite_1_13" sourceNode="FCMComposite_1_10" sourceTerminalName="OutTerminal.Log%20Retry" targetTerminalName="InTerminal.Input"/>
			// WHICH WOULD BE CHANGED TO LOOK LIKE THIS FOR INPUT TERMINAL ...
			// <connections xmi:type="eflow:FCMConnection" xmi:id="FCMConnection_6" targetNode="FCMComposite_1_10" sourceNode="FCMComposite_1_9" sourceTerminalName="OutTerminal.Output" targetTerminalName="InTerminal.In"/>
			
			// In other words, the terminal naming mapping from GenericHL7Output >> HL7DFDLOutput is as follows
			// OutTerminal.Output >> OutTerminal.Failure
			// OutTerminal.Output1 >> OutTerminal.Out
			// OutTerminal.Output2 >> OutTerminal.Log%20Retry
			// InTerminal.Input >> InTerminal.In		
		
			for (int m=0; m<xmiidsOut.size(); m++) {
				String xmiidNodeIdentifierOut = xmiidsOut.get(m);
				XPathExpression exprConnectionsOut = xpath.compile("/ecore:EPackage/eClassifiers/composition/connections[@sourceNode='"+xmiidNodeIdentifierOut+"']/@sourceTerminalName");
				NodeList connectionsSourceTerminalsOut = (NodeList) exprConnectionsOut.evaluate(doc, XPathConstants.NODESET);
				// ConnectionsSourceTerminalsOut will hold all the relevant <connections> entries which need to be updated 
				for (int n=0; n < connectionsSourceTerminalsOut.getLength(); n++) {
					org.w3c.dom.Node connectionSourceTerminalOut = connectionsSourceTerminalsOut.item(n);
					if (connectionSourceTerminalOut.getNodeValue().equals("OutTerminal.Output")) {
						//System.out.println("Found the Failure terminal - resetting it!");
						connectionSourceTerminalOut.setTextContent("OutTerminal.Failure");
					}
					if (connectionSourceTerminalOut.getNodeValue().equals("OutTerminal.Output1")) {
						//System.out.println("Found the Out terminal - resetting it!");
						connectionSourceTerminalOut.setTextContent("OutTerminal.Out");
					}
					if (connectionSourceTerminalOut.getNodeValue().equals("OutTerminal.Output2")) {
						//System.out.println("Found the Log Retry terminal - resetting it!");
						connectionSourceTerminalOut.setTextContent("OutTerminal.Log%20Retry");
					}
				}				
				// Repeat the same trick but for the input terminal of the output node ...
				XPathExpression exprConnectionsIn = xpath.compile("/ecore:EPackage/eClassifiers/composition/connections[@targetNode='"+xmiidNodeIdentifierOut+"']/@targetTerminalName");
				NodeList connectionsSourceTerminalsIn = (NodeList) exprConnectionsIn.evaluate(doc, XPathConstants.NODESET);
				for (int o=0; o<connectionsSourceTerminalsIn.getLength(); o++) {
					org.w3c.dom.Node connectionSourceTerminalIn = connectionsSourceTerminalsIn.item(o);
					if (connectionSourceTerminalIn.getNodeValue().equals("InTerminal.Input")) {
						//System.out.println("Found the Input terminal - resetting it!");
						connectionSourceTerminalIn.setTextContent("InTerminal.In");
					}
				}				
			}
		}
		if (inputNodeWasUpdated || outputNodeWasUpdated) {
			// Before writing the amended flow to disk, we must also deal with updating the namespace prefix/pair declaration
		
			// xmlns:hl7in_HL7Input.msgflow="hl7in/HL7Input.msgflow"
			// NEEDS TO BE CHANGED TO ...
			// xmlns:hl7dfdlin_HL7DFDLInput.subflow="hl7dfdlin/HL7DFDLInput.subflow"
			
			NodeList rootTag = doc.getElementsByTagNameNS("*", "EPackage");
			org.w3c.dom.Node root = rootTag.item(0);
			Element rootElement = (Element) root;			
			if (inputNodeWasUpdated) {				
				// The input node was updated so now we need to change the attribute at the top of the flow xml
				if (rootElement.hasAttributeNS("http://www.w3.org/2000/xmlns/","hl7dfdlin_HL7DFDLInput.subflow")) {					
					// The attribute has already been added, so don't need to add it again.
					// This could protect us in future hypothetical circumstances where code changes cause inputNodeWasUpdated to be true
					// but where this migrate code has already been run against this flow already, and hence the new attribute already been added!
				} else {
					// Add the new attribute
					rootElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:hl7dfdlin_HL7DFDLInput.subflow",  "hl7dfdlin/HL7DFDLInput.subflow");
					if (rootElement.hasAttributeNS("http://www.w3.org/2000/xmlns/","hl7in_HL7Input.msgflow") ) {
						// If the new attribute added then we should always come in here and delete the old attribute too!
						rootElement.removeAttributeNS("http://www.w3.org/2000/xmlns/","hl7in_HL7Input.msgflow");
					}
				}
			}
			if (outputNodeWasUpdated) {
				// The output node was updated so now we need to change the attribute at the top of the flow xml
				if (rootElement.hasAttributeNS("http://www.w3.org/2000/xmlns/","hl7dfdlout_HL7DFDLOutput.subflow")) {					
					// The attribute has already been added, so we don't need to add it again.
					// This could protect us in future hypothetical circumstances where code changes cause outputNodeWasUpdated to be true
					// but where this migrate code has already been run against this flow already, and hence the new attribute already been added!
				} else {
					// Add the new attribute
					rootElement.setAttributeNS("http://www.w3.org/2000/xmlns/","xmlns:hl7dfdlout_HL7DFDLOutput.subflow",  "hl7dfdlout/HL7DFDLOutput.subflow");
					if (rootElement.hasAttributeNS("http://www.w3.org/2000/xmlns/","hl7out_HL7Output.msgflow") ) {
						// If the new attribute is added then we should always come in here and delete the old attribute too!
						rootElement.removeAttributeNS("http://www.w3.org/2000/xmlns/","hl7out_HL7Output.msgflow");
					}
				}			
			}
			System.out.println("==> Flow Report Flow Details BEFORE Migrating ...");
			FlowReport(msgflowFile);
			System.out.println("Writing the migrated flow to "+msgflowFile.getAbsoluteFile());
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.transform(new DOMSource(doc),  new StreamResult(msgflowFile));
			System.out.println("==> Flow Report Flow Details AFTER Migrating ...");
			FlowReport(msgflowFile);
		} else {
			System.out.println("We didn't find any GenericHL7Input or GenericHL7Output nodes so message flow "+msgflowFile.getName()+" does not need updating!");
		}
		
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
	}
	
	public static void FlowReport (File msgflowFile) {
		
		try {
			MessageFlow mf1;
			mf1 = FlowRendererMSGFLOW.read(msgflowFile);
			System.out.println("==> FlowReport: About to analyse message flow named "+mf1.getName());
			// List out Flow Properties
			Vector<FlowProperty> flowProperties = mf1.getFlowProperties();
			for (int p=0; p < flowProperties.size(); p++) {
				System.out.println("==> FlowReport: There is a flow property named "+flowProperties.get(p).getName());
			}
			// List out the nodes in the flow
			List<Node> msgflowNodes = new ArrayList<Node>();
			msgflowNodes = mf1.getNodes();
			for (Node msgflowNode : msgflowNodes) {
				System.out.println("==> FlowReport: There is a message flow node named "+msgflowNode.getNodeName()+" which has type "+msgflowNode.getTypeName());
				// List out the node properties
				Enumeration<String> nodeProperties = msgflowNode.getPropertyNames();
				while (nodeProperties.hasMoreElements()) {
					String nodeProperty = nodeProperties.nextElement();
					System.out.println("==> FlowReport: Property named "+nodeProperty+" has value "+msgflowNode.getPropertyValue(nodeProperty));					
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}			
	}	
}
	
