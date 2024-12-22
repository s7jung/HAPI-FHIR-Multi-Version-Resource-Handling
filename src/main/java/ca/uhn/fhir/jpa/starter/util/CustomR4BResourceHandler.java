package ca.uhn.fhir.jpa.starter.util;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4b.model.SubscriptionTopic;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CustomR4BResourceHandler {
	private static final FhirContext fhirContext = FhirContext.forR4B();

	// This method creates XML from CustomR4BResource and write it to a file
	public static void writeResourceToXml(CustomR4BResource resource, String filePath) throws IOException {
		IParser xmlParser = fhirContext.newXmlParser();
		xmlParser.setPrettyPrint(true);
		String xmlString = xmlParser.encodeResourceToString(resource); // TODO fix: HAPI-1716: Resource class[ca.uhn.fhir.jpa.starter.util.CustomR4BResource] does not contain any valid HAPI-FHIR annotations
		System.out.println("here11");
		try (FileWriter writer = new FileWriter(filePath)) {
			writer.write(xmlString);
			System.out.println("Resource successfully written to XML file: " + filePath);
		}
	}

	// This method reads XML from a file and creates a CustomR4BResource object
	public static CustomR4BResource readResourceFromXml(String filePath) throws IOException {
		String xmlContent = new String(Files.readAllBytes(Paths.get(filePath)));
		IParser xmlParser = fhirContext.newXmlParser();

		// Parse the XML and check if it's R4B-compatible
		if (xmlContent.contains("<SubscriptionTopic") && xmlContent.contains("<Patient")) {
			CustomR4BResource resource = (CustomR4BResource) xmlParser.parseResource(CustomR4BResource.class, xmlContent);
			System.out.println("Resource successfully loaded from XML file: " + filePath);
			return resource;
		} else {
			throw new IllegalArgumentException("The XML file does not contain valid R4B CustomR4BResource data.");
		}
	}

	public static CustomR4BResource createSampleResource() {
		CustomR4BResource customResource = new CustomR4BResource();

		SubscriptionTopic subscriptionTopic = new SubscriptionTopic();
		subscriptionTopic.setId("Sample Subscription Topic");
		subscriptionTopic.setUrl("https://hl7.org/fhir/r4b/SubscriptionTopic");
		customResource.setSubscriptionTopic(subscriptionTopic);

		Patient patient = new Patient();
		patient.setId("R4-patient");
		patient.addName().setFamily("Doe").addGiven("John");
		customResource.setPatient(patient);
		patient.addExtension("https://hl7.org/fhir/r4",
			new org.hl7.fhir.r4.model.StringType("R4"));

		return customResource;
	}
}
