# OpenLMIS Selv v3 DHIS2 Integration Service
Service for integrating SELV3 OpenLMIS implementation with DHIS2.

### Environment Variable Setup
This repository requires an environment file called `.env` in the root folder of the project, with required project settings and credentials. For a template environment file, you can use [this one](https://raw.githubusercontent.com/OpenLMIS/openlmis-ref-distro/master/settings-sample.env). e.g.
 ```shell
 curl -o .env -L https://raw.githubusercontent.com/OpenLMIS/openlmis-ref-distro/master/settings-sample.env
 ```

### Build Deployment Image
Attached docker-compose.builder.yml allows automated docker image generation, run these two commands to 
create local image used together with selv-v3-distro

docker-compose -f docker-compose.builder.yml run builder \
docker-compose -f docker-compose.builder.yml build image
 
You need to insert Transifex token between these commands, if you don't have one you can generate new here:
https://www.transifex.com/user/settings/api/
