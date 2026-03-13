package com.recruting.demo.controller;

import com.recruting.demo.dto.InterviewDTO;
import com.recruting.demo.service.InterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
public class InterviewController {
    
    private final InterviewService interviewService;
    
    @GetMapping
    public ResponseEntity<List<InterviewDTO>> getAllInterviews() {
        return ResponseEntity.ok(interviewService.getAllInterviews());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<InterviewDTO> getInterviewById(@PathVariable Long id) {
        return ResponseEntity.ok(interviewService.getInterviewById(id));
    }
    
    @PostMapping
    public ResponseEntity<InterviewDTO> createInterview(@Valid @RequestBody InterviewDTO interviewDTO) {
        InterviewDTO created = interviewService.createInterview(interviewDTO);
        return ResponseEntity.status(201).body(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<InterviewDTO> updateInterview(@PathVariable Long id, 
                                                       @Valid @RequestBody InterviewDTO interviewDTO) {
        return ResponseEntity.ok(interviewService.updateInterview(id, interviewDTO));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInterview(@PathVariable Long id) {
        interviewService.deleteInterview(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/application/{applicationId}")
    public ResponseEntity<List<InterviewDTO>> getInterviewsByApplication(@PathVariable Long applicationId) {
        return ResponseEntity.ok(interviewService.getInterviewsByApplication(applicationId));
    }
    
    @GetMapping("/interviewer/{interviewer}")
    public ResponseEntity<List<InterviewDTO>> getInterviewsByInterviewer(@PathVariable String interviewer) {
        return ResponseEntity.ok(interviewService.getInterviewsByInterviewer(interviewer));
    }
    
    @GetMapping("/upcoming")
    public ResponseEntity<List<InterviewDTO>> getUpcomingInterviews() {
        return ResponseEntity.ok(interviewService.getUpcomingInterviews());
    }
}
