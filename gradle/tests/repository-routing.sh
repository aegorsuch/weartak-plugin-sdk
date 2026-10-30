#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/../.."

clean_env=(env
  -u ARTIFACTORY_CONSUME_USER -u ARTIFACTORY_CONSUME_PASSWORD
  -u ARTIFACTORY_USER -u ARTIFACTORY_PASSWORD -u TAK_USERNAME -u TAK_PASSWORD
  -u ROUTING_TEST_USER -u ROUTING_TEST_PASSWORD)
check() {
  "${clean_env[@]}" "$@" ./gradlew --no-daemon --offline --quiet \
    --init-script gradle/tests/repository-routing.init.gradle help > /dev/null
}

check
check ARTIFACTORY_CONSUME_USER=test-user
check ARTIFACTORY_CONSUME_PASSWORD=test-password
check ARTIFACTORY_CONSUME_USER='   ' ARTIFACTORY_CONSUME_PASSWORD='   '
check ARTIFACTORY_CONSUME_USER=preferred-user ARTIFACTORY_USER=ignored-user TAK_USERNAME=ignored-user \
  ARTIFACTORY_CONSUME_PASSWORD=preferred-password ARTIFACTORY_PASSWORD=ignored-password TAK_PASSWORD=ignored-password \
  ROUTING_TEST_USER=preferred-user ROUTING_TEST_PASSWORD=preferred-password
check ARTIFACTORY_CONSUME_USER='   ' ARTIFACTORY_USER=legacy-user TAK_USERNAME=ignored-user \
  ARTIFACTORY_CONSUME_PASSWORD='   ' ARTIFACTORY_PASSWORD=legacy-password TAK_PASSWORD=ignored-password \
  ROUTING_TEST_USER=legacy-user ROUTING_TEST_PASSWORD=legacy-password
check ARTIFACTORY_CONSUME_USER='   ' ARTIFACTORY_USER='   ' TAK_USERNAME=tak-user \
  ARTIFACTORY_CONSUME_PASSWORD='   ' ARTIFACTORY_PASSWORD='   ' TAK_PASSWORD=tak-password \
  ROUTING_TEST_USER=tak-user ROUTING_TEST_PASSWORD=tak-password
check ARTIFACTORY_CONSUME_USER=mixed-user TAK_PASSWORD=mixed-password \
  ROUTING_TEST_USER=mixed-user ROUTING_TEST_PASSWORD=mixed-password
echo 'Repository routing matrix passed (8 cases, both scopes; no repository requests).'
