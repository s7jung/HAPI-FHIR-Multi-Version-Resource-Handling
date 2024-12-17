## R4B suppport and limitation
from [FHIR R4B](https://hapifhir.io/hapi-fhir/docs/getting_started/r4b.html): 
> - FHIR Parsers/Serializers are well tested and should be fully functional, as there is no need for any specific R4B code in this part of the codebase.
> - FHIR Client is well tested and should be fully functional, as there is no need for any specific R4B code in this part of the codebase.
> - Plain Server is well tested and should be fully functional, as there is no need for any specific R4B code in this part of the codebase.
> - Validator does not currently work with R4B and may crash or cause unexpected results. Note that the underlying InstanceValidator is able to validate R4B resources, so this could be fixed in a future release if demand is there. The FHIR Validator Site can also be used.
> - JPA Server is able to store data, and supports all basic storage interactions (CRUD). Search also works, including built-in and custom search parameters. However most other functionality has not been implemented or tested, including Subscriptions, Terminology Services, etc.



## some findings 

1. Operations like `subscriptionTopic.addContained(r4Patient)` are not supported due to compatibility issues. However, something like the following works:
```
String patientXml = r4Parser.encodeResourceToString(r4Patient);

subscriptionTopic.addExtension("http://hl7.org/fhir/StructureDefinition/embedded-patient",
			new org.hl7.fhir.r4b.model.StringType(patientXml)); 
```
This allows encapsulating R4 Data (Patient in this case) inside the SubscriptionTopic without violating FHIR’s data model. The idea is that SubscriptionTopic might be used in contexts where Patient resource is included as part of the subscription's metadata, so embedding it as an extension makes it possible to bundle both the SubscriptionTopic and the Patient in one resource.
Note: the url is a placeholder, it is a unique identifier for the extension but it does not point to anything in this example 

3. There is no `VersionConvertorFactory_40_43` implementation. Direct conversion between R4 (40) and R4B (43) is unavailable.

