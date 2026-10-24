package com.sba.project.modules.utilities.repository;

import com.sba.project.modules.utilities.entity.ContractService;
import com.sba.project.modules.utilities.enums.ContractServiceStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for {@link ContractService} entity.
 */
@Repository
public interface ContractServiceRepository extends JpaRepository<ContractService, UUID>, JpaSpecificationExecutor<ContractService> {

    List<ContractService> findByContractIdAndStatus(UUID contractId, ContractServiceStatus status);

    List<ContractService> findByContractTenantIdAndStatus(UUID contractTenantId, ContractServiceStatus status);

    List<ContractService> findByRoomServiceIdAndStatus(UUID roomServiceId, ContractServiceStatus status);

    List<ContractService> findByRoomIdAndStatus(UUID roomId, ContractServiceStatus status);

    List<ContractService> findByContractId(UUID contractId);

    List<ContractService> findByRoomServiceIdIn(List<UUID> roomServiceIds);

    List<ContractService> findByRoomServiceIdInAndStatus(List<UUID> roomServiceIds, ContractServiceStatus status);

    List<ContractService> findByStatus(ContractServiceStatus status);
}
