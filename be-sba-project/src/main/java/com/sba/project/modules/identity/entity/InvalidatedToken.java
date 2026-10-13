package com.sba.project.modules.identity.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "invalidate_tokens", schema = "public")
@Getter
@Setter
@NoArgsConstructor
public class InvalidatedToken {
    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "expired_time", nullable = false)
    private LocalDateTime expiredTime;

    public InvalidatedToken(String id, LocalDateTime expiredTime) {
        this.id = id;
        this.expiredTime = expiredTime;
    }
}
