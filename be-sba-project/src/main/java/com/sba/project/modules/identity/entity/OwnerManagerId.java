package com.sba.project.modules.identity.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class OwnerManagerId implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "manager_id", nullable = false)
    private UUID managerId;
}
