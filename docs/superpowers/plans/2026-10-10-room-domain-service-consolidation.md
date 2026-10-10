# Room Domain Service Consolidation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Consolidate room, room type, room slot, and public room operations behind one `RoomService` and `RoomServiceImpl` without changing HTTP routes or payloads.

**Architecture:** Keep the four controllers as separate route boundaries and make each inject `RoomService`. Add resource-specific method names to avoid Java signature collisions, move each existing implementation into `RoomServiceImpl`, then delete the now-unused service interfaces and implementations.

**Tech Stack:** Java 21, Spring Boot 4.1.1, Spring Data JPA, JUnit 5, Mockito, MockMvc.

**Spec:** `docs/superpowers/specs/2026-10-10-room-domain-service-consolidation-design.md`

## Global Constraints

- Preserve all current HTTP paths and JSON payloads.
- Preserve validation, transaction boundaries, error mappings, sorting, room-code uniqueness, room-slot ownership checks, public-field filtering, and room-type/slot CRUD behavior.
- Keep `AppointmentService`, persistence entities, repositories, and mappers unchanged.
- Do not change database schema or add new behavior.
- Preserve unrelated local changes, including the current enum refactor and local configuration edits.

## Review Focus

- Public search must keep the default `roomCode` sort and reject unsupported sort properties; covered by `PublicRoomServiceTest` and `PublicRoomControllerTest` in Task 1.
- Public room details must still omit internal fields; covered by `getById_returnsOnlyPublicFields` and `detail_jsonHasNoManagerId` in Task 1.
- Slot updates must reject moving a slot to another room; covered by the existing reassignment case in `RoomSlotServiceTest` in Task 2.
- Missing/referenced room types must retain 404/409 behavior; covered by `RoomTypeServiceTest` in Task 3.
- Existing route bindings and request validation must remain intact; the controller tests for each group are retained in Tasks 1–3.

---

### Task 1: Fold public room operations into RoomService

**Files:**
- Modify: `be-sba-project/src/main/java/com/sba/project/service/RoomService.java`
- Modify: `be-sba-project/src/main/java/com/sba/project/service/impl/RoomServiceImpl.java`
- Modify: `be-sba-project/src/main/java/com/sba/project/controller/PublicRoomController.java`
- Delete: `be-sba-project/src/main/java/com/sba/project/service/PublicRoomService.java`
- Delete: `be-sba-project/src/main/java/com/sba/project/service/impl/PublicRoomServiceImpl.java`
- Modify: `be-sba-project/src/test/java/com/sba/project/service/PublicRoomServiceTest.java`
- Modify: `be-sba-project/src/test/java/com/sba/project/controller/PublicRoomControllerTest.java`

**Interfaces:**
- Consumes: Existing `RoomService` operations and the current public room repository/mapping behavior.
- Produces: `Page<PublicRoomResponse> searchPublicRooms(RoomSearchRequest criteria, Pageable pageable)` and `PublicRoomResponse getPublicRoomById(UUID roomId)` on `RoomService`.

- [ ] **Step 1: Update existing public service/controller tests to target the shared interface.** In `PublicRoomServiceTest`, construct `RoomServiceImpl` with the existing room repository/mapper plus the required constructor dependencies, and call `searchPublicRooms`/`getPublicRoomById`. In `PublicRoomControllerTest`, mock `RoomService` and verify the same methods while keeping the current `/api/v1/public/rooms` requests and assertions.
- [ ] **Step 2: Run the focused public room tests and confirm they fail because the shared methods are not implemented.**

Run: `cd be-sba-project; .\mvnw.cmd "-Dtest=PublicRoomServiceTest,PublicRoomControllerTest" test`

Expected: compilation/test failure because `RoomService` lacks the public methods.

- [ ] **Step 3: Add the two public methods to `RoomService` and move the existing `PublicRoomServiceImpl` logic into `RoomServiceImpl`.** Preserve validation, allowed sort fields, default sorting, public response mapping, and 404 behavior. Switch `PublicRoomController` to inject `RoomService` without changing routes.
- [ ] **Step 4: Remove `PublicRoomService` and `PublicRoomServiceImpl`, then rerun the focused public tests.**

Run: `cd be-sba-project; .\mvnw.cmd "-Dtest=PublicRoomServiceTest,PublicRoomControllerTest" test`

Expected: both test classes pass.

- [ ] **Step 5: Commit Task 1.**

### Task 2: Fold room slot operations into RoomService

