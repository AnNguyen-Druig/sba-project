package com.sba.project.service.impl;

import com.sba.project.dto.request.RoomTypeRequest;
import com.sba.project.dto.response.RoomTypeResponse;
import com.sba.project.entity.RoomType;
import com.sba.project.exception.DuplicateResourceException;
import com.sba.project.exception.ResourceNotFoundException;
import com.sba.project.mapper.RoomTypeMapper;
import com.sba.project.repository.RoomTypeRepository;
import com.sba.project.service.RoomTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomTypeServiceImpl implements RoomTypeService {

    private final RoomTypeRepository roomTypeRepository;
    private final RoomTypeMapper roomTypeMapper;

    @Override
    @Transactional
    public RoomTypeResponse create(RoomTypeRequest request) {
        RoomType roomType = roomTypeRepository.save(roomTypeMapper.toEntity(request));
        return roomTypeMapper.toResponse(roomType);
    }

    @Override
    @Transactional(readOnly = true)
    public RoomTypeResponse getById(UUID roomTypeId) {
        return roomTypeMapper.toResponse(requireRoomType(roomTypeId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoomTypeResponse> list(Pageable pageable) {
        return roomTypeRepository.findAll(pageable).map(roomTypeMapper::toResponse);
    }

    @Override
    @Transactional
    public RoomTypeResponse update(UUID roomTypeId, RoomTypeRequest request) {
        RoomType roomType = requireRoomType(roomTypeId);
        roomTypeMapper.updateEntity(roomType, request);
        return roomTypeMapper.toResponse(roomTypeRepository.save(roomType));
    }

    @Override
    @Transactional
    public void delete(UUID roomTypeId) {
        RoomType roomType = requireRoomType(roomTypeId);
        try {
            roomTypeRepository.delete(roomType);
            roomTypeRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateResourceException("Không thể xóa loại phòng đang được sử dụng");
        }
    }

    private RoomType requireRoomType(UUID roomTypeId) {
        return roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại phòng: " + roomTypeId));
    }
}
