package com.sba.project.modules.property.service.impl;

import com.sba.project.common.exceptions.AppException;
import com.sba.project.common.exceptions.ErrorCode;
import com.sba.project.modules.property.dto.request.*;
import com.sba.project.modules.property.dto.response.*;
import com.sba.project.modules.property.entity.*;
import com.sba.project.modules.property.enums.RoomOperationalStatus;
import com.sba.project.modules.property.enums.RoomSlotStatus;
import com.sba.project.modules.property.mapper.RoomMapper;
import com.sba.project.modules.property.repository.*;
import com.sba.project.modules.property.service.RoomService;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {
    private final RoomRepository rooms;
    private final RoomSlotRepository slots;
    private final RoomTypeRepository types;
    private final EntityManager entityManager;
    private final RoomMapper mapper;

    @Override @Transactional
    public RoomResponse create(RoomRequest request) {
        requireBranch(request.getBranchId());
        RoomType type = requireType(request.getRoomTypeId());
        if (!type.getBranchId().equals(request.getBranchId())) throw new AppException(ErrorCode.INVALID_REQUEST);
        ensureCodeAvailable(request.getBranchId(), request.getRoomCode(), null);
        return mapper.toResponse(rooms.save(mapper.toEntity(request)));
    }

    @Override @Transactional(readOnly = true)
    public RoomResponse getById(UUID id) { return mapper.toResponse(requireRoom(id)); }

    @Override @Transactional(readOnly = true)
    public Page<RoomResponse> search(RoomSearchRequest c, Pageable p) {
        validatePrice(c);
        return rooms.search(false, c.getBranchId(), c.getRoomTypeId(), c.getMinPrice(), c.getMaxPrice(),
                c.getMinCapacity(), normalize(c.getStatus()), toAddressPattern(c.getAddress()), p).map(mapper::toResponse);
    }

    @Override @Transactional
    public RoomResponse update(UUID id, RoomRequest request) {
        Room room = requireRoom(id);
        requireBranch(request.getBranchId());
        RoomType type = requireType(request.getRoomTypeId());
        if (!type.getBranchId().equals(request.getBranchId())) throw new AppException(ErrorCode.INVALID_REQUEST);
        ensureCodeAvailable(request.getBranchId(), request.getRoomCode(), id);
        mapper.updateEntity(room, request);
        return mapper.toResponse(rooms.save(room));
    }

    @Override @Transactional
    public void delete(UUID id) {
        Room room = requireRoom(id);
        room.setOperationalStatus(RoomOperationalStatus.INACTIVE);
        slots.findByRoomId(id).forEach(slot -> slot.setStatus(RoomSlotStatus.INACTIVE));
    }

    @Override @Transactional(readOnly = true)
    public Page<PublicRoomResponse> searchPublicRooms(RoomSearchRequest c, Pageable p) {
        validatePrice(c);
        validatePublicSort(p.getSort());
        Pageable effective = p.getSort().isUnsorted()
                ? PageRequest.of(p.getPageNumber(), p.getPageSize(), Sort.by("code")) : p;
        return rooms.search(true, c.getBranchId(), c.getRoomTypeId(), c.getMinPrice(), c.getMaxPrice(),
                c.getMinCapacity(), normalize(c.getStatus()), toAddressPattern(c.getAddress()), effective)
                .map(this::toPublicResponse);
    }

    @Override @Transactional(readOnly = true)
    public PublicRoomResponse getPublicRoomById(UUID id) {
        Room room = rooms.findById(id).filter(r -> Boolean.TRUE.equals(r.getIsPublic()))
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
        return toPublicResponse(room);
    }

    @Override @Transactional
    public RoomSlotResponse createRoomSlot(RoomSlotRequest request) {
        requireRoom(request.getRoomId());
        RoomSlot slot = new RoomSlot(); slot.setRoomId(request.getRoomId());
        slot.setCode(request.getSlotCode()); slot.setName(request.getSlotName());
        slot.setStatus(request.getStatus() == null ? RoomSlotStatus.AVAILABLE : request.getStatus());
        return slotResponse(slots.save(slot));
    }

    @Override @Transactional(readOnly = true)
    public RoomSlotResponse getRoomSlotById(UUID id) { return slotResponse(requireSlot(id)); }

    @Override @Transactional(readOnly = true)
    public List<RoomSlotResponse> listRoomSlotsByRoom(UUID roomId) {
        requireRoom(roomId); return slots.findByRoomId(roomId).stream().map(this::slotResponse).toList();
    }

    @Override @Transactional
    public RoomSlotResponse updateRoomSlot(UUID id, RoomSlotRequest request) {
        RoomSlot slot = requireSlot(id);
        if (!slot.getRoomId().equals(request.getRoomId())) throw new AppException(ErrorCode.INVALID_REQUEST, "Không thể chuyển slot sang phòng khác");
        slot.setCode(request.getSlotCode()); slot.setName(request.getSlotName());
        if (request.getStatus() != null) slot.setStatus(request.getStatus());
        return slotResponse(slots.save(slot));
    }

    @Override @Transactional
    public void deleteRoomSlot(UUID id) { requireSlot(id).setStatus(RoomSlotStatus.INACTIVE); }

    @Override @Transactional
    public RoomTypeResponse createRoomType(RoomTypeRequest request) {
        requireBranch(request.getBranchId());
        RoomType type = new RoomType(); apply(type, request);
        return typeResponse(types.save(type));
    }

    @Override @Transactional(readOnly = true)
    public RoomTypeResponse getRoomTypeById(UUID id) { return typeResponse(requireType(id)); }

    @Override @Transactional(readOnly = true)
    public Page<RoomTypeResponse> listRoomTypes(Pageable pageable) { return types.findAll(pageable).map(this::typeResponse); }

    @Override @Transactional
    public RoomTypeResponse updateRoomType(UUID id, RoomTypeRequest request) {
        RoomType type = requireType(id); requireBranch(request.getBranchId()); apply(type, request);
        return typeResponse(types.save(type));
    }

    @Override @Transactional
    public void deleteRoomType(UUID id) {
        RoomType type = requireType(id);
        if (rooms.existsByRoomTypeId(id)) throw new AppException(ErrorCode.DATA_CONFLICT, "Không thể xóa loại phòng đang được sử dụng");
        type.setActive(false);
    }

    private Room requireRoom(UUID id) { return rooms.findById(id).orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND)); }
    private RoomSlot requireSlot(UUID id) { return slots.findById(id).orElseThrow(() -> new AppException(ErrorCode.ROOM_SLOT_NOT_FOUND)); }
    private RoomType requireType(UUID id) { return types.findById(id).orElseThrow(() -> new AppException(ErrorCode.ROOM_TYPE_NOT_FOUND)); }
    private void requireBranch(UUID id) { if (entityManager.find(Branch.class, id) == null) throw new AppException(ErrorCode.BRANCH_NOT_FOUND); }
    private void ensureCodeAvailable(UUID branch, String code, UUID currentId) {
        boolean duplicate = currentId == null ? rooms.existsByBranchIdAndCode(branch, code)
                : rooms.existsByBranchIdAndCodeAndIdNot(branch, code, currentId);
        if (duplicate) throw new AppException(ErrorCode.DATA_CONFLICT, "Mã phòng đã tồn tại trong chi nhánh: " + code);
    }
    private void validatePrice(RoomSearchRequest c) {
        if (c.getMinPrice() != null && c.getMaxPrice() != null && c.getMinPrice().compareTo(c.getMaxPrice()) > 0)
            throw new AppException(ErrorCode.INVALID_REQUEST, "Giá tối thiểu không được lớn hơn giá tối đa");
    }
    private void validatePublicSort(Sort sort) {
        for (Sort.Order order : sort) if (!List.of("referencePrice", "capacity", "code").contains(order.getProperty()))
            throw new AppException(ErrorCode.INVALID_REQUEST, "Không hỗ trợ sắp xếp theo: " + order.getProperty());
    }
    private String normalize(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private String toAddressPattern(String address) {
        String normalized = normalize(address);
        return normalized == null ? null : "%" + normalized.toLowerCase() + "%";
    }
    private PublicRoomResponse toPublicResponse(Room room) {
        Branch branch = entityManager.find(Branch.class, room.getBranchId());
        RoomType type = requireType(room.getRoomTypeId());
        return mapper.toPublicResponse(room, branch, type);
    }
    private RoomSlotResponse slotResponse(RoomSlot slot) { return RoomSlotResponse.builder().id(slot.getId()).roomId(slot.getRoomId()).code(slot.getCode()).name(slot.getName()).status(slot.getStatus()).build(); }
    private RoomTypeResponse typeResponse(RoomType t) { return RoomTypeResponse.builder().id(t.getId()).branchId(t.getBranchId()).code(t.getCode()).name(t.getName()).defaultCapacity(t.getDefaultCapacity()).description(t.getDescription()).active(t.getActive()).build(); }
    private void apply(RoomType type, RoomTypeRequest r) {
        type.setBranchId(r.getBranchId());
        String name = r.getTypeName() == null ? r.getCode() : r.getTypeName();
        type.setName(name); type.setCode(r.getCode() == null ? name : r.getCode());
        type.setDefaultCapacity(r.getDefaultCapacity()); type.setDescription(r.getDescription());
        if (r.getActive() != null) type.setActive(r.getActive());
    }
}
