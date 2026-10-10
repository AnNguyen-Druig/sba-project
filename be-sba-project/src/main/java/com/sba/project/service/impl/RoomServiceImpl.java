package com.sba.project.service.impl;

import com.sba.project.dto.request.RoomRequest;
import com.sba.project.dto.request.RoomSearchRequest;
import com.sba.project.dto.response.RoomResponse;
import com.sba.project.entity.Branch;
import com.sba.project.entity.Manager;
import com.sba.project.entity.Room;
import com.sba.project.entity.RoomType;
import com.sba.project.exception.BusinessException;
import com.sba.project.exception.DuplicateResourceException;
import com.sba.project.exception.ResourceNotFoundException;
import com.sba.project.mapper.RoomMapper;
import com.sba.project.repository.RoomRepository;
import com.sba.project.repository.RoomTypeRepository;
import com.sba.project.service.RoomService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final EntityManager entityManager;
    private final RoomMapper roomMapper;

    @Override
    @Transactional
    public RoomResponse create(RoomRequest request) {
        Branch branch = requireBranch(request.getBranchId());
        Manager manager = requireManager(request.getManagerId());
        RoomType roomType = requireRoomType(request.getRoomTypeId());
        ensureRoomCodeAvailable(request.getBranchId(), request.getRoomCode());
        try {
            return roomMapper.toResponse(roomRepository.save(roomMapper.toEntity(request, branch, manager, roomType)));
        } catch (DataIntegrityViolationException exception) {
            throw duplicateRoomCode(request.getRoomCode());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public RoomResponse getById(UUID roomId) {
        return roomMapper.toResponse(requireRoom(roomId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoomResponse> search(RoomSearchRequest criteria, Pageable pageable) {
        if (criteria.getMinPrice() != null && criteria.getMaxPrice() != null
                && criteria.getMinPrice().compareTo(criteria.getMaxPrice()) > 0) {
            throw new BusinessException("Giá tối thiểu không được lớn hơn giá tối đa");
        }

        return roomRepository.search(criteria.getBranchId(), criteria.getRoomTypeId(), criteria.getMinPrice(),
                        criteria.getMaxPrice(), criteria.getMinCapacity(), normalize(criteria.getStatus()),
                        normalize(criteria.getAddress()), pageable)
                .map(roomMapper::toResponse);
    }

    @Override
    @Transactional
    public RoomResponse update(UUID roomId, RoomRequest request) {
        Room room = requireRoom(roomId);
        Branch branch = requireBranch(request.getBranchId());
        Manager manager = requireManager(request.getManagerId());
        RoomType roomType = requireRoomType(request.getRoomTypeId());

        if (!room.getBranch().getBranchId().equals(request.getBranchId())
                || !room.getRoomCode().equals(request.getRoomCode())) {
            ensureRoomCodeAvailable(request.getBranchId(), request.getRoomCode());
        }

        roomMapper.updateEntity(room, request, branch, manager, roomType);
        try {
            return roomMapper.toResponse(roomRepository.save(room));
        } catch (DataIntegrityViolationException exception) {
            throw duplicateRoomCode(request.getRoomCode());
        }
    }

    @Override
    @Transactional
    public void delete(UUID roomId) {
        Room room = requireRoom(roomId);
        try {
            roomRepository.delete(room);
            roomRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateResourceException("Không thể xóa phòng đang được sử dụng");
        }
    }

    private Room requireRoom(UUID roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng: " + roomId));
    }

    private Branch requireBranch(UUID branchId) {
        Branch branch = entityManager.find(Branch.class, branchId);
        if (branch == null) {
            throw new ResourceNotFoundException("Không tìm thấy chi nhánh: " + branchId);
        }
        return branch;
    }

    private Manager requireManager(UUID managerId) {
        Manager manager = entityManager.find(Manager.class, managerId);
        if (manager == null) {
            throw new ResourceNotFoundException("Không tìm thấy quản lý: " + managerId);
        }
        return manager;
    }

    private RoomType requireRoomType(UUID roomTypeId) {
        return roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại phòng: " + roomTypeId));
    }

    private void ensureRoomCodeAvailable(UUID branchId, String roomCode) {
        if (roomRepository.existsByBranch_BranchIdAndRoomCode(branchId, roomCode)) {
            throw duplicateRoomCode(roomCode);
        }
    }

    private DuplicateResourceException duplicateRoomCode(String roomCode) {
        return new DuplicateResourceException("Mã phòng đã tồn tại trong chi nhánh: " + roomCode);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
