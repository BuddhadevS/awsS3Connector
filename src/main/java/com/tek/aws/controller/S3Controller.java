package com.tek.aws.controller;

import com.tek.aws.repo.StudentRepository;
import com.tek.aws.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/s3")
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service s3Service;
    private final StudentRepository studentRepository;

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<String> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("studentId") Long studentId
    ) {

        String s3Key = s3Service.uploadImage(file);

        studentRepository.updateStudentImage(studentId, s3Key);

        return ResponseEntity.ok("Image uploaded successfully: " + s3Key);
    }
}




