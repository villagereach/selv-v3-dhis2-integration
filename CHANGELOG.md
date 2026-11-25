1.0.1 / 2026-11-25
==================

Improvements:
* [SELV3-908](https://openlmis.atlassian.net/browse/SELV3-908): The service can log in to PostgreSQL with `scram-sha-256` passwords, which is needed before the SELV databases stop accepting md5.
  * PostgreSQL JDBC driver upgraded from 42.0.0 to 42.6.2
  * Base image upgraded from `openlmis/service-base:4` to `openlmis/service-base:7` (OpenJDK 8u275, Alpine 3.13)
