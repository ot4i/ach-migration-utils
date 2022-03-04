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

## Can you please give me a step-by-step example of how to use the code in this repo?
Yes! After cloning this repository, locate the project interchange file named PI_IIBv10ExampleWithGenericHL7Nodes.zip 
As its name suggests, this project interchange zip file contains some example message flows (located inside an Integration Project) which use the GenericHL7Input node and GenericHL7Output node. In this worked example we will use the code in this repo to locate and convert these message flow nodes into an HL7DFDLInput node and HL7DFDLOutput node. These instructions assume you have already installed App Connect Enterprise 12.0.3.0, App Connect for Healthcare 6.0.1.0 and linked the installations in the normal way using the ACH_CreateLink.bat script. 

1. Launch your App Connect Enterprise 12.0.3.0 Toolkit associated with an existing workspace or create a new one such as `C:\workspace1`, and use the File > Import menu option and in the resulting dialog, choose the Project Interchange option from the IBM Integration folder. Hit Next and use the Browse button for the `From zip file` property, to select `PI_IIBv10ExampleWithGenericHL7Nodes.zip`

2. Select all three available projects:

* `PatternInstanceGeneratedWithIIBv100011` (this is the pattern instance project, which you may be interested in, but which is not needed to demonstrate the migration tool itself)
* `PatternInstanceGeneratedWithIIBv100011_HL7toHL7` (this is the main Integration Project, which holds the message flows)
* `PatternInstanceGeneratedWithIIBv100011_Hl7toHL7_Subflows` (this is another Integration Project which holds some subflow dependencies of the main Integration project mentioned above)

The projects will be imported. Once imported, `PatternInstanceGeneratedWithIIBv100011_HL7toHL7` will show some Errors in the Problems view.

Open `PatternInstanceGeneratedWithIIBv100011Receiver.msgflow` and it should look like this (note that the Toolkit shows errors for the GenericHL7Input):

![alt text](https://github.com/ot4i/ach-migration-utils/blob/main/ExampleMessageFlowContainingGenericHL7Input.png?raw=true)

Open `PatternInstanceGeneratedWithIIBv100011Sender.msgflow` and it should look like this (note that the Toolkit shows errors for the GenericHL7Output):

PICTURE HERE

3. Close the Toolkit and then launch a new, second App Connect Enterprise 12.0.3.0 Toolkit session using a different workspace, such as `C:\workspace2`. This workspace will be used to hold the Java migration tool from this git repository. If you wish to use the same workspace as the previous step you can do so, but these instructions assume a second workspace so that we can close the Eclipse Toolkit with the first workspace, whilst running the migration tool against the message flow artifacts. This avoids any potential confusion over stale relationships / required refreshes between Eclipse and the filesystem as the messageflow is updated.  Import the Java Project named `MigrateGenericHL7Nodes`.

4. Navigate into the project `MigrateGenericHL7Nodes`, expanding its subfolders to locate `MigrateGenericHL7Nodes.java` Right-click this java file and select Run As > Run Configurations. Select the Java application option and click the icon to create a New Launch Configuration. On the Arguments tab, add a single new Program Argument which is the folder on disk where the HL7toHL7 integration project was located such as `C:\workspace1\PatternInstanceGeneratedWithIIBv100011_HL7toHL7` and then run the java by hitting the Run button.

5. The java program should run and write some messages to the Console view in Toolkit. The project in question has two message flows which should cause action to be taken:
* `PatternInstanceGeneratedWithIIBv100011Receiver.msgflow` contains one instance of a GenericHL7Input node
* `PatternInstanceGeneratedWithIIBv100011Sender.msgflow` contains one instance of a GenericHL7Output node
The console output should look like this:

6. If you were to run the program a second time (and so the updates have already been made) you would see console output like this:

```
Aiming to seek message flows inside the following directory (and its descendant subdirectories): C:\workspace1\PatternInstanceGeneratedWithIIBv100011_HL7toHL7
We didn't find any GenericHL7Input or GenericHL7Output nodes so message flow Dest1Filter.msgflow does not need updating!
We didn't find any GenericHL7Input or GenericHL7Output nodes so message flow PatternInstanceGeneratedWithIIBv100011Receiver.msgflow does not need updating!
We didn't find any GenericHL7Input or GenericHL7Output nodes so message flow PatternInstanceGeneratedWithIIBv100011TransformAndRoute1.msgflow does not need updating!
We didn't find any GenericHL7Input or GenericHL7Output nodes so message flow SubJournal.msgflow does not need updating!
We didn't find any GenericHL7Input or GenericHL7Output nodes so message flow SubReceiverExceptionHandler.msgflow does not need updating!
We didn't find any GenericHL7Input or GenericHL7Output nodes so message flow SubTransformAndRouteExceptionHandler.msgflow does not need updating!
We didn't find any GenericHL7Input or GenericHL7Output nodes so message flow PatternInstanceGeneratedWithIIBv100011Dest1Sender.msgflow does not need updating!
We didn't find any GenericHL7Input or GenericHL7Output nodes so message flow SubSenderExceptionHandler.msgflow does not need updating!
Terminating - my work here is done!
```

7. Having run the tool, launch your App Connect Enterprise 12.0.3.0 Toolkit with `C:\workspace1`, and open up the two message flows which have been migrated. 

`PatternInstanceGeneratedWithIIBv100011Receiver.msgflow` should look like this:


`PatternInstanceGeneratedWithIIBv100011Dest1Sender.msgflow` should look like this:


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
