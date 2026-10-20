# Rental JPA mapping checklist

Source of truth: unchanged deployed DDL and [catalog](../database/rental/schema-catalog.json). Live Supabase validation: NOT EXECUTED.

| PostgreSQL table | Java entity | Package | Phase | Columns | BaseEntity | Mapping status |
|---|---|---|---|---:|---|---|
| appointments | [Appointment](../../be-sba-project/src/main/java/com/sba/project/modules/leasing/entity/Appointment.java) | `com.sba.project.modules.leasing.entity` | 1 | 12 | Yes | PASS metadata; live DB not executed |
| audit_logs | [AuditLog](../../be-sba-project/src/main/java/com/sba/project/common/audit/entity/AuditLog.java) | `com.sba.project.common.audit.entity` | 1 | 9 | No | PASS metadata; live DB not executed |
| bookings | [Booking](../../be-sba-project/src/main/java/com/sba/project/modules/leasing/entity/Booking.java) | `com.sba.project.modules.leasing.entity` | 1 | 21 | Yes | PASS metadata; live DB not executed |
| branch_policies | [BranchPolicy](../../be-sba-project/src/main/java/com/sba/project/modules/property/entity/BranchPolicy.java) | `com.sba.project.modules.property.entity` | 1 | 14 | No | PASS metadata; live DB not executed |
| branches | [Branch](../../be-sba-project/src/main/java/com/sba/project/modules/property/entity/Branch.java) | `com.sba.project.modules.property.entity` | 1 | 24 | Yes | PASS metadata; live DB not executed |
| contract_amendments | [ContractAmendment](../../be-sba-project/src/main/java/com/sba/project/modules/leasing/entity/ContractAmendment.java) | `com.sba.project.modules.leasing.entity` | 2 | 19 | No | PASS metadata; live DB not executed |
| contract_services | [ContractService](../../be-sba-project/src/main/java/com/sba/project/modules/utilities/entity/ContractService.java) | `com.sba.project.modules.utilities.entity` | 1 | 13 | No | PASS metadata; live DB not executed |
| contract_tenants | [ContractTenant](../../be-sba-project/src/main/java/com/sba/project/modules/leasing/entity/ContractTenant.java) | `com.sba.project.modules.leasing.entity` | 1 | 37 | Yes | PASS metadata; live DB not executed |
| contracts | [Contract](../../be-sba-project/src/main/java/com/sba/project/modules/leasing/entity/Contract.java) | `com.sba.project.modules.leasing.entity` | 1 | 22 | Yes | PASS metadata; live DB not executed |
| cost_allocation_proposals | [CostAllocationProposal](../../be-sba-project/src/main/java/com/sba/project/modules/utilities/entity/CostAllocationProposal.java) | `com.sba.project.modules.utilities.entity` | 1 | 11 | No | PASS metadata; live DB not executed |
| furniture_warranties | [FurnitureWarranty](../../be-sba-project/src/main/java/com/sba/project/modules/maintenance/entity/FurnitureWarranty.java) | `com.sba.project.modules.maintenance.entity` | 1 | 7 | No | PASS metadata; live DB not executed |
| furnitures | [Furniture](../../be-sba-project/src/main/java/com/sba/project/modules/maintenance/entity/Furniture.java) | `com.sba.project.modules.maintenance.entity` | 1 | 10 | No | PASS metadata; live DB not executed |
| invoice_items | [InvoiceItem](../../be-sba-project/src/main/java/com/sba/project/modules/billing/entity/InvoiceItem.java) | `com.sba.project.modules.billing.entity` | 1 | 19 | No | PASS metadata; live DB not executed |
| invoices | [Invoice](../../be-sba-project/src/main/java/com/sba/project/modules/billing/entity/Invoice.java) | `com.sba.project.modules.billing.entity` | 1 | 19 | No | PASS metadata; live DB not executed |
| notifications | [Notification](../../be-sba-project/src/main/java/com/sba/project/modules/notification/entity/Notification.java) | `com.sba.project.modules.notification.entity` | 1 | 17 | No | PASS metadata; live DB not executed |
| owner_managers | [OwnerManager](../../be-sba-project/src/main/java/com/sba/project/modules/identity/entity/OwnerManager.java) | `com.sba.project.modules.identity.entity` | 1 | 4 | No | PASS metadata; live DB not executed |
| payment_webhook_events | [PaymentWebhookEvent](../../be-sba-project/src/main/java/com/sba/project/modules/payment/entity/PaymentWebhookEvent.java) | `com.sba.project.modules.payment.entity` | 1 | 12 | No | PASS metadata; live DB not executed |
| payments | [Payment](../../be-sba-project/src/main/java/com/sba/project/modules/payment/entity/Payment.java) | `com.sba.project.modules.payment.entity` | 1 | 28 | No | PASS metadata; live DB not executed |
| report_records | [ReportRecord](../../be-sba-project/src/main/java/com/sba/project/modules/maintenance/entity/ReportRecord.java) | `com.sba.project.modules.maintenance.entity` | 1 | 16 | No | PASS metadata; live DB not executed |
| report_requests | [ReportRequest](../../be-sba-project/src/main/java/com/sba/project/modules/maintenance/entity/ReportRequest.java) | `com.sba.project.modules.maintenance.entity` | 1 | 18 | Yes | PASS metadata; live DB not executed |
| report_requests_furnitures | [ReportRequestFurniture](../../be-sba-project/src/main/java/com/sba/project/modules/maintenance/entity/ReportRequestFurniture.java) | `com.sba.project.modules.maintenance.entity` | 1 | 5 | No | PASS metadata; live DB not executed |
| report_requests_services | [ReportRequestService](../../be-sba-project/src/main/java/com/sba/project/modules/maintenance/entity/ReportRequestService.java) | `com.sba.project.modules.maintenance.entity` | 1 | 5 | No | PASS metadata; live DB not executed |
| resource_allocations | [ResourceAllocation](../../be-sba-project/src/main/java/com/sba/project/modules/leasing/entity/ResourceAllocation.java) | `com.sba.project.modules.leasing.entity` | 1 | 15 | No | PASS metadata; live DB not executed |
| roles | [Role](../../be-sba-project/src/main/java/com/sba/project/modules/identity/entity/Role.java) | `com.sba.project.modules.identity.entity` | 1 | 2 | No | PASS metadata; live DB not executed |
| room_service_charges | [RoomServiceCharge](../../be-sba-project/src/main/java/com/sba/project/modules/utilities/entity/RoomServiceCharge.java) | `com.sba.project.modules.utilities.entity` | 1 | 19 | No | PASS metadata; live DB not executed |
| room_services | [RoomService](../../be-sba-project/src/main/java/com/sba/project/modules/utilities/entity/RoomService.java) | `com.sba.project.modules.utilities.entity` | 1 | 9 | No | PASS metadata; live DB not executed |
| room_slot_occupancies | [RoomSlotOccupancy](../../be-sba-project/src/main/java/com/sba/project/modules/leasing/entity/RoomSlotOccupancy.java) | `com.sba.project.modules.leasing.entity` | 1 | 7 | No | PASS metadata; live DB not executed |
| room_slots | [RoomSlot](../../be-sba-project/src/main/java/com/sba/project/modules/property/entity/RoomSlot.java) | `com.sba.project.modules.property.entity` | 1 | 5 | No | PASS metadata; live DB not executed |
| room_types | [RoomType](../../be-sba-project/src/main/java/com/sba/project/modules/property/entity/RoomType.java) | `com.sba.project.modules.property.entity` | 1 | 7 | No | PASS metadata; live DB not executed |
| rooms | [Room](../../be-sba-project/src/main/java/com/sba/project/modules/property/entity/Room.java) | `com.sba.project.modules.property.entity` | 1 | 17 | Yes | PASS metadata; live DB not executed |
| services | [ServiceCatalog](../../be-sba-project/src/main/java/com/sba/project/modules/utilities/entity/ServiceCatalog.java) | `com.sba.project.modules.utilities.entity` | 1 | 10 | No | PASS metadata; live DB not executed |
| threads | [Thread](../../be-sba-project/src/main/java/com/sba/project/modules/community/entity/Thread.java) | `com.sba.project.modules.community.entity` | 3 | 13 | Yes | PASS metadata; live DB not executed |
| user_roles | [UserRole](../../be-sba-project/src/main/java/com/sba/project/modules/identity/entity/UserRole.java) | `com.sba.project.modules.identity.entity` | 1 | 4 | No | PASS metadata; live DB not executed |
| users | [User](../../be-sba-project/src/main/java/com/sba/project/modules/identity/entity/User.java) | `com.sba.project.modules.identity.entity` | 1 | 33 | Yes | PASS metadata; live DB not executed |
