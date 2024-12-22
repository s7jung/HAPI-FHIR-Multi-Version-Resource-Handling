package ca.uhn.fhir.jpa.starter.util;

import org.hl7.fhir.r4b.model.DomainResource;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4b.model.ResourceType;
import org.hl7.fhir.r4b.model.SubscriptionTopic;

/* Custom R4B resource that includes both SubscriptionTopic and
	R4 Patient Resource. */
public class CustomR4BResource extends DomainResource {
	private Patient r4patient;
	private SubscriptionTopic subscriptionTopic;

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

	@Override
	public DomainResource copy() {
		return null;
	}

	@Override
	public ResourceType getResourceType() {
		return null;
	}
}
