-- Auth uses the existing UUID accounts table; never copy/drop or guess a users-to-accounts mapping.
SET LOCAL lock_timeout = '10s';
SET LOCAL statement_timeout = '120s';
DO $guard$
BEGIN
    IF to_regclass('public.users') IS NOT NULL THEN
        RAISE EXCEPTION 'AUTH_SCHEMA_GUARD: separate users table exists; preserve it and review identity mapping first';
    END IF;
    IF (SELECT data_type FROM information_schema.columns
        WHERE table_schema='public' AND table_name='accounts' AND column_name='id') IS DISTINCT FROM 'uuid' THEN
        RAISE EXCEPTION 'AUTH_SCHEMA_GUARD: accounts.id must already be UUID';
    END IF;
    IF EXISTS (SELECT lower(email) FROM public.accounts WHERE email IS NOT NULL
               GROUP BY lower(email) HAVING count(*) > 1) THEN
        RAISE EXCEPTION 'AUTH_DATA_GUARD: duplicate case-insensitive account emails; resolve ownership without deleting accounts';
    END IF;
END
$guard$;
-- Preserves original email spelling and existing NULL-email legacy accounts.
CREATE UNIQUE INDEX uq_accounts_email_ignore_case ON public.accounts(lower(email));
CREATE TABLE public.invalidate_tokens (
    -- JWT jti is an opaque string, not an account/room/entity foreign key.
    id varchar(36) PRIMARY KEY,
    expired_time timestamp(6) without time zone NOT NULL
);
CREATE INDEX ix_invalidate_tokens_expiry ON public.invalidate_tokens(expired_time);
