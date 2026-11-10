-- EC08 offline movement idempotency (MySQL 8).
-- Execute on a staged database first and back up the production database
-- before applying to a deployed environment.
-- Existing rows keep NULL request ids; duplicates of NULL are permitted.
-- Do not run this script twice: inspect INFORMATION_SCHEMA first.

ALTER TABLE material_movements
    ADD COLUMN client_request_id VARCHAR(128) NULL;

ALTER TABLE material_movements
    ADD CONSTRAINT uk_material_movements_client_request
    UNIQUE (material_id, client_request_id);

-- The API must be deployed together with Mobile's new sync contract.
-- Verify one movement is created for each clientRequestId and that replay
-- returns HTTP 200 with the original movement. A conflicting payload gets 409.
