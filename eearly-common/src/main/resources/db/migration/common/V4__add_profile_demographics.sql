-- Demographics synced from the admin service (which owns them), needed to derive body
-- composition from a scale reading. Nullable: existing profiles are filled by the admin backfill.
ALTER TABLE profile ADD COLUMN date_of_birth DATE;
ALTER TABLE profile ADD COLUMN sex VARCHAR(16);
