-- Shared UUID schema: reuse branches; no demo data. Flyway owns the transaction.
CREATE INDEX ix_manager_active_branch ON public.manager_branch_assignments(manager_id, branch_id)
    WHERE is_active = true AND unassigned_at IS NULL;

CREATE TABLE public.contracts (
    id uuid PRIMARY KEY, version bigint NOT NULL DEFAULT 0,
    branch_id uuid NOT NULL REFERENCES public.branches(id),
    room_id uuid NOT NULL, room_slot_id uuid,
    rental_mode varchar(20) NOT NULL CHECK (rental_mode IN ('WHOLE_ROOM', 'SLOT')),
    status varchar(20) NOT NULL CHECK (status IN ('DRAFT', 'ACTIVE', 'ENDED', 'CANCELLED')),
    start_date date NOT NULL, end_date date NOT NULL,
    monthly_rent numeric(19,2) NOT NULL CHECK (monthly_rent > 0),
    first_month_rent numeric(19,2) CHECK (first_month_rent >= 0),
    last_month_rent numeric(19,2) CHECK (last_month_rent >= 0),
    payment_day integer NOT NULL CHECK (payment_day BETWEEN 1 AND 31),
    CHECK (start_date <= end_date),
    CHECK ((rental_mode = 'SLOT' AND room_slot_id IS NOT NULL) OR
           (rental_mode = 'WHOLE_ROOM' AND room_slot_id IS NULL))
);
CREATE TABLE public.contract_tenants (
    id uuid PRIMARY KEY, contract_id uuid NOT NULL REFERENCES public.contracts(id),
    tenant_id uuid NOT NULL, joined_date date NOT NULL, actual_leave_date date,
    representative boolean NOT NULL,
    CHECK (actual_leave_date IS NULL OR actual_leave_date >= joined_date)
);
CREATE INDEX ix_contract_tenants_contract ON public.contract_tenants(contract_id);
CREATE INDEX ix_contracts_room ON public.contracts(room_id);

CREATE TABLE public.invoices (
    id uuid PRIMARY KEY, version bigint NOT NULL DEFAULT 0,
    contract_id uuid NOT NULL REFERENCES public.contracts(id),
    status varchar(20) NOT NULL CHECK (status IN ('DRAFT', 'ISSUED', 'CANCELLED')),
    period_start date NOT NULL, period_end date NOT NULL,
    scheduled_payment_date date NOT NULL, due_date date,
    subtotal_amount numeric(19,0) NOT NULL CHECK (subtotal_amount >= 0),
    adjustment_amount numeric(19,0) NOT NULL,
    total_amount numeric(19,0) NOT NULL CHECK (total_amount > 0),
    note varchar(1000), created_by uuid NOT NULL, updated_by uuid NOT NULL,
    cancelled_by uuid, cancelled_at timestamp with time zone, cancellation_reason varchar(500),
    created_at timestamp with time zone NOT NULL, updated_at timestamp with time zone NOT NULL,
    source_refreshed_at timestamp with time zone,
    CHECK (period_start <= period_end),
    CHECK (total_amount = subtotal_amount + adjustment_amount)
);
CREATE INDEX ix_invoices_contract ON public.invoices(contract_id);

CREATE TABLE public.invoice_billing_periods (
    id uuid PRIMARY KEY, invoice_id uuid NOT NULL REFERENCES public.invoices(id),
    contract_id uuid NOT NULL REFERENCES public.contracts(id),
    billing_month date NOT NULL,
    active_marker boolean CHECK (active_marker IS NULL OR active_marker = true),
    CHECK (EXTRACT(DAY FROM billing_month) = 1),
    CONSTRAINT uk_invoice_contract_month UNIQUE (contract_id, billing_month, active_marker)
);
CREATE INDEX ix_invoice_periods_invoice ON public.invoice_billing_periods(invoice_id);

CREATE TABLE public.invoice_items (
    id uuid PRIMARY KEY, invoice_id uuid NOT NULL REFERENCES public.invoices(id),
    line_order integer,
    type varchar(20) NOT NULL CHECK (type IN ('RENT', 'SERVICE', 'SURCHARGE', 'DISCOUNT')),
    description varchar(500) NOT NULL,
    quantity numeric(19,4) NOT NULL CHECK (quantity >= 0),
    unit_price numeric(19,4) NOT NULL CHECK (unit_price >= 0),
    amount numeric(19,0) NOT NULL CHECK (amount >= 0),
    service_period_start date, service_period_end date,
    source_charge_id uuid, source_allocation_id uuid,
    source_quantity numeric(19,4), source_unit_price numeric(19,4), source_total_amount numeric(19,2)
);
CREATE INDEX ix_invoice_items_invoice ON public.invoice_items(invoice_id);
CREATE TABLE public.invoice_item_allocations (
    id uuid PRIMARY KEY, invoice_item_id uuid NOT NULL REFERENCES public.invoice_items(id),
    contract_tenant_id uuid NOT NULL REFERENCES public.contract_tenants(id),
    tenant_id uuid NOT NULL, amount numeric(19,0) NOT NULL CHECK (amount >= 0),
    CONSTRAINT uk_invoice_item_tenant UNIQUE (invoice_item_id, tenant_id)
);
CREATE TABLE public.invoice_draft_warnings (
    invoice_id uuid NOT NULL REFERENCES public.invoices(id), warning_order integer NOT NULL,
    warning varchar(500) NOT NULL, PRIMARY KEY (invoice_id, warning_order)
);
CREATE TABLE public.invoice_audit_events (
    id uuid PRIMARY KEY, invoice_id uuid NOT NULL REFERENCES public.invoices(id),
    actor_id uuid NOT NULL, action varchar(30) NOT NULL, details varchar(2000) NOT NULL,
    occurred_at timestamp with time zone NOT NULL
);
CREATE INDEX ix_invoice_audit_invoice ON public.invoice_audit_events(invoice_id, occurred_at);
