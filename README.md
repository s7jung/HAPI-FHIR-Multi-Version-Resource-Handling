## Overview

This project experiments with using both FHIR R4 and R4B within a single application class. The goal is to determine the feasibility and effort required to program both R4 and R4B simultaneously using HAPI FHIR.

## Known Limitations 
- Validator:
  
	Does not currently work with R4B.
	May crash or produce unexpected results when used with R4B resources.

- JPA Server:

	Supports basic data storage (CRUD) and search operations, including built-in and custom search parameters.
	Most advanced functionalities, such as Subscriptions and Terminology Services, remain untested or unimplemented.

  from [FHIR R4B](https://hapifhir.io/hapi-fhir/docs/getting_started/r4b.html)

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

## Approaches to Embedding and Parsing Mixed R4 and R4B Resources
1. ResourceBundleHandler
   
	This approach uses a container tag <Resource> to store both `Patient` and `SubscriptionTopic` within
a single xml file. Each resource is serialized into the container and can be parsed separately after deserialization.

	Generated xml: 
	```
	<Resources>
	<SubscriptionTopic xmlns="http://hl7.org/fhir"><title value="Sample Subscription Topic"/></SubscriptionTopic>
	<Patient xmlns="http://hl7.org/fhir"><id value="0403"/><name><family value="Doe"/><given value="John"/></name></Patient>
	</Resources>
	```

2. SubscriptionWithPatientExtensionHandler

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
## Additional approaches to explore
1. Create a Custom FHIR Resource


## To-do: 
1. Resource Type Detection

	Currently, the code assumes parsing using the R4B context. This need to be updated to dynamically detect whether an element in the XML belongs to R4 or R4B before parsing.
3. Test Mixed Resource Serialization and Parsing