**Files:**
- Modify: `be-sba-project/src/main/java/com/sba/project/service/RoomService.java`
- Modify: `be-sba-project/src/main/java/com/sba/project/service/impl/RoomServiceImpl.java`
- Modify: `be-sba-project/src/main/java/com/sba/project/controller/RoomSlotController.java`
- Delete: `be-sba-project/src/main/java/com/sba/project/service/RoomSlotService.java`
- Delete: `be-sba-project/src/main/java/com/sba/project/service/impl/RoomSlotServiceImpl.java`
- Modify: `be-sba-project/src/test/java/com/sba/project/service/RoomSlotServiceTest.java`
- Modify: `be-sba-project/src/test/java/com/sba/project/controller/RoomSlotControllerTest.java`

**Interfaces:**
- Consumes: Task 1 `RoomService` and `RoomServiceImpl`.
- Produces: `createRoomSlot(RoomSlotRequest)`, `getRoomSlotById(UUID)`, `listRoomSlotsByRoom(UUID)`, `updateRoomSlot(UUID, RoomSlotRequest)`, and `deleteRoomSlot(UUID)` on `RoomService`.

- [ ] **Step 1: Update room slot service/controller tests to construct or mock `RoomService` and call the resource-specific methods. Keep all route, validation, ownership, and error assertions.**
- [ ] **Step 2: Run the focused room slot tests and confirm they fail because the shared methods are absent.**

Run: `cd be-sba-project; .\mvnw.cmd "-Dtest=RoomSlotServiceTest,RoomSlotControllerTest" test`

Expected: compilation/test failure because `RoomService` lacks the slot methods.

- [ ] **Step 3: Move the current slot service operations into `RoomServiceImpl` and update `RoomSlotController` to inject `RoomService`.** Preserve room lookup, transactional behavior, room ownership checks, and exception mapping.
- [ ] **Step 4: Remove `RoomSlotService` and `RoomSlotServiceImpl`, then rerun the focused room slot tests.**

Run: `cd be-sba-project; .\mvnw.cmd "-Dtest=RoomSlotServiceTest,RoomSlotControllerTest" test`

Expected: both test classes pass.

- [ ] **Step 5: Commit Task 2.**

### Task 3: Fold room type operations into RoomService

**Files:**
- Modify: `be-sba-project/src/main/java/com/sba/project/service/RoomService.java`
- Modify: `be-sba-project/src/main/java/com/sba/project/service/impl/RoomServiceImpl.java`
- Modify: `be-sba-project/src/main/java/com/sba/project/controller/RoomTypeController.java`
- Delete: `be-sba-project/src/main/java/com/sba/project/service/RoomTypeService.java`
- Delete: `be-sba-project/src/main/java/com/sba/project/service/impl/RoomTypeServiceImpl.java`
- Modify: `be-sba-project/src/test/java/com/sba/project/service/RoomTypeServiceTest.java`
- Modify: `be-sba-project/src/test/java/com/sba/project/controller/RoomTypeControllerTest.java`

**Interfaces:**
- Consumes: Task 1 and Task 2 `RoomService` operations.
- Produces: `createRoomType(RoomTypeRequest)`, `getRoomTypeById(UUID)`, `listRoomTypes(Pageable)`, `updateRoomType(UUID, RoomTypeRequest)`, and `deleteRoomType(UUID)` on `RoomService`.

- [ ] **Step 1: Update room type service/controller tests to construct or mock `RoomService` and call the resource-specific methods. Keep existing validation, pagination, 404, and referenced-delete 409 assertions.**
- [ ] **Step 2: Run the focused room type tests and confirm they fail because the shared methods are absent.**

Run: `cd be-sba-project; .\mvnw.cmd "-Dtest=RoomTypeServiceTest,RoomTypeControllerTest" test`

Expected: compilation/test failure because `RoomService` lacks the type methods.

- [ ] **Step 3: Move the current room type operations into `RoomServiceImpl` and update `RoomTypeController` to inject `RoomService`.** Preserve duplicate checks, paging, transactions, and exception mapping.
- [ ] **Step 4: Remove `RoomTypeService` and `RoomTypeServiceImpl`, then rerun the focused room type tests.**

Run: `cd be-sba-project; .\mvnw.cmd "-Dtest=RoomTypeServiceTest,RoomTypeControllerTest" test`

Expected: both test classes pass.

- [ ] **Step 5: Run the full suite and inspect the final diff for leftover service references.**

Run: `cd be-sba-project; .\mvnw.cmd test`

Expected: all tests pass; `rg -n "com\\.sba\\.project\\.service\\.(PublicRoomService|RoomSlotService|RoomTypeService)(;|$)|\\b(PublicRoomServiceImpl|RoomSlotServiceImpl|RoomTypeServiceImpl)\\b" src/main/java src/test/java` returns no references to the deleted service types.

- [ ] **Step 6: Commit Task 3.**
