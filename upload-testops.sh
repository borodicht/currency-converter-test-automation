#!/usr/bin/env bash
set -euo pipefail

: "${ALLURE_TOKEN:?Set ALLURE_TOKEN through a protected secret or environment variable}"

export ALLURE_ENDPOINT="${ALLURE_ENDPOINT:-https://allure.pflb.ru}"
export ALLURE_PROJECT_ID="${ALLURE_PROJECT_ID:-11}"
export ALLURE_RESULTS="${ALLURE_RESULTS:-target/allure-results}"
export ALLURE_LAUNCH_NAME="${ALLURE_LAUNCH_NAME:-Регрессия — Банковский конвертер — Java автотесты}"
export ALLURE_LAUNCH_TAGS="${ALLURE_LAUNCH_TAGS:-regression,java,junit5,currency-converter}"

if [[ ! -x tools/allurectl ]]; then
  printf 'tools/allurectl is missing. Install the official Apple Silicon binary first.\n' >&2
  exit 2
fi

tools/allurectl upload "$ALLURE_RESULTS"
