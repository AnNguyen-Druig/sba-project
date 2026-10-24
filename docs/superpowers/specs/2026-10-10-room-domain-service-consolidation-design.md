# Room Domain Service Consolidation

## Goal

Use one `RoomService` boundary for room, room type, room slot, and public room operations. These operations manage the same room domain and should not require separate service interfaces and implementations.

## Approved approach

- Keep one `RoomService` interface and one `RoomServiceImpl`.
- Move the current `RoomTypeServiceImpl`, `RoomSlotServiceImpl`, and `PublicRoomServiceImpl` operations into `RoomServiceImpl`.
- Remove the `RoomTypeService`, `RoomSlotService`, and `PublicRoomService` interfaces and their implementations after callers are migrated.
- Keep the four controllers separate. Each controller injects `RoomService`, preserving the current separation between management routes and public routes.
- Keep `AppointmentService` separate because appointment transitions and booking conflicts are a distinct workflow.

## Service method names

Use resource-specific names where signatures would otherwise collide:

- Room: `createRoom`, `getRoomById`, `searchRooms`, `updateRoom`, `deleteRoom`.
- Room type: `createRoomType`, `getRoomTypeById`, `listRoomTypes`, `updateRoomType`, `deleteRoomType`.
- Room slot: `createRoomSlot`, `getRoomSlotById`, `listRoomSlotsByRoom`, `updateRoomSlot`, `deleteRoomSlot`.
- Public room view: `searchPublicRooms`, `getPublicRoomById`.

Controllers delegate to the matching methods. No route or request/response type changes are part of this work.

## Behavior and compatibility

- Preserve validation, transaction boundaries, error mappings, sorting, room-code uniqueness, room-slot ownership checks, public-field filtering, and room-type/slot CRUD behavior from the existing implementations.
- Preserve all current HTTP paths and JSON payloads.
- Keep `AppointmentService` and all persistence entities, repositories, and mappers unchanged.
- Do not change database schema or add new behavior.

## Verification

- Update the existing service and controller tests to use `RoomService` and the resource-specific methods.
- Run the focused room service/controller test classes and the full Maven test suite if the working Maven configuration parses. The currently modified `pom.xml` has a stray `+mnb` line; it must be resolved before Maven can run.
- Preserve unrelated local changes, including the current enum refactor and local configuration edits.
