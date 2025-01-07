## Overview

This project experiments with using both FHIR R4 and R4B within a single application class. The goal is to determine the feasibility and effort required to program both R4 and R4B simultaneously using HAPI FHIR.

## Objective: 
The primary goal of this task was to create a Java class that performs the following functions:
- Create a Java FHIR object: Construct an R4B-based object containing both a SubscriptionTopic resource and a R4 Patient resource, then serialize this object to an XML file.
- Load and reconstruct the object from XML: Read the XML file created above, parse it, and reconstruct the original Java object. The solution should also detect and handle scenarios where both R4B and R4 elements coexist within the XML.
This task aimed to evaluate how efficiently HAPI FHIR SDK can handle R4 and R4B simultaneously.

## Solution:
1. Implementation of Custom Resource Handler:

	CustomR4BResourceHandler class encapsulates the logic for serializing and deserializing the combined R4B SubscriptionTopic and R4 Patient resources. 	The class includes:
	- writeResourceToXml: writes the combined resource to an XML file.
	- readResourceFromXml: reads the XML file and reconstructs the original object.
	- createSampleResource: generates sample data.
  
2. Custom Resource Creation:

	CustomR4BResource class represents the combined resource structure. It encapsulates both SubscriptionTopic and Patient resources in a single object. 
3. R4 and R4B Compatibility Handling:
   
	Implemented detection logic within the XML parsing method to validate the presence of R4B (SubscriptionTopic) and R4 (Patient) elements in the XML content.
5. Serialization and deserialization
	
 	Used HAPI FHIR's parsers (IParser) to handle serialization and deserialization for both R4 and R4B resources.

## Challenges Encountered 
1. Handling Mixed R4 and R4B Resources:
	
 	Detecting and differentiating between R4 and R4B in the XML file required custom validation logic because the standard parsers do not inherently identify mixed profiles.



## Limitation
1. The solution assumes that the XML content will always include both SubscriptionTopic and Patient resources. If additional R4 or R4B resource types are introduced, the current implementation would need to be updated to support them.


## Exploration of Alternative Approaches Mixing R4 and R4B Resources

1. SubscriptionWithPatientExtensionHandler - Embed R4 Patient as an extension of a SubscriptionTopic resource

	In this approach, the Patient resource is serialized into an XML string and embedded as an extension of the SubscriptionTopic resource. A custom URL is used to identify the embedded resource. The resulting XML is saved and parsed later using FHIR R4B. The parsing process involves extracting the extension and converting it back to a `Patient` resource.

	Generated xml:
	```
	<SubscriptionTopic xmlns="http://hl7.org/fhir">
	   <id value="0403"/>
	   <extension url="http://hl7.org/fhir/StructureDefinition/embedded-patient">
	      <valueString value="&lt;Patient xmlns=&quot;http://hl7.org/fhir&quot;>&lt;id value=&quot;0403&quot;/>&lt;name>&lt;family value=&quot;Doe&quot;/>&lt;given value=&quot;John&quot;/>&lt;/name>&lt;/Patient>"/>
	   </extension>
	   <title value="Sample Subscription Topic"/>
	</SubscriptionTopic>
	```
 	This approach demonstrates the feasibility of embedding resources but added deserialization challenges.

2. ResourceBundleHandler - Extract R4 Patient and SubscriptionTopic from one xml file  
   
	This approach uses a container tag <Resource> to store both `Patient` and `SubscriptionTopic` within
a single xml file. Each resource is serialized into the container and can be parsed separately after deserialization.

	Generated xml: 
	```
	<Resources>
	<SubscriptionTopic xmlns="http://hl7.org/fhir"><title value="Sample Subscription Topic"/></SubscriptionTopic>
	<Patient xmlns="http://hl7.org/fhir"><id value="0403"/><name><family value="Doe"/><given value="John"/></name></Patient>
	</Resources>
	```
	The goal of this approach was to evaluate the complexity of managing Patient and SubscriptionTopic resources separately, without introducing additional XML structures or relationships between them. 
 
## Ease of Implementation 
1. Creating and Writing Resources to XML: 

	Straightforward using HAPI FHIR's encodeResourceToString method. The SDK provides intuitive methods to convert Java objects into XML. 

2. Reading and Parsing Resources from XML: 

	Slightly more complex due to the need to validate the presence of specific resource types. However, the task was manageable using HAPI FHIR's parseResource method and additional validation logic. 

3. Handling Mixed Resource Profiles: 

	More challenging since HAPI FHIR does not natively support combining R4 and R4B resources. Designing a custom solution added complexity but was necessary to meet the objective. 

## Key findings 

1. Direct operations are not supported

   Operations like `subscriptionTopic.addContained(r4Patient)` are not supported due to compatibility issues. However, something like the following works:
	```
	String patientXml = r4Parser.encodeResourceToString(r4Patient);

	subscriptionTopic.addExtension("http://hl7.org/fhir/StructureDefinition/embedded-patient",
			new org.hl7.fhir.r4b.model.StringType(patientXml)); 
	```
	This allows encapsulating R4 Data (`Patient` in this case) inside the `SubscriptionTopic` without violating FHIR’s data model. The idea is that `SubscriptionTopic` might be used in contexts where Patient resource is included as part of the subscription's metadata, so embedding it as an extension makes it possible to bundle both `SubscriptionTopic` and `Patient` in one resource.

2. Version Conversion is not supported

 	There is no `VersionConvertorFactory_40_43` implementation. Direct conversion between R4 (40) and R4B (43) is unavailable.
3. Mixed Resource (R4 Patient and R4B Patient) Serialization and Parsing

## To-do: 

