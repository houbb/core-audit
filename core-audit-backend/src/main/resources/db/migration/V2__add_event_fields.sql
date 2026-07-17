-- ============================================================
-- V2: Add P1 event fields to audit_event table
-- Description: Add event_id, event_type, source, version,
--              published, publish_time, publish_result columns
--              for Phase 1 Audit Event Runtime
-- ============================================================

ALTER TABLE audit_event ADD COLUMN event_id       TEXT;
ALTER TABLE audit_event ADD COLUMN event_type     TEXT;
ALTER TABLE audit_event ADD COLUMN source         TEXT;
ALTER TABLE audit_event ADD COLUMN version        TEXT DEFAULT '1.0';
ALTER TABLE audit_event ADD COLUMN published      INTEGER DEFAULT 1;
ALTER TABLE audit_event ADD COLUMN publish_time   TEXT;
ALTER TABLE audit_event ADD COLUMN publish_result TEXT;

-- ============================================================
-- Index: event_type filtering (common query pattern)
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_audit_event_event_type
    ON audit_event(event_type);

-- ============================================================
-- Index: event_id lookup (distributed event correlation)
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_audit_event_event_id
    ON audit_event(event_id);