package com.ystu.schedule.controller;

import com.ystu.schedule.dto.request.TeacherRequest;
import com.ystu.schedule.dto.response.TeacherResponse;
import com.ystu.schedule.service.TeacherService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/teachers")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @GetMapping
    public ResponseEntity<List<TeacherResponse>> getAll(
            @RequestParam(required = false) Long departmentId) {
        if (departmentId != null) {
            return ResponseEntity.ok(teacherService.getByDepartment(departmentId));
        }
        return ResponseEntity.ok(teacherService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeacherResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(teacherService.getById(id));
    }

    @PostMapping
    public ResponseEntity<TeacherResponse> create(@Valid @RequestBody TeacherRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teacherService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeacherResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody TeacherRequest request) {
        return ResponseEntity.ok(teacherService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        teacherService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/subjects/{subjectId}")
    public ResponseEntity<TeacherResponse> addSubject(@PathVariable Long id,
                                                       @PathVariable Long subjectId) {
        return ResponseEntity.ok(teacherService.addSubject(id, subjectId));
    }

    @DeleteMapping("/{id}/subjects/{subjectId}")
    public ResponseEntity<TeacherResponse> removeSubject(@PathVariable Long id,
                                                          @PathVariable Long subjectId) {
        return ResponseEntity.ok(teacherService.removeSubject(id, subjectId));
    }
}
