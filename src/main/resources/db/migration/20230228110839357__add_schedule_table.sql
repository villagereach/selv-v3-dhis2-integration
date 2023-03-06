CREATE TABLE schedule (
    id UUID NOT NULL,
    periodEnumerator TEXT NOT NULL,
    timeOffset INT NOT NULL,
    serverId UUID NOT NULL,
    datasetId UUID NOT NULL,
    elementId UUID NOT NULL,

    CONSTRAINT schedule_pkey PRIMARY KEY (id),
    CONSTRAINT server_fkey FOREIGN KEY (serverId) REFERENCES server(id),
    CONSTRAINT dataset_fkey FOREIGN KEY (datasetId) REFERENCES dataset(id),
    CONSTRAINT data_element_fkey FOREIGN KEY (elementId) REFERENCES data_element(id)
);
