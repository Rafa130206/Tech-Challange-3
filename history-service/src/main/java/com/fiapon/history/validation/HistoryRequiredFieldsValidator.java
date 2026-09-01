package com.fiapon.history.validation;

import com.fiapon.history.dto.HistoryRequest;
import com.fiapon.history.exceptions.InvalidHistoryDataException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class HistoryRequiredFieldsValidator implements HistoryValidator {

    @Override
    public void validate(HistoryRequest request, Long schedulingId){
        List<String> missingFields = new ArrayList<String>();

        if (request.doctorId() == null){
            missingFields.add("doctorId");
        }

        if (request.patientId() == null){
            missingFields.add("patientId");
        }

        if (request.date() == null){
            missingFields.add("date");
        }

        if (request.schedulingId() == null){
            missingFields.add("schedulingId");
        }

        if (request.medicalRecords() == null || request.medicalRecords().isBlank()){
            missingFields.add("medicalRecords");
        }

        String errorMessage = String.join(", ", missingFields);

        if (!missingFields.isEmpty()){
            throw new InvalidHistoryDataException("Missing fields for schedule Id " + schedulingId + ": " + errorMessage);
        }
    }
}
