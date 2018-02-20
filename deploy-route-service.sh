#!/usr/bin/env bash
./gradlew clean check assemble --info
cf push edge-router-service

cf create-user-provided-service edge-router-service -r https://edge-router-service.apps.pcf-t01-we.rabobank.nl
cf bind-route-service apps.pcf-t01-we.rabobank.nl edge-router-service --hostname http-header-logger
curl https://http-header-logger.apps.pcf-t01-we.rabobank.nl/health -H "x-auth-token: BEN:10:COOL" -v

# cf logs edge-router-service --recent
# cf logs http-header-logger --recent
# cf unbind-route-service apps.pcf-t01-we.rabobank.nl edge-router-service --hostname http-header-logger -f
# cf delete-service edge-router-service -f
# cf delete edge-router-service -f
# cf delete-orphaned-routes -f