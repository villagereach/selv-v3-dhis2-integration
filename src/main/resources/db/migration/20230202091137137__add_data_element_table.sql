CREATE TABLE data_element (
    id UUID NOT NULL,
    name TEXT UNIQUE NOT NULL,
    source TEXT NOT NULL,
    indicator TEXT NOT NULL,
    orderable TEXT NOT NULL,
    element TEXT NOT NULL,
    datasetId UUID NOT NULL,

    CONSTRAINT data_element_pkey PRIMARY KEY (id),
    CONSTRAINT dataset_fkey FOREIGN KEY (datasetId) REFERENCES dataset(id)
);
