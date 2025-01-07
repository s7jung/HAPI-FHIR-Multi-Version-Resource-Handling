package ca.uhn.fhir.jpa.starter.util;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import org.hl7.fhir.r4b.model.DomainResource;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4b.model.ResourceType;
import org.hl7.fhir.r4b.model.SubscriptionTopic;

import java.util.ArrayList;
import java.util.List;

/* Custom R4B resource that includes both SubscriptionTopic and
	R4 Patient Resource. */
@ResourceDef(name = "CustomR4BResource", profile = "http://s7jung/fhir/StructureDefinition/CustomR4BResource")
public class CustomR4BResource extends DomainResource {
	private Patient r4patient;
	private SubscriptionTopic subscriptionTopic;
	private List<Patient> containedPatients = new ArrayList<Patient>();


	public Patient getPatient() {
		return r4patient;
	}

	public void setPatient(Patient patient) {
		this.r4patient = patient;
	}

	public SubscriptionTopic getSubscriptionTopic() {
		return subscriptionTopic;
	}

	public void setSubscriptionTopic(SubscriptionTopic subscriptionTopic) {
		this.subscriptionTopic = subscriptionTopic;
	}


	private String customProperty;

	public String getCustomProperty() {
		return customProperty;
	}

	public void setCustomProperty(String customProperty) {
		this.customProperty = customProperty;
	}

	@Override
	public CustomR4BResource copy() {
		CustomR4BResource copy = new CustomR4BResource();
		copy.setCustomProperty(this.customProperty);
		return copy;
	}

	@Override
	public ResourceType getResourceType() {
		return null;
	}

	public void addContainedPatient(Patient resource) {
		if (resource == null) {
			throw new IllegalArgumentException("Resource cannot be null");
		}
		containedPatients.add(resource);
	}
	public List<Patient> getContainedPatient(){
		return containedPatients;
	}
}