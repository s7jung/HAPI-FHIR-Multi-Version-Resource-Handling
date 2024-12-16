package ca.uhn.fhir.jpa.starter;

import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4b.model.SubscriptionTopic;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class SubscriptionHandler {

	private static final FhirContext R4_CONTEXT = FhirContext.forR4();
	private static final FhirContext R4B_CONTEXT = FhirContext.forR4B();

	// Create SubscriptionTopic and Patient, save to XML
	public void saveResourcesToXml(String filePath) throws IOException {
		SubscriptionTopic subscriptionTopic = new SubscriptionTopic();
		subscriptionTopic.setTitle("Sample Subscription Topic");

		Patient patient = new Patient();
		patient.setId("0403");
		patient.addName().setFamily("Doe").addGiven("John");

		IParser r4bParser = R4B_CONTEXT.newXmlParser();
		IParser r4Parser = R4_CONTEXT.newXmlParser();

		String subscriptionXml = r4bParser.encodeResourceToString(subscriptionTopic);
		String patientXml = r4Parser.encodeResourceToString(patient);

		// Serialize to XML with a container!
		String combinedXml = "<Resources>\n"
			+ r4bParser.encodeResourceToString(subscriptionTopic) + "\n"
			+ r4Parser.encodeResourceToString(patient) + "\n"
			+ "</Resources>";
		Files.write(new File(filePath).toPath(), combinedXml.getBytes());
	}

	public void loadResourcesFromXml(String filePath) throws IOException {
		String content = Files.readString(new File(filePath).toPath());

		// Remove container tags for parsing individual resources
		String subscriptionXml = content.substring(content.indexOf("<SubscriptionTopic"),
			content.indexOf("</SubscriptionTopic>") + "</SubscriptionTopic>".length());
		String patientXml = content.substring(content.indexOf("<Patient"),
			content.indexOf("</Patient>") + "</Patient>".length());

		// parse R4B
		try {
			SubscriptionTopic subscriptionTopic = (SubscriptionTopic) R4B_CONTEXT.newXmlParser()
				.parseResource(SubscriptionTopic.class, subscriptionXml);
			System.out.println("Parsed R4B SubscriptionTopic: " + subscriptionTopic.getTitle());
		} catch (Exception e) {
			System.out.println("R4B parsing failed: " + e.getMessage());
		}

		// parse R4
		try {
			Patient patient = (Patient) R4_CONTEXT.newXmlParser()
				.parseResource(Patient.class, patientXml);
			System.out.println("Parsed R4 Patient: " + patient.getNameFirstRep().getFamily());
		} catch (Exception e) {
			System.out.println("R4 parsing failed: " + e.getMessage());
		}
	}
}