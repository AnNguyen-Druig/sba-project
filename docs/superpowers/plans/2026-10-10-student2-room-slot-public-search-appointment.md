# Student 2 Room, Slot, Public Search, and Appointment Implementation Plan

> **For agentic workers:** This plan is executed natively in this session, one task at a time. Checkboxes track each task. Preserve all pre-existing uncommitted work.

**Goal:** Implement the Ready Student 2 room, room-slot, public-room, and appointment functions in the existing backend, and add the missing referenced domain entities so the backend compiles.

**Architecture:** Follow the existing `com.sba.project` Spring Boot structure: controller → service interface/implementation → repository, with injected mapper components and existing exception types. Add only the missing `Branch`, `Manager`, and `Tenant` JPA entities from the repository's data model; retain `/api/v1` route prefixes.

**Tech Stack:** Java 21, Spring Boot 4.1.1, Spring MVC, Spring Data JPA, Lombok, Maven, H2 test profile.

**Spec:** `docs/superpowers/specs/2026-10-10-student2-room-slot-public-search-appointment-design.md`

## Global Constraints

- Implement only Ready requirements T1–T10 in `D:/Downloads/student2_implementation_plan.md`.
- Use package `com.sba.project`, existing Lombok DTO classes, injected mapper beans, service interfaces with `service/impl` implementations, and `/api/v1` routes.
- Do not alter existing DTOs, repositories, mapper style, global exception handler, configuration, dependencies, security, or database migration/schema files.
- Use existing exceptions: `ResourceNotFoundException` for 404, `BusinessException` for 400, and `DuplicateResourceException` for 409.
- Preserve existing user changes; add only the files listed in each task and avoid staging unrelated files.
- Use H2 for verification; never run against shared PostgreSQL.

## Review Focus

- Same `roomCode` in a different branch remains valid; duplicate within one branch yields 409, including the database-race path. Test in RoomServiceTest.
- Blank address/status filters become null and `minPrice > maxPrice` yields 400. Test in RoomServiceTest and PublicRoomServiceTest.
- Public sort accepts only `referencePrice`, `capacity`, and `roomCode`, defaults to `roomCode`, and the response contains no manager or tenant data. Test in PublicRoomServiceTest and PublicRoomControllerTest.
- A RoomSlot cannot be reassigned to a different Room. Test in RoomSlotServiceTest.
- Appointment conflicts exclude only `CANCELLED` and `REJECTED`; invalid transitions and terminal statuses yield 409. Test in AppointmentServiceTest.

---

### Task 1: Add missing referenced entities

**Files:**
- Create: `be-sba-project/src/main/java/com/sba/project/entity/Manager.java`
- Create: `be-sba-project/src/main/java/com/sba/project/entity/Branch.java`
- Create: `be-sba-project/src/main/java/com/sba/project/entity/Tenant.java`

**Interfaces:**
- `Manager`: UUID `managerId`; `fullName`, `phone`, `email`, and `status` fields from `docs/All_Diagrams.drawio`.
- `Branch`: UUID `branchId`; `Manager manager`; `branchCode`, `branchName`, `address`, `description`, and `status` fields from the diagram.
- `Tenant`: UUID `tenantId`; `fullName`, `LocalDate dateOfBirth`, `gender`, `phone`, `email`, `identityDocument`, and `status` fields from the diagram.
- Use table/column names and Lombok/JPA conventions matching the existing entities. Do not add inverse collections or repositories.

- [ ] Add the three entities with generated UUID identifiers and the fields/relationship above.
- [ ] Run `be-sba-project/mvnw.cmd -f be-sba-project/pom.xml -DskipTests compile`.
- [ ] Confirm `Room`, `Appointment`, and their existing mappers compile without changing them.

### Task 2: Verify entity mappers

**Files:**
- Create: `be-sba-project/src/test/java/com/sba/project/mapper/MapperTest.java`
- Modify only if a specified mapper behavior is missing: `be-sba-project/src/main/java/com/sba/project/mapper/{RoomTypeMapper,RoomMapper,RoomSlotMapper,AppointmentMapper}.java`

**Interfaces:** Existing mapper components expose conversions for RoomType, Room, RoomSlot, and Appointment. Keep their instance methods and existing extra methods; do not convert them to static methods or remove pre-existing code.

