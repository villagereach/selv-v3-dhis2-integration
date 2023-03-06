CREATE TABLE shared_facility (
    id UUID NOT NULL,
    code TEXT NOT NULL,
    facilityId UUID NOT NULL,
    orgUnitId UUID NOT NULL,
    serverId UUID NOT NULL,

    CONSTRAINT shared_facility_pkey PRIMARY KEY (id),
    CONSTRAINT server_fkey FOREIGN KEY (serverId) REFERENCES server(id)
);
