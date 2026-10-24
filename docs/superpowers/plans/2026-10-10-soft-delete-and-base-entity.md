# Soft Delete and Base Entity Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Preserve Room, RoomSlot, RoomType, and Appointment rows on deletion and give every persisted entity consistent timestamps.

**Architecture:** Add a JPA `BaseEntity` mapped superclass for timestamps and make every entity inherit it. Add per-entity soft-delete flags and Hibernate restrictions to the four requested entities, with service methods marking records deleted and all normal queries hiding them.

**Tech Stack:** Java 21, Spring Boot 4.1.1, Spring Data JPA, Hibernate, PostgreSQL, H2, JUnit 5, Mockito.

**Spec:** `docs/superpowers/specs/2026-10-10-soft-delete-and-base-entity-design.md`

## Global Constraints

- Keep `LocalDateTime` and the existing UTC JDBC timezone configuration.
- Soft-delete only `Room`, `RoomSlot`, `RoomType`, and `Appointment`; other entities only receive shared timestamps.
- Deleting a room marks its room slots deleted in the same transaction. Appointments are retained for history and are not automatically deleted or cancelled.
- Deleting a room type still fails while any room references it.
- Keep `(branch_id, room_code)` reserved after room soft deletion.
- Do not add an Appointment HTTP endpoint, restore workflow, or audit-history log.
- Preserve the user's local POM and configuration changes.

## Review Focus

- Legacy rows must migrate as active and have usable timestamp values: Task 1 and Task 2 verify H2 startup and persistence.
- Hibernate restrictions must apply to both derived and custom room search queries: Task 2 tests repository reads and search.
- Deleting a room must hide its slots without deleting appointments: Task 3 tests cascading flags and retained rows.
- A deleted RoomType may still be referenced by a historical Room: Task 3 tests type deletion remains blocked by any reference.
- A deleted Appointment must not reserve its room/time pair: Task 4 tests conflict checks ignore it.

---

### Task 1: Add shared persistence timestamps

**Files:**
- Create: `be-sba-project/src/main/java/com/sba/project/entity/BaseEntity.java`
- Modify: all 14 JPA entity classes under `be-sba-project/src/main/java/com/sba/project/entity/`
- Test: `be-sba-project/src/test/java/com/sba/project/BaseEntityPersistenceTest.java`

**Interfaces:**
- Produces: inherited `LocalDateTime getCreatedAt()` and `getUpdatedAt()` for all entities.

- [ ] **Step 1: Write a failing persistence test**
  - Add a `@DataJpaTest` that persists a `Branch`, flushes and clears the persistence context, and asserts `createdAt` and `updatedAt` are non-null.
  - Modify and flush the same row, then assert `updatedAt` is not before the original timestamp.
- [ ] **Step 2: Run the test to verify RED**
  - Run: `.\mvnw.cmd -f pom.codex-verification.xml "-Dtest=BaseEntityPersistenceTest" test`
  - Expected: compilation or assertion failure because `Branch` does not yet inherit timestamp fields.
- [ ] **Step 3: Implement the mapped superclass**
  - Create `BaseEntity` with `@MappedSuperclass`, inherited `createdAt`/`updatedAt` fields, `@CreationTimestamp`/`@UpdateTimestamp`, `LocalDateTime`, and safe defaults for existing PostgreSQL/H2 rows.
  - Make all 14 entities extend `BaseEntity`; remove their duplicate timestamp fields and related imports/annotations while retaining existing column names.
- [ ] **Step 4: Run persistence and existing mapper tests**
  - Run: `.\mvnw.cmd -f pom.codex-verification.xml "-Dtest=BaseEntityPersistenceTest,MapperTest" test`
  - Expected: PASS; schema creation succeeds and inherited timestamp properties remain accessible to existing services and DTO mapping.
- [ ] **Step 5: Commit**
  - Commit message: `feat(persistence): add shared entity timestamps`

### Task 2: Add soft-delete mappings and active-only repository reads

**Files:**
- Modify: `Room.java`, `RoomSlot.java`, `RoomType.java`, `Appointment.java`
- Modify: `RoomRepository.java`, `RoomSlotRepository.java`, `RoomTypeRepository.java`, `AppointmentRepository.java`
- Test: `be-sba-project/src/test/java/com/sba/project/SoftDeleteRepositoryTest.java`

**Interfaces:**
- Produces: `boolean isDeleted` state on the four entities; active-only normal repository lookups and searches.
- Produces: `RoomRepository.existsReferencedByAnyRoom(UUID roomTypeId)` using a native reference check that includes archived rooms.

- [ ] **Step 1: Write failing repository tests**
  - Persist active and deleted records for each soft-deletable entity and assert normal `findById`, `findAll`, room search, and room-slot listing expose only active rows.
  - Persist a deleted Appointment and assert its row is excluded from the room/time conflict existence query.
  - Assert the all-room RoomType reference check sees a relation even when the Room is deleted.
- [ ] **Step 2: Run tests to verify RED**
  - Run: `.\mvnw.cmd -f pom.codex-verification.xml "-Dtest=SoftDeleteRepositoryTest" test`
  - Expected: failures show deleted records are currently returned and no soft-delete property/reference method exists.
- [ ] **Step 3: Add soft-delete state and query restrictions**
  - Add `is_deleted`, non-null, default-false fields to Room, RoomSlot, RoomType, and Appointment.
  - Add Hibernate `@SQLRestriction("is_deleted = false")` to those entity classes so inherited and custom repository queries filter deleted rows.
  - Add the native RoomType reference query to preserve FK behavior even when a referencing Room is archived.
