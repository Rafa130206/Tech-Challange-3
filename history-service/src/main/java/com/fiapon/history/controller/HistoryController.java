package com.fiapon.history.controller;

import com.fiapon.history.dto.HistoryRequest;
import com.fiapon.history.dto.HistoryResponse;
import com.fiapon.history.service.HistoryService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class HistoryController {

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    public List<HistoryResponse> getAll(){
        return historyService.getAll();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    public List<HistoryResponse> getByDoctorId(@Argument Long doctorId){
        return historyService.getByDoctorId(doctorId);
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    public HistoryResponse getBySchedulingId(@Argument Long schedulingId){
        return historyService.getBySchedulingId(schedulingId);
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    public List<HistoryResponse> getByPatientId(@Argument Long patientId){
        return historyService.getByPatientId(patientId);
    }

    @QueryMapping
    @PreAuthorize("hasRole('PATIENT')")
    public List<HistoryResponse> myHistory() {
        Long patientId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
        return historyService.getByPatientId(patientId);
    }

    @MutationMapping
    @PreAuthorize("hasRole('NURSE')")
    public HistoryResponse create (@Argument HistoryRequest input) {
        return historyService.create(input);
    }

    @MutationMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public HistoryResponse update (@Argument HistoryRequest input, @Argument Long schedulingId){
        return historyService.update(input, schedulingId);
    }

    @MutationMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public Boolean delete (@Argument Long schedulingId){
        historyService.delete(schedulingId);
        return true;
    }
}
