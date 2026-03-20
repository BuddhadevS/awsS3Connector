package com.tek.aws.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="students")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    @Column(name = "media_url", length = 1024)
    private String mediaUrl;

    @Column(name = "s3_key", length = 512)
    private String s3Key;

    @Column(name = "media_type", length = 128)
    private String mediaType;

    @Column(name = "original_file_name", length = 255)
    private String originalFileName;
}