- [ ] **Step 4: Run repository tests**
  - Run: `.\mvnw.cmd -f pom.codex-verification.xml "-Dtest=SoftDeleteRepositoryTest" test`
  - Expected: PASS on H2, including custom room search and conflict query filtering.
- [ ] **Step 5: Commit**
  - Commit message: `feat(persistence): add soft-delete filters`

### Task 3: Soft-delete all RoomService resources

**Files:**
- Modify: `be-sba-project/src/main/java/com/sba/project/service/impl/RoomServiceImpl.java`
- Modify: `be-sba-project/src/main/java/com/sba/project/repository/RoomSlotRepository.java`
- Test: `be-sba-project/src/test/java/com/sba/project/service/RoomServiceTest.java`
- Test: `be-sba-project/src/test/java/com/sba/project/service/RoomSlotServiceTest.java`
- Test: `be-sba-project/src/test/java/com/sba/project/service/RoomTypeServiceTest.java`

**Interfaces:**
- Consumes: active-only entity reads and `RoomRepository.existsReferencedByAnyRoom(UUID)` from Task 2.
- Preserves existing public method signatures on `RoomService`.

- [ ] **Step 1: Write failing service tests**
  - `RoomServiceTest`: deleting marks Room deleted, marks its active slots deleted, does not call physical delete, and a second delete returns not found.
  - `RoomSlotServiceTest`: deleting marks a slot deleted and does not call physical delete.
  - `RoomTypeServiceTest`: deleting an unreferenced type marks it deleted; deleting a referenced type raises the existing conflict exception; no physical delete is called.
- [ ] **Step 2: Run tests to verify RED**
  - Run: `.\mvnw.cmd -f pom.codex-verification.xml "-Dtest=RoomServiceTest,RoomSlotServiceTest,RoomTypeServiceTest" test`
  - Expected: assertion failures because services still call repository `delete`/`flush` and don't set deleted flags.
- [ ] **Step 3: Implement RoomService soft-delete behavior**
  - `delete(roomId)`: load active Room, set deleted, mark each active RoomSlot belonging to it deleted, and rely on transaction flush; remove physical delete and FK exception handling.
  - `deleteRoomSlot(roomSlotId)`: load active slot and set deleted; remove physical delete and FK handling.
  - `deleteRoomType(roomTypeId)`: reject when any Room references it, otherwise set deleted; remove physical delete and FK handling.
- [ ] **Step 4: Run focused room-domain tests**
  - Run: `.\mvnw.cmd -f pom.codex-verification.xml "-Dtest=RoomServiceTest,RoomSlotServiceTest,RoomTypeServiceTest,PublicRoomControllerTest,RoomSlotControllerTest,RoomTypeControllerTest" test`
  - Expected: PASS; existing endpoint behavior and response shapes remain unchanged.
- [ ] **Step 5: Commit**
  - Commit message: `feat(rooms): soft-delete room domain records`

### Task 4: Add Appointment soft delete and conflict behavior

**Files:**
- Modify: `be-sba-project/src/main/java/com/sba/project/service/AppointmentService.java`
- Modify: `be-sba-project/src/main/java/com/sba/project/service/impl/AppointmentServiceImpl.java`
- Test: `be-sba-project/src/test/java/com/sba/project/service/AppointmentServiceTest.java`

**Interfaces:**
- Produces: `void delete(UUID appointmentId)` on `AppointmentService`; no controller or HTTP route.

- [ ] **Step 1: Write failing tests**
  - `delete_existing_marksDeleted`: deletes an active Appointment by setting the flag without calling repository `delete`.
  - `delete_missing_throws404`: deleting an absent or already-deleted Appointment raises `ResourceNotFoundException`.
  - `assertNoConflict_deletedAppointmentDoesNotConflict`: service behavior remains backed by repository filtering from Task 2.
- [ ] **Step 2: Run tests to verify RED**
  - Run: `.\mvnw.cmd -f pom.codex-verification.xml "-Dtest=AppointmentServiceTest" test`
  - Expected: compilation failure for the missing delete method and failing soft-delete assertions.
- [ ] **Step 3: Implement appointment delete**
  - Add `delete(UUID)` to `AppointmentService`; load an active Appointment, mark it deleted, and let the transaction persist it.
- [ ] **Step 4: Run appointment tests**
  - Run: `.\mvnw.cmd -f pom.codex-verification.xml "-Dtest=AppointmentServiceTest" test`
  - Expected: PASS; conflict checks ignore deleted rows.
- [ ] **Step 5: Commit**
  - Commit message: `feat(appointments): soft-delete appointments`

### Task 5: Verify schema and complete suite

**Files:**
- Test: all backend tests; no new production files.

- [ ] **Step 1: Run full backend test suite**
  - Run: `.\mvnw.cmd -f pom.codex-verification.xml test`
  - Expected: BUILD SUCCESS; all tests pass with H2 schema creation.
- [ ] **Step 2: Review generated schema behavior and source references**
  - Confirm new timestamp columns use existing column names, existing records receive defaults, soft-delete defaults are false, and no target delete method calls `repository.delete`.
  - Confirm no Appointment controller was added.
- [ ] **Step 3: Commit verification-only fixes if any**
  - Commit message if needed: `fix(persistence): align soft-delete schema defaults`
