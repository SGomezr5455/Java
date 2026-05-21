package com.ystu.schedule.controller;

import com.ystu.schedule.dto.request.LessonRequest;
import com.ystu.schedule.dto.response.LessonResponse;
import com.ystu.schedule.service.LessonService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lessons")
public class LessonController {

    private final LessonService lessonService;

    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @GetMapping
    public ResponseEntity<List<LessonResponse>> getAll(
            @RequestParam(required = false) Long groupId,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) Integer weekNumber) {
        if (groupId != null) {
            return ResponseEntity.ok(lessonService.getByGroup(groupId));
        }
        if (teacherId != null) {
            return ResponseEntity.ok(lessonService.getByTeacher(teacherId));
        }
        if (roomId != null) {
            return ResponseEntity.ok(lessonService.getByRoom(roomId));
        }
        if (weekNumber != null) {
            return ResponseEntity.ok(lessonService.getByWeek(weekNumber));
        }
        return ResponseEntity.ok(lessonService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LessonResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(lessonService.getById(id));
    }

    @PostMapping
    public ResponseEntity<LessonResponse> create(@Valid @RequestBody LessonRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lessonService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LessonResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody LessonRequest request) {
        return ResponseEntity.ok(lessonService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        lessonService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/groups/{groupId}")
    public ResponseEntity<LessonResponse> addGroup(@PathVariable Long id,
                                                    @PathVariable Long groupId) {
        return ResponseEntity.ok(lessonService.addGroup(id, groupId));
    }

    @DeleteMapping("/{id}/groups/{groupId}")
    public ResponseEntity<LessonResponse> removeGroup(@PathVariable Long id,
                                                       @PathVariable Long groupId) {
        return ResponseEntity.ok(lessonService.removeGroup(id, groupId));
    }
}
