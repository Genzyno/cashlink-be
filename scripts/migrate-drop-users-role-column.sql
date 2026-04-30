-- Migrate users table: remove old enum "role" column (replaced by role_id FK to ledger.role)
-- Run this script if you get: null value in column "role" of relation "users" violates not-null constraint
-- Usage: psql -U postgres -d postgres -f scripts/migrate-drop-users-role-column.sql

ALTER TABLE ledger.users DROP COLUMN IF EXISTS role;
