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

    private String imageKey; // S3 path
}


