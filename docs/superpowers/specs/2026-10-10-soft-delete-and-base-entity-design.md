# Soft Delete and Base Entity Design

## Goal

Preserve room-domain and appointment records when users remove them, while giving persisted entities consistent creation and update timestamps.

## Approved scope

- Add a JPA `BaseEntity` (`@MappedSuperclass`) with `createdAt` and `updatedAt` timestamps.
- Have all persisted entities extend `BaseEntity`. Move existing timestamp fields from their entities into the base class instead of keeping duplicates.
- Keep `LocalDateTime` and the existing UTC JDBC timezone configuration. Hibernate populates creation and update timestamps on persist/update.
- Add soft-delete state to `Room`, `RoomSlot`, `RoomType`, and `Appointment`; other entities only receive the shared timestamps.
- Change all three delete operations in `RoomService` to mark records deleted. Add a delete operation to `AppointmentService`, which currently has no delete operation or controller.

## Delete behavior and visibility

- New rows begin as not deleted. Deleting an already deleted row is handled as not found by normal service lookups.
- Standard repository reads, room searches, room-slot listings, and appointment-conflict checks exclude soft-deleted records.
- Deleting a room marks its room slots deleted in the same transaction. Appointments are retained for history and are not automatically deleted or cancelled.
- Deleting a room type still fails while any room references it, preserving the current foreign-key protection and avoiding hidden association targets.
- Soft-deleting a room does not free its `(branch_id, room_code)` pair. The existing unique constraint remains unchanged; room codes remain reserved by historical records.
- There is no Appointment controller today. This change adds only the service operation and does not create an HTTP endpoint.
- No restore operation or endpoint is added.

## Persistence and migration constraints

- The application has no migration framework configured and uses Hibernate `ddl-auto: update` against PostgreSQL; tests use H2.
- Added soft-delete columns must be non-null and default to `false`, so existing rows are treated as active when the schema is updated.
- Timestamp columns must be compatible with both databases and existing `LocalDateTime` mappings. Existing records receive a safe database default during schema update; entity lifecycle timestamps govern subsequent writes.
- No physical deletes are issued by these service methods after the change.

## Verification

- Add tests first for soft-delete behavior, active-only reads/searches, slot cascading, room-type reference protection, and appointment conflict exclusion.
- Verify timestamp creation/update behavior and entity mappings.
- Run focused service/repository tests, then the full backend test suite using the existing test profile. Preserve the user's local POM and configuration changes.

## Out of scope

- Soft-deleting entities outside Room, RoomSlot, RoomType, and Appointment.
- Reusing identifiers or room codes from deleted records.
- Adding appointment HTTP endpoints, restore workflows, or a general audit-history log.
