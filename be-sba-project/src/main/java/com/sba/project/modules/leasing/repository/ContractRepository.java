package com.sba.project.modules.leasing.repository;

import com.sba.project.modules.leasing.entity.Contract;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractRepository extends JpaRepository<Contract, UUID> {
}
