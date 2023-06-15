DELETE FROM period_mapping;

ALTER TABLE period_mapping
DROP CONSTRAINT server_fkey,
ADD COLUMN datasetId UUID NOT NULL,
ADD CONSTRAINT dataset_fkey FOREIGN KEY (datasetId) REFERENCES dataset(id),
DROP COLUMN serverId;