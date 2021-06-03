package org.opencds.cqf.cql.evaluator.fhir.builders.r4;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.opencds.cqf.cql.evaluator.fhir.builders.BaseBuilder;  

public class OperationOutcomeBuilder extends BaseBuilder<OperationOutcome> {
    public OperationOutcomeBuilder() {
        super(new OperationOutcome());
    }

    public OperationOutcomeBuilder buildIssue(String severity, String code, String details) {
        complexProperty.addIssue(new OperationOutcome.OperationOutcomeIssueComponent()
                .setSeverity(OperationOutcome.IssueSeverity.fromCode(severity))
                .setCode(OperationOutcome.IssueType.fromCode(code)).setDetails(new CodeableConcept().setText(details)));

        return this;
    }
}
