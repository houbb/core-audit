-- ============================================================
-- V3: Add context_json column to audit_event table
-- Description: Stores the full AuditContext (Operator, Request,
--              Client, Business, System) as a JSON TEXT column
--              for P2 Context Runtime.
-- ============================================================

ALTER TABLE audit_event ADD COLUMN context_json TEXT;