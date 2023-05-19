CREATE TABLE period_mapping (
    id UUID NOT NULL,
    name TEXT NOT NULL,
    source TEXT NOT NULL,
    dhisPeriod TEXT NOT NULL,
    processingPeriodId UUID NOT NULL,
    startDate date NOT NULL,
    endDate date NOT NULL,
    serverId UUID NOT NULL,

    CONSTRAINT period_mapping_pkey PRIMARY KEY (id),
    CONSTRAINT server_fkey FOREIGN KEY (serverId) REFERENCES server(id)
);