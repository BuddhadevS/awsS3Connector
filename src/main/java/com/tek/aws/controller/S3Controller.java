package com.tek.aws.controller;

import com.tek.aws.dto.MediaUploadResponse;
import com.tek.aws.dto.StudentMediaResponse;
import com.tek.aws.entity.Student;
import com.tek.aws.exception.ResourceNotFoundException;
import com.tek.aws.repo.StudentRepository;
import com.tek.aws.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static java.util.Comparator.comparing;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service s3Service;
    private final StudentRepository studentRepository;

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<MediaUploadResponse> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam("email") String email
    ) {
        S3Service.UploadResult uploadResult = s3Service.uploadMedia(file);

        Student student = new Student();
        student.setName(name);
        student.setEmail(email);
        student.setS3Key(uploadResult.fileKey());
        student.setMediaUrl(uploadResult.mediaUrl());
        student.setMediaType(uploadResult.mediaType());
        student.setOriginalFileName(uploadResult.originalFileName());

        Student savedStudent = studentRepository.save(student);

        return ResponseEntity.ok(new MediaUploadResponse(
                savedStudent.getId(),
                savedStudent.getName(),
                savedStudent.getEmail(),
                savedStudent.getMediaUrl(),
                s3Service.createPresignedGetUrl(savedStudent.getS3Key()),
                savedStudent.getMediaType(),
                "Media uploaded successfully"
        ));
    }

    @GetMapping
    public ResponseEntity<List<StudentMediaResponse>> getStudents() {
        List<StudentMediaResponse> students = studentRepository.findAll().stream()
                .sorted(comparing(Student::getId).reversed())
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentMediaResponse> getStudent(@PathVariable Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found for id " + id));
        return ResponseEntity.ok(toResponse(student));
    }

    private StudentMediaResponse toResponse(Student student) {
        String displayUrl = student.getS3Key() == null ? null : s3Service.createPresignedGetUrl(student.getS3Key());
        return new StudentMediaResponse(
                student.getId(),
                student.getName(),
                student.getEmail(),
                student.getMediaUrl(),
                displayUrl,
                student.getMediaType(),
                student.getOriginalFileName()
        );
    }
}
