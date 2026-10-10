package com.sba.project.repository;
import com.sba.project.entity.ContractService;
import com.sba.project.enums.ServiceRegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;
@Repository
public interface ContractServiceRepository extends JpaRepository<ContractService, UUID> {
    List<ContractService> findByContractId(UUID contractId);
    List<ContractService> findByContractIdAndRegistrationStatus(UUID contractId, ServiceRegistrationStatus status);
    boolean existsByContractIdAndRoomServiceIdAndRegistrationStatusIn(
        UUID contractId, UUID roomServiceId, List<ServiceRegistrationStatus> statuses
    );
}
