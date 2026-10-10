package com.sba.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "managers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Manager {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "manager_id", updatable = false, nullable = false)
    private UUID managerId;

    @Column(name = "full_name")
    private String fullName;

    private String phone;

    private String email;

    private String status;
}
