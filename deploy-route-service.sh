#!/usr/bin/env bash
cf push wiretap-route-service
cf create-user-provided-service wiretap-route-service -r https://wiretap-route-service.cfapps.pez.pivotal.io
cf bind-route-service cfapps.pez.pivotal.io wiretap-route-service --hostname cover-service
