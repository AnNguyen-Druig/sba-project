package com.sba.project.service.impl;

import com.sba.project.dto.request.RoomSlotRequest;
import com.sba.project.dto.response.RoomSlotResponse;
import com.sba.project.entity.Room;
import com.sba.project.entity.RoomSlot;
import com.sba.project.exception.BusinessException;
import com.sba.project.exception.DuplicateResourceException;
import com.sba.project.exception.ResourceNotFoundException;
import com.sba.project.mapper.RoomSlotMapper;
import com.sba.project.repository.RoomRepository;
import com.sba.project.repository.RoomSlotRepository;
import com.sba.project.service.RoomSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomSlotServiceImpl implements RoomSlotService {

    private final RoomSlotRepository roomSlotRepository;
    private final RoomRepository roomRepository;
    private final RoomSlotMapper roomSlotMapper;

    @Override
    @Transactional
    public RoomSlotResponse create(RoomSlotRequest request) {
        Room room = requireRoom(request.getRoomId());
        return roomSlotMapper.toResponse(roomSlotRepository.save(roomSlotMapper.toEntity(request, room)));
    }

    @Override
    @Transactional(readOnly = true)
    public RoomSlotResponse getById(UUID roomSlotId) {
        return roomSlotMapper.toResponse(requireRoomSlot(roomSlotId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomSlotResponse> listByRoom(UUID roomId) {
        requireRoom(roomId);
        return roomSlotRepository.findByRoom_RoomId(roomId).stream()
                .map(roomSlotMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public RoomSlotResponse update(UUID roomSlotId, RoomSlotRequest request) {
        RoomSlot roomSlot = requireRoomSlot(roomSlotId);
        if (!roomSlot.getRoom().getRoomId().equals(request.getRoomId())) {
            throw new BusinessException("Không thể chuyển slot sang phòng khác");
        }
        roomSlotMapper.updateEntity(roomSlot, request, roomSlot.getRoom());
        return roomSlotMapper.toResponse(roomSlotRepository.save(roomSlot));
    }

    @Override
    @Transactional
    public void delete(UUID roomSlotId) {
        RoomSlot roomSlot = requireRoomSlot(roomSlotId);
        try {
            roomSlotRepository.delete(roomSlot);
            roomSlotRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateResourceException("Không thể xóa slot đang được sử dụng");
        }
    }

    private RoomSlot requireRoomSlot(UUID roomSlotId) {
        return roomSlotRepository.findById(roomSlotId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy slot: " + roomSlotId));
    }

    private Room requireRoom(UUID roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng: " + roomId));
    }
}
