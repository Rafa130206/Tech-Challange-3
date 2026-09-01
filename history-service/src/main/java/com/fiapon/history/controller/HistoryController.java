package com.fiapon.history.controller;

import com.fiapon.history.dto.HistoryRequest;
import com.fiapon.history.dto.HistoryResponse;
import com.fiapon.history.service.HistoryService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class HistoryController {

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @QueryMapping
    public List<HistoryResponse> getAll(){
        return historyService.getAll();
    }

    @QueryMapping
    public List<HistoryResponse> getByDoctorId(@Argument Long doctorId){
        return historyService.getByDoctorId(doctorId);
    }

    @QueryMapping
    public HistoryResponse getBySchedulingId(@Argument Long schedulingId){
        return historyService.getBySchedulingId(schedulingId);
    }

    @QueryMapping
    public List<HistoryResponse> getByPatientId(@Argument Long patientId){
        return historyService.getByPatientId(patientId);
    }

    @MutationMapping
    public HistoryResponse create (@Argument HistoryRequest input) {
        return historyService.create(input);
    }

    @MutationMapping
    public HistoryResponse update (@Argument HistoryRequest input, @Argument Long schedulingId){
        return historyService.update(input, schedulingId);
    }

    @MutationMapping
    public Boolean delete (@Argument Long schedulingId){
        historyService.delete(schedulingId);
        return true;
    }
}
