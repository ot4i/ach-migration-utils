# ach-migration-utils
This repository provides Java for migrating message flow files containing the GenericHL7Input and GenericHL7Output message flow nodes.

## Support statement
This code is provided in good faith and AS-IS. There is no warranty or further service implied or committed and any supplied sample code is not supported via IBM product service channels.  You may submit a question in the issues, but a response is not guaranteed.

## What is IBM App Connect for Healthcare?
IBM App Connect for Healthcare 6.0.1.0 builds on IBM App Connect Enterprise to support applications in healthcare environments. IBM App Connect for Healthcare 6.0.1.0 extends IBM App Connect Enterprise 12.0.3.0 and later fix packs, and provides the following features:

* A message model that you use to parse, route, and transform HL7® messages in a message flow
* Healthcare-specific patterns that you can use to generate solutions for connecting medical applications
* Schemas for Fast Health Interoperability Resources (FHIR®) R4 XML and JSON resources
* Patterns that validate and convert FHIR R4 resources, create programs that generate FHIR R4 resources, and map selected HL7 messages to FHIR R4 resources
* Integration with DICOM Picture Archiving Communication Systems (PACS) and DICOM modalities, so that you can locate, process, and transfer DICOM images by using message flows
* Generation of ATNA audit events to support patient information confidentiality, data integrity, and user accountability
* The capability to extract information from healthcare data in message flows and to send the information to data warehouses for analysis
* Conversion of HIPAA files to XML files by using the Healthcare: HIPAA to XML pattern
* Patterns to help you create integration solutions that conform to Integrating the Healthcare Enterprise (IHE) PIX, PDQ, and XDS integration profiles

## What versions of IBM App Connect for Healthcare are there, and which versions of App Connect Enterprise do they work with?
Historically, there have been several available versions of IBM App Connect for Healthcare (and previous deliverables of the same technology but under different names):
* IBM App Connect for Healthcare 6.0.1.0 (released Dec 2021, EoS not yet declared) extends IBM App Connect Enterprise 12.0.3.0 and later fix packs
* IBM App Connect for Healthcare 5.0.0.0 (released Mar 2020, EoS not yet declared) extends IBM App Connect Enterprise 11.0.0.8 and later fix packs
* IBM Integration Bus Healthcare Pack 4.0.0.0 (released Jun 2015, EoS Sep 2022) extends IBM Integration Bus 10
* IBM Integration Bus Healthcare Pack 3.0 (released Mar 2014, EoS Apr 2020) extends IBM Integration Bus 9
* IBM WebSphere Message Broker Connectivity Pack for Healthcare version 8.0 (released Mar 2013, EoS Apr 2017) extends IBM WebSphere Message Broker 8
* IBM WebSphere Message Broker Connectivity Pack for Healthcare version 7.0 (released May 2011, EoS Apr 2017) extends IBM WebSphere Message Broker 7

## How do I migrate from previous versions of IBM App Connect for Healthcare?
Each version of the product provides its own specific migration advice and tools. For the latest version, App Connect for Healthcare 6.0.1.0, to use resources from previous versions you can use the migration wizard to convert them to IBM App Connect Enterprise 12.0 resources. The migration wizard converts your projects to use the new-style subflow user-defined nodes (introduced in IBM App Connect Enterprise 12.0.2.0) so that you can take advantage of the testing and debugging tools that are available in IBM App Connect Enterprise 12.0. The migration wizard also converts IBM Integration Bus 10.0 integration projects to applications and libraries so that you can deploy your solutions separately, taking advantage of the ability to isolate applications within an integration server. If you do not convert an integration project to an application, the integration project resources are grouped into the default application when you deploy your BAR file. Therefore, all resources in the same application are started, stopped, and deployed at the same time and share a scope in the server. To share artifacts between multiple applications, you can put those artifacts in shared libraries so that multiple applications can use the shared libraries.

## Given that IBM App Connect for Healthcare provides a migration tool, what additional value does the code in this repository provide?
A small proportion of users of IBM Integration Bus Healthcare Pack 4.0.0.0 (or in fact previous versions of the product which are now out of support) may have message flows which utilise the parsing technology known as MRM Message Sets. This parsing capability pre-dates the recommended and future-strategic DFDL message models. All users are recommended to move away from MRM Message Sets and adopt DFDL Message models when convenient. The DFDL technology performs faster at runtime, and has better build-time tooling which enables you to independently unit test your message models in the App Connect Enterprise Toolkit prior to deploying them to a runtime environment. Users of the HL7 MRM Message Sets (which were provided with v4) might well also have created message flows containing the GenericHL7Input and GenericHL7Output message flow nodes. The purpose of the Java code provided in this repository is to search through a top level directory and its sub-directories for message flow files which contain the GenericHL7Input and GenericHL7Output and replace them with their modern equivalents - the HL7DFDLInput node and HL7DFDLOutput node respectively.

## License
This code is provided under the terms of the MIT License:

MIT License

Copyright (c) 2022 Open Technologies for Integration

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
