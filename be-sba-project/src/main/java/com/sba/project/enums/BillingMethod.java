package com.sba.project.enums;

/**
 * Cách tính phí dịch vụ
 */
public enum BillingMethod {
    BY_METER,       // Theo chỉ số (điện, nước)
    BY_PERSON,      // Theo số người
    BY_ROOM,        // Theo phòng (cố định)
    FIXED,          // Cố định
    PER_USE         // Theo lượt sử dụng
}
