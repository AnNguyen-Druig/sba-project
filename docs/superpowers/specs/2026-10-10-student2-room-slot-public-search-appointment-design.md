# Student 2 Room, Slot, Public Search, and Appointment Design

## Goal

Implement the Ready Student 2 functions in `student2_implementation_plan.md` using the existing backend structure, and restore compilability by adding the referenced `Branch`, `Manager`, and `Tenant` entities that are currently missing.

## Current system and assumptions

- The backend is under `be-sba-project`, using Java 21, Spring Boot 4.1.1, Lombok, Spring Data JPA, and package `com.sba.project`.
- Existing request/response DTOs, repositories, and mapper beans are already present. They are the source of truth and will not be recreated. Mappers remain Spring `@Component` beans, matching the project rather than the plan's static-method assumption.
- Controllers use `/api/v1` as their base route. New routes will retain that prefix.
- The project already maps 404, 400, and 409 through `ResourceNotFoundException`, `BusinessException`, and `DuplicateResourceException`, respectively. New work will follow these existing exception types without modifying the global handler.
- The current model diagram in `docs/All_Diagrams.drawio` supplies the missing Branch, Manager, and Tenant fields and relationships. Add only the fields needed by that model and the existing Room/Appointment references; do not add repositories or APIs for those entities.
- The user-approved scope permits adding those missing entities despite the attached plan's earlier assumption that they exist.

## Scope

Implement only Ready items from T1–T10:

- RoomType CRUD (P1-21), Room CRUD and branch-scoped room-code uniqueness (P1-22/23), filtered/paged room search (P1-29), and RoomSlot CRUD (P1-25).
- Public room search and detail (P1-37 room portion, P1-38/39/40), exposing only the fields in `PublicRoomResponse` and applying a sort-property allowlist.
- Appointment same-room/same-instant conflict checking and the stated status transitions (P1-46/47), without appointment creation, rescheduling, or controller endpoints.
- The required RoomType, Room, RoomSlot, and Appointment mapper behavior, implemented by extending the existing mapper beans only where a method is missing.

Do not implement Blocked items, public branch listing, contact information, booking/occupancy rules, authentication/authorization, or schema/config/security changes beyond mapping the three missing domain entities. Do not change existing DTOs, repositories, or unrelated files.

## Behavior and data flow

- Controllers validate request DTOs and delegate to services. Management endpoints use `/api/v1/room-types`, `/api/v1/rooms`, and `/api/v1/room-slots`; public room endpoints use `/api/v1/public/rooms`.
- Services resolve referenced entities, enforce the plan's validation and duplicate checks, and map responses while lazy relations are available in the transaction.
- Room search reuses `RoomRepository.search` with trimmed blank text filters. Public search rejects unsupported sort properties and uses `roomCode` as a stable default sort when none is supplied.
- Public responses contain room, branch display, and room-type display data only; no manager, tenant, contract, or debt fields are returned.
- Appointment status transitions are `PENDING → CONFIRMED | REJECTED | CANCELLED` and `CONFIRMED → COMPLETED | CANCELLED`; terminal states cannot transition. Conflict checks ignore `CANCELLED` and `REJECTED` appointments.

## Error behavior

- Missing entities return 404 via `ResourceNotFoundException`.
- Invalid filters, unsupported sort properties, room-slot reassignment, and unknown appointment statuses return 400 via `BusinessException`.
- Duplicate room codes, foreign-key-blocked deletion, appointment conflicts, and invalid status transitions return 409 via `DuplicateResourceException`.

## Verification

Use the project's disposable H2 test profile. Add focused tests described by T1–T10 and run each task's specified test target, followed by the complete Maven test suite. Do not run against the shared PostgreSQL configuration.

