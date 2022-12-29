# OpenLMIS Selv v3 DHIS2 Integration Service
Service for integrating SELV3 OpenLMIS implementation with DHIS2.

### Build Deployment Image
Attached docker-compose.builder.yml allows automated docker image generation, run these two commands to 
create local image used together with selv-v3-distro

docker-compose -f docker-compose.builder.yml run builder \
docker-compose -f docker-compose.builder.yml build image
 
You need to insert Transifex token between these commands, if you don't have one you can generate new here:
https://www.transifex.com/user/settings/api/