- [ ] Add unit tests for request→entity, update, response, and public-room response field mappings; assert generated IDs are not set by request mapping.
- [ ] Run `cd be-sba-project; .\mvnw.cmd -Dtest=MapperTest test`.
- [ ] Make the smallest mapper-only correction if a specified mapping is absent, then rerun the focused test.

### Task 3: RoomType service

**Files:**
- Create: `be-sba-project/src/main/java/com/sba/project/service/RoomTypeService.java`
- Create: `be-sba-project/src/main/java/com/sba/project/service/impl/RoomTypeServiceImpl.java`
- Create: `be-sba-project/src/test/java/com/sba/project/service/RoomTypeServiceTest.java`

**Interfaces:** `create(RoomTypeRequest)`, `getById(UUID)`, `list(Pageable)`, `update(UUID, RoomTypeRequest)`, and `delete(UUID)` return the types in the attached plan. Inject `RoomTypeRepository` and `RoomTypeMapper`.

- [ ] Test create, get missing (404), update, delete referenced (409), and delete missing (404).
- [ ] Implement CRUD; for deletion call `deleteById` and `flush` and map `DataIntegrityViolationException` to 409.
- [ ] Run `cd be-sba-project; .\mvnw.cmd -Dtest=RoomTypeServiceTest test`.

### Task 4: RoomType controller

**Files:**
- Create: `be-sba-project/src/main/java/com/sba/project/controller/RoomTypeController.java`
- Create: `be-sba-project/src/test/java/com/sba/project/controller/RoomTypeControllerTest.java`

**Interfaces:** `/api/v1/room-types`: POST create (201), GET by ID/list (200), PUT update (200), DELETE (204). Inject `RoomTypeService`; validate request bodies.

- [ ] Test success responses, blank name and non-positive capacity validation, missing ID, and delete 204.
- [ ] Implement delegation without adding security annotations.
- [ ] Run `cd be-sba-project; .\mvnw.cmd -Dtest=RoomTypeControllerTest test`.

### Task 5: Room service

**Files:**
- Create: `be-sba-project/src/main/java/com/sba/project/service/RoomService.java`
- Create: `be-sba-project/src/main/java/com/sba/project/service/impl/RoomServiceImpl.java`
- Create: `be-sba-project/src/test/java/com/sba/project/service/RoomServiceTest.java`

**Interfaces:** `create(RoomRequest)`, `getById(UUID)`, `search(RoomSearchRequest, Pageable)`, `update(UUID, RoomRequest)`, and `delete(UUID)`. Inject `RoomRepository`, `RoomTypeRepository`, `EntityManager`, and `RoomMapper`.

- [ ] Test branch-scoped uniqueness, references missing (404), update uniqueness, normalized search filters, exact repository search argument order, price bounds (400), and FK-blocked delete (409).
- [ ] Resolve Branch and Manager with `EntityManager.find`; resolve RoomType through its repository. Check uniqueness on create and only when branch or code changes on update; translate a save race to 409.
- [ ] Reuse `RoomRepository.search`; pass blank address/status as null. Delete with `deleteById` and `flush`, translating FK violations to 409.
- [ ] Run `cd be-sba-project; .\mvnw.cmd -Dtest=RoomServiceTest test`.

### Task 6: Room controller

**Files:**
- Create: `be-sba-project/src/main/java/com/sba/project/controller/RoomController.java`
- Create: `be-sba-project/src/test/java/com/sba/project/controller/RoomControllerTest.java`

**Interfaces:** `/api/v1/rooms`: POST create (201), GET by ID/search (200), PUT update (200), DELETE (204). Bind `RoomSearchRequest` and `Pageable`; validate request bodies.

- [ ] Test create and request validation, duplicate response, and search query binding.
- [ ] Implement delegation to `RoomService` only.
- [ ] Run `cd be-sba-project; .\mvnw.cmd -Dtest=RoomControllerTest test`.

### Task 7: RoomSlot service

**Files:**
- Create: `be-sba-project/src/main/java/com/sba/project/service/RoomSlotService.java`
- Create: `be-sba-project/src/main/java/com/sba/project/service/impl/RoomSlotServiceImpl.java`
- Create: `be-sba-project/src/test/java/com/sba/project/service/RoomSlotServiceTest.java`

**Interfaces:** `create(RoomSlotRequest)`, `getById(UUID)`, `listByRoom(UUID)`, `update(UUID, RoomSlotRequest)`, and `delete(UUID)`. Inject `RoomSlotRepository`, `RoomRepository`, and `RoomSlotMapper`.

