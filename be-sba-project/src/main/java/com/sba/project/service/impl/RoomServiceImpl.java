package com.sba.project.service.impl;

import com.sba.project.dto.request.RoomRequest;
import com.sba.project.dto.request.RoomSearchRequest;
import com.sba.project.dto.request.RoomSlotRequest;
import com.sba.project.dto.request.RoomTypeRequest;
import com.sba.project.dto.response.PublicRoomResponse;
import com.sba.project.dto.response.RoomResponse;
import com.sba.project.dto.response.RoomSlotResponse;
import com.sba.project.dto.response.RoomTypeResponse;
import com.sba.project.entity.Branch;
import com.sba.project.entity.Manager;
import com.sba.project.entity.Room;
import com.sba.project.entity.RoomSlot;
import com.sba.project.entity.RoomType;
import com.sba.project.exception.BusinessException;
import com.sba.project.exception.DuplicateResourceException;
import com.sba.project.exception.ResourceNotFoundException;
import com.sba.project.mapper.RoomMapper;
import com.sba.project.mapper.RoomSlotMapper;
import com.sba.project.mapper.RoomTypeMapper;
import com.sba.project.repository.RoomRepository;
import com.sba.project.repository.RoomSlotRepository;
import com.sba.project.repository.RoomTypeRepository;
import com.sba.project.service.RoomService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private static final Set<String> ALLOWED_PUBLIC_SORT_PROPERTIES = Set.of("referencePrice", "capacity", "roomCode");

    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final RoomSlotRepository roomSlotRepository;
    private final EntityManager entityManager;
    private final RoomMapper roomMapper;
    private final RoomSlotMapper roomSlotMapper;
    private final RoomTypeMapper roomTypeMapper;

    @Override
    @Transactional
    public RoomResponse create(RoomRequest request) {
        Branch branch = requireBranch(request.getBranchId());
        Manager manager = requireManager(request.getManagerId());
        RoomType roomType = requireRoomType(request.getRoomTypeId());
        ensureRoomCodeAvailable(request.getBranchId(), request.getRoomCode());
        try {
            return roomMapper.toResponse(roomRepository.saveAndFlush(roomMapper.toEntity(request, branch, manager, roomType)));
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
            return roomMapper.toResponse(roomRepository.saveAndFlush(room));
        } catch (DataIntegrityViolationException exception) {
            throw duplicateRoomCode(request.getRoomCode());
        }
    }

    @Override
    @Transactional
    public void delete(UUID roomId) {
        Room room = requireRoom(roomId);
        roomSlotRepository.findByRoom_RoomId(roomId).forEach(slot -> slot.setDeleted(true));
        room.setDeleted(true);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PublicRoomResponse> searchPublicRooms(RoomSearchRequest criteria, Pageable pageable) {
        if (criteria.getMinPrice() != null && criteria.getMaxPrice() != null
                && criteria.getMinPrice().compareTo(criteria.getMaxPrice()) > 0) {
            throw new BusinessException("Giá tối thiểu không được lớn hơn giá tối đa");
        }
        validatePublicSort(pageable.getSort());
        Pageable effectivePageable = pageable.getSort().isUnsorted()
                ? PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("roomCode"))
                : pageable;

        return roomRepository.search(criteria.getBranchId(), criteria.getRoomTypeId(), criteria.getMinPrice(),
                        criteria.getMaxPrice(), criteria.getMinCapacity(), normalize(criteria.getStatus()),
                        normalize(criteria.getAddress()), effectivePageable)
                .map(roomMapper::toPublicResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PublicRoomResponse getPublicRoomById(UUID roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng: " + roomId));
        return roomMapper.toPublicResponse(room);
    }

    @Override
    @Transactional
    public RoomSlotResponse createRoomSlot(RoomSlotRequest request) {
        Room room = requireRoom(request.getRoomId());
        return roomSlotMapper.toResponse(roomSlotRepository.save(roomSlotMapper.toEntity(request, room)));
    }

    @Override
    @Transactional(readOnly = true)
    public RoomSlotResponse getRoomSlotById(UUID roomSlotId) {
        return roomSlotMapper.toResponse(requireRoomSlot(roomSlotId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomSlotResponse> listRoomSlotsByRoom(UUID roomId) {
        requireRoom(roomId);
        return roomSlotRepository.findByRoom_RoomId(roomId).stream()
                .map(roomSlotMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public RoomSlotResponse updateRoomSlot(UUID roomSlotId, RoomSlotRequest request) {
        RoomSlot roomSlot = requireRoomSlot(roomSlotId);
        if (!roomSlot.getRoom().getRoomId().equals(request.getRoomId())) {
            throw new BusinessException("Không thể chuyển slot sang phòng khác");
        }
        roomSlotMapper.updateEntity(roomSlot, request, roomSlot.getRoom());
        return roomSlotMapper.toResponse(roomSlotRepository.save(roomSlot));
    }

    @Override
    @Transactional
    public void deleteRoomSlot(UUID roomSlotId) {
        RoomSlot roomSlot = requireRoomSlot(roomSlotId);
        roomSlot.setDeleted(true);
    }

    @Override
    @Transactional
    public RoomTypeResponse createRoomType(RoomTypeRequest request) {
        RoomType roomType = roomTypeRepository.save(roomTypeMapper.toEntity(request));
        return roomTypeMapper.toResponse(roomType);
    }

    @Override
    @Transactional(readOnly = true)
    public RoomTypeResponse getRoomTypeById(UUID roomTypeId) {
        return roomTypeMapper.toResponse(requireRoomType(roomTypeId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoomTypeResponse> listRoomTypes(Pageable pageable) {
        return roomTypeRepository.findAll(pageable).map(roomTypeMapper::toResponse);
    }

    @Override
    @Transactional
    public RoomTypeResponse updateRoomType(UUID roomTypeId, RoomTypeRequest request) {
        RoomType roomType = requireRoomType(roomTypeId);
        roomTypeMapper.updateEntity(roomType, request);
        return roomTypeMapper.toResponse(roomTypeRepository.save(roomType));
    }

    @Override
    @Transactional
    public void deleteRoomType(UUID roomTypeId) {
        RoomType roomType = requireRoomType(roomTypeId);
        if (roomRepository.existsReferencedByAnyRoom(roomTypeId)) {
            throw new DuplicateResourceException("Không thể xóa loại phòng đang được sử dụng");
        }
        roomType.setDeleted(true);
    }

    private Room requireRoom(UUID roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng: " + roomId));
    }

    private RoomSlot requireRoomSlot(UUID roomSlotId) {
        return roomSlotRepository.findById(roomSlotId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy slot: " + roomSlotId));
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

    private void validatePublicSort(Sort sort) {
        for (Sort.Order order : sort) {
            if (!ALLOWED_PUBLIC_SORT_PROPERTIES.contains(order.getProperty())) {
                throw new BusinessException("Không hỗ trợ sắp xếp theo: " + order.getProperty());
            }
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
