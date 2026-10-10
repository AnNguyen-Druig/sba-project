package com.sba.project.service.impl;

import com.sba.project.dto.request.RoomSearchRequest;
import com.sba.project.dto.response.PublicRoomResponse;
import com.sba.project.entity.Room;
import com.sba.project.exception.BusinessException;
import com.sba.project.exception.ResourceNotFoundException;
import com.sba.project.mapper.RoomMapper;
import com.sba.project.repository.RoomRepository;
import com.sba.project.service.PublicRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PublicRoomServiceImpl implements PublicRoomService {

    private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of("referencePrice", "capacity", "roomCode");

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<PublicRoomResponse> search(RoomSearchRequest criteria, Pageable pageable) {
        if (criteria.getMinPrice() != null && criteria.getMaxPrice() != null
                && criteria.getMinPrice().compareTo(criteria.getMaxPrice()) > 0) {
            throw new BusinessException("Giá tối thiểu không được lớn hơn giá tối đa");
        }
        validateSort(pageable.getSort());
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
    public PublicRoomResponse getById(UUID roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng: " + roomId));
        return roomMapper.toPublicResponse(room);
    }

    private void validateSort(Sort sort) {
        for (Sort.Order order : sort) {
            if (!ALLOWED_SORT_PROPERTIES.contains(order.getProperty())) {
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