- [ ] Test create/list missing Room (404), update reassignment (400), normal update, and FK-blocked delete (409).
- [ ] Verify the Room before listing; reject a changed `roomId`; do not add slot-code uniqueness.
- [ ] Run `cd be-sba-project; .\mvnw.cmd -Dtest=RoomSlotServiceTest test`.

### Task 8: RoomSlot controller

**Files:**
- Create: `be-sba-project/src/main/java/com/sba/project/controller/RoomSlotController.java`
- Create: `be-sba-project/src/test/java/com/sba/project/controller/RoomSlotControllerTest.java`

**Interfaces:** Under `/api/v1`, POST `/room-slots` (201), GET `/room-slots/{roomSlotId}` (200), GET `/rooms/{roomId}/slots` (200), PUT `/room-slots/{roomSlotId}` (200), DELETE `/room-slots/{roomSlotId}` (204).

- [ ] Test create validation, list, and delete responses.
- [ ] Implement delegation without security annotations.
- [ ] Run `cd be-sba-project; .\mvnw.cmd -Dtest=RoomSlotControllerTest test`.

### Task 9: Public room service

**Files:**
- Create: `be-sba-project/src/main/java/com/sba/project/service/PublicRoomService.java`
- Create: `be-sba-project/src/main/java/com/sba/project/service/impl/PublicRoomServiceImpl.java`
- Create: `be-sba-project/src/test/java/com/sba/project/service/PublicRoomServiceTest.java`

**Interfaces:** `search(RoomSearchRequest, Pageable)` returns `Page<PublicRoomResponse>`; `getById(UUID)` returns `PublicRoomResponse`. Inject `RoomRepository` and `RoomMapper`.

- [ ] Test default sort, sort allowlist, invalid price range, 404 detail, and public-only mapped fields.
- [ ] Reuse Room search, normalize blank text filters, reject any sort property outside `referencePrice`, `capacity`, `roomCode`, and apply `roomCode` when unsorted.
- [ ] Run `cd be-sba-project; .\mvnw.cmd -Dtest=PublicRoomServiceTest test`.

### Task 10: Public room controller

**Files:**
- Create: `be-sba-project/src/main/java/com/sba/project/controller/PublicRoomController.java`
- Create: `be-sba-project/src/test/java/com/sba/project/controller/PublicRoomControllerTest.java`

**Interfaces:** `/api/v1/public/rooms`: GET search and GET `/{roomId}`, both 200. Inject `PublicRoomService`; do not change security configuration.

- [ ] Test paged search, invalid sort (400), missing detail (404), and JSON absence of manager/tenant/contract/debt data.
- [ ] Implement delegation and match existing controller/test conventions.
- [ ] Run `cd be-sba-project; .\mvnw.cmd -Dtest=PublicRoomControllerTest test`.

### Task 11: Appointment status and conflict service

**Files:**
- Create: `be-sba-project/src/main/java/com/sba/project/service/AppointmentStatus.java`
- Create: `be-sba-project/src/main/java/com/sba/project/service/AppointmentService.java`
- Create: `be-sba-project/src/main/java/com/sba/project/service/impl/AppointmentServiceImpl.java`
- Create: `be-sba-project/src/test/java/com/sba/project/service/AppointmentServiceTest.java`

**Interfaces:** `AppointmentStatus` string constants `PENDING`, `CONFIRMED`, `COMPLETED`, `CANCELLED`, `REJECTED`; `changeStatus(UUID, AppointmentStatusRequest)` returns `AppointmentResponse`; `assertNoConflict(UUID, LocalDateTime)` returns void. Inject `AppointmentRepository` and `AppointmentMapper`.

- [ ] Test pending→confirmed, invalid transitions, terminal states, unknown status (400), missing appointment (404), and conflict inclusion/exclusion.
- [ ] Implement only transitions `PENDING → CONFIRMED | REJECTED | CANCELLED` and `CONFIRMED → COMPLETED | CANCELLED`; ignore `request.note()`; exclude cancelled and rejected statuses from exact-time conflict checks.
- [ ] Run `cd be-sba-project; .\mvnw.cmd -Dtest=AppointmentServiceTest test`.

### Task 12: Full verification

**Files:** No source changes unless a preceding task's verification exposes an in-scope defect.

- [ ] Run `cd be-sba-project; .\mvnw.cmd test` using the H2 test profile.
- [ ] Review the final diff and ensure existing user changes remain untouched and no blocked feature was added.
