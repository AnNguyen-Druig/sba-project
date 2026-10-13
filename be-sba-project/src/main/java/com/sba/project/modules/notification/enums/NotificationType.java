package com.sba.project.modules.notification.enums;

/**
 * Enumeration of supported notification types across modules.
 */
public enum NotificationType {
    // Appointment events
    APPOINTMENT_CREATED,
    APPOINTMENT_CONFIRMED,
    APPOINTMENT_CANCELLED,
    APPOINTMENT_REJECTED,

    // Booking events
    BOOKING_CREATED,
    BOOKING_APPROVED,
    BOOKING_REJECTED,
    BOOKING_EXPIRED,

    // Deposit events
    DEPOSIT_RECEIVED,
    DEPOSIT_REFUNDED,

    // Contract events
    CONTRACT_CREATED,
    CONTRACT_ACTIVATED,
    CONTRACT_EXPIRING,
    CONTRACT_TERMINATED,

    // Invoice events
    INVOICE_ISSUED,
    INVOICE_DUE_REMINDER,
    INVOICE_OVERDUE,

    // Payment events
    PAYMENT_RECEIVED,
    PAYMENT_CONFIRMED,
    PAYMENT_FAILED,

    // Maintenance events
    MAINTENANCE_CREATED,
    MAINTENANCE_UPDATED,
    MAINTENANCE_COMPLETED,

    // Service & utility cost allocation events
    SERVICE_CHARGE_FINALIZED,
    COST_ALLOCATION_PROPOSED,
    COST_ALLOCATION_PROCESSED,

    // General notifications
    GENERAL
}
