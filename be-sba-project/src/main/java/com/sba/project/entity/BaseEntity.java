package com.sba.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, columnDefinition = "timestamp not null default current_timestamp")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamp not null default current_timestamp")
    private LocalDateTime updatedAt;
}
