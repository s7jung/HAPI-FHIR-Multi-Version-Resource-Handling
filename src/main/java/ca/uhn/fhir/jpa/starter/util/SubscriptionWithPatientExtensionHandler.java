package ca.uhn.fhir.jpa.starter.util;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4b.model.Extension;
import org.hl7.fhir.r4b.model.SubscriptionTopic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


/*
The Patient resource is serialized into an XML string and added as an extension
to the SubscriptionTopic with a URL (arbitray). The file is then saved as an XML, and later loaded
and parsed using FHIR R4B. The code attempts to parse the XML with R4B first, extracting
the embedded Patient resource.
*/

public class SubscriptionWithPatientExtensionHandler {

	private static final FhirContext R4_CONTEXT = FhirContext.forR4();
	private static final FhirContext R4B_CONTEXT = FhirContext.forR4B();

	public static SubscriptionTopic createR4BSubscriptionWithPatient() throws IOException {
		IParser r4Parser = R4_CONTEXT.newXmlParser();
		IParser r4bParser = R4B_CONTEXT.newXmlParser().setPrettyPrint(true);

		Patient r4Patient = new Patient();
		r4Patient.setId("0403");
		r4Patient.addName().setFamily("Doe").addGiven("John");
		r4Patient.addExtension("https://hl7.org/fhir/r4",
			new org.hl7.fhir.r4.model.StringType("R4"));
		String patientXml = r4Parser.encodeResourceToString(r4Patient);

		org.hl7.fhir.r4b.model.Patient r4bPatient = new org.hl7.fhir.r4b.model.Patient();
		r4bPatient.setId("1234");
		r4bPatient.addName().setFamily("Gray").addGiven("Roberta");
		r4bPatient.addExtension("https://hl7.org/fhir/r4b",
			new org.hl7.fhir.r4b.model.StringType("R4B"));
		String r4bPatientXml = r4bParser.encodeResourceToString(r4bPatient);

		SubscriptionTopic subscriptionTopic = new SubscriptionTopic();
		subscriptionTopic.setTitle("Sample Subscription Topic");
		subscriptionTopic.addExtension("https://hl7.org/fhir/r4b",
			new org.hl7.fhir.r4b.model.StringType("R4B"));
		subscriptionTopic.setId("0101");
		subscriptionTopic.addExtension("https://hl7.org/fhir/r4/embedded-patient/1",
			new org.hl7.fhir.r4b.model.StringType(patientXml)); // embed an XML representation of r4Patient
		subscriptionTopic.addExtension("https://hl7.org/fhir/r4b/embedded-patient/1",
			new org.hl7.fhir.r4b.model.StringType(r4bPatientXml)); // embed an XML representation of r4Patient

		String subscriptionXml = r4bParser.encodeResourceToString(subscriptionTopic);
		Files.writeString(Paths.get("subscriptionTopic_with_patient.xml"), subscriptionXml);

		System.out.println("Resources saved to XML.");
		return subscriptionTopic;
	}

	public static void loadAndParseXML() throws IOException {
		Path filePath = Paths.get("subscriptionTopic_with_patient.xml");
		String xmlContent = Files.readString(filePath);

		String fhirVersion = getFHIRVersionFromXML(xmlContent);
		System.out.println(fhirVersion);

		try {
			if ("R4B".equals(fhirVersion)) {
				System.out.println("Detected FHIR version: R4B");
				IBaseResource r4bResource = R4B_CONTEXT.newXmlParser().parseResource(xmlContent);
				if (r4bResource instanceof SubscriptionTopic) {
					SubscriptionTopic parsedSubscription = (SubscriptionTopic) r4bResource;
					System.out.println("Parsed R4B SubscriptionTopic: " + parsedSubscription.getTitle());

					for (Extension extension : parsedSubscription.getExtension()) {
						if (extension.getUrl().equals("https://hl7.org/fhir/r4")
								|| extension.getUrl().equals("https://hl7.org/fhir/r4b")) continue;
						if (getFHIRVersionFromXML(extension.getUrl()).equals("R4")) {
							// currently only handles Patient resource type
							String patientXml = extension.getValueAsPrimitive().getValueAsString();
							org.hl7.fhir.r4.model.Patient r4patient = R4_CONTEXT.newXmlParser().parseResource(org.hl7.fhir.r4.model.Patient.class, patientXml);
							System.out.println("Parsed R4 Patient: " + r4patient.getNameFirstRep().getFamily());
						} else if (getFHIRVersionFromXML(extension.getUrl()).equals("R4B")) {
							String patientXml = extension.getValueAsPrimitive().getValueAsString();
							org.hl7.fhir.r4b.model.Patient r4bpatient = R4B_CONTEXT.newXmlParser().parseResource(org.hl7.fhir.r4b.model.Patient.class, patientXml);
							System.out.println("Parsed R4B Patient: " + r4bpatient.getNameFirstRep().getFamily());
						} else {
							System.err.println("Unknown FHIR version: " + getFHIRVersionFromXML(extension.getUrl()));
						}
					}
				}
			}
		} catch(Exception e){
			System.err.println("Error parsing XML: " + e.getMessage());
		}
	}

	private static String getFHIRVersionFromXML(String xmlContent) throws IOException {
		if (xmlContent.contains("https://hl7.org/fhir/r4b")) {
			return "R4B";
		} else if (xmlContent.contains("https://hl7.org/fhir/r4")) {
			return "R4";
		}
		return "Unknown";
	}
}