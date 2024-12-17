package ca.uhn.fhir.jpa.starter.util;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Patient;
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
		Patient r4Patient = new Patient();
		r4Patient.setId("0403");
		r4Patient.addName().setFamily("Doe").addGiven("John");
		IParser r4Parser = R4_CONTEXT.newXmlParser();
		String patientXml = r4Parser.encodeResourceToString(r4Patient);

		SubscriptionTopic subscriptionTopic = new SubscriptionTopic();
		subscriptionTopic.setTitle("Sample Subscription Topic");
		subscriptionTopic.setId(r4Patient.getIdPart()); // set r4 Id
		subscriptionTopic.addExtension("http://hl7.org/fhir/StructureDefinition/embedded-patient",
			new org.hl7.fhir.r4b.model.StringType(patientXml)); // embed an XML representation of r4Patient
		IParser r4bParser = R4B_CONTEXT.newXmlParser().setPrettyPrint(true);

		String subscriptionXml = r4bParser.encodeResourceToString(subscriptionTopic);
		Files.writeString(Paths.get("subscriptiontopic_with_patient.xml"), subscriptionXml);

		System.out.println("Resources saved to XML.");
		return subscriptionTopic;
	}

	public static void loadAndParseXML() throws IOException {
		Path filePath = Paths.get("subscriptiontopic_with_patient.xml");
		String xmlContent = Files.readString(filePath);

		// Try parsing with R4B first
		try {
			IBaseResource r4bResource = R4B_CONTEXT.newXmlParser().parseResource(xmlContent); // TODO can't assume it is r4b
			if (r4bResource instanceof SubscriptionTopic) {
				SubscriptionTopic parsedSubscription = (SubscriptionTopic) r4bResource;
				System.out.println("Parsed R4B SubscriptionTopic: " + parsedSubscription.getTitle());

				// Extract embedded Patient from Extension
				String patientXml = parsedSubscription.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/embedded-patient")
					.getValueAsPrimitive()
					.getValueAsString();
				Patient parsedPatient = (Patient) R4_CONTEXT.newXmlParser().parseResource(Patient.class, patientXml);
				System.out.println("Parsed R4 Patient: " + parsedPatient.getNameFirstRep().getFamily());
			}
		} catch (Exception e) {
			System.out.println("Failed to parse as R4B: " + e.getMessage());
		}
	}
}