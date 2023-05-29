ALTER TABLE data_element
DROP CONSTRAINT data_element_name_key;

ALTER TABLE data_element
ADD CONSTRAINT data_element_name_key UNIQUE(name, datasetid);

ALTER TABLE server
ADD CONSTRAINT server_name_key UNIQUE(name);