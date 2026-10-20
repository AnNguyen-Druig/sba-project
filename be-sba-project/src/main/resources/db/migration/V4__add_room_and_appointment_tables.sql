-- Add the Student 2 mappings merged after V3. Existing rows and IDs are preserved.
-- Flyway owns the transaction. A pre-existing conflicting table must fail, not be skipped.
SET LOCAL lock_timeout = '10s';
SET LOCAL statement_timeout = '120s';

CREATE TABLE public.managers (
    manager_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name varchar(255),
    phone varchar(255),
    email varchar(255),
    status varchar(255)
);
CREATE TABLE public.tenants (
    tenant_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name varchar(255),
    date_of_birth date,
    gender varchar(255),
    phone varchar(255),
    email varchar(255),
    identity_document varchar(255),
    status varchar(255)
);
CREATE TABLE public.room_types (
    room_type_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    type_name varchar(255) NOT NULL,
    description text,
    default_capacity integer NOT NULL,
    features text
);
ALTER TABLE public.branches ADD COLUMN manager_id uuid;
ALTER TABLE public.branches ADD CONSTRAINT fk_branches_manager_profile
    FOREIGN KEY (manager_id) REFERENCES public.managers(manager_id);
CREATE INDEX ix_branches_manager_profile ON public.branches(manager_id);

CREATE TABLE public.rooms (
    room_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id uuid NOT NULL REFERENCES public.branches(id),
    manager_id uuid NOT NULL REFERENCES public.managers(manager_id),
    room_type_id uuid NOT NULL REFERENCES public.room_types(room_type_id),
    room_code varchar(255) NOT NULL,
    reference_price numeric(15,2) NOT NULL,
    capacity integer NOT NULL,
    status varchar(255) NOT NULL,
    description text,
    CONSTRAINT uq_rooms_branch_code UNIQUE (branch_id, room_code)
);
CREATE INDEX ix_rooms_manager ON public.rooms(manager_id);
CREATE INDEX ix_rooms_type ON public.rooms(room_type_id);
CREATE TABLE public.room_slots (
    room_slot_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id uuid NOT NULL REFERENCES public.rooms(room_id),
    slot_code varchar(255) NOT NULL,
    slot_name varchar(255) NOT NULL,
    status varchar(255) NOT NULL,
    description text
);
CREATE INDEX ix_room_slots_room ON public.room_slots(room_id);
CREATE TABLE public.appointment (
    appointment_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id uuid NOT NULL REFERENCES public.tenants(tenant_id),
    room_id uuid NOT NULL REFERENCES public.rooms(room_id),
    appointment_at timestamp(6) without time zone NOT NULL,
    status varchar(255) NOT NULL,
    note text
);
CREATE INDEX ix_appointment_room_time ON public.appointment(room_id, appointment_at);
CREATE INDEX ix_appointment_tenant ON public.appointment(tenant_id);

-- managers.manager_id is a profile ID; assignments.manager_id remains an accounts.id.
-- Existing Contract/Student 4 room/tenant UUID references are not rewritten or guessed here.
