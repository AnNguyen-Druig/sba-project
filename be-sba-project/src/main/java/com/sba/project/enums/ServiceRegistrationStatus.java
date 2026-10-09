package com.sba.project.enums;

/**
 * Trạng thái đăng ký/hủy dịch vụ tùy chọn
 */
public enum ServiceRegistrationStatus {
    PENDING,     // Chờ duyệt
    APPROVED,    // Đã duyệt
    REJECTED,    // Từ chối
    CANCELLED    // Đã hủy
}
