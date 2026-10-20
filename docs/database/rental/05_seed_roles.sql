-- Reference data only; safe to rerun. These are application roles, not DB roles.
BEGIN;
INSERT INTO rental.roles(id,code)
SELECT md5('rental-role:'||code)::uuid,code
FROM unnest(ARRAY['USER','OWNER','MANAGER','TENANT','ADMIN']) code
ON CONFLICT(code) DO NOTHING;
COMMIT;
