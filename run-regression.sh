#!/usr/bin/env bash
set -uo pipefail

rm -rf target/allure-results target/allure-report
mvn clean test
test_exit=$?

allure generate target/allure-results --clean -o target/allure-report
report_exit=$?

printf '\nTest execution exit code: %s (expected: non-zero because five product defects are detected)\n' "$test_exit"
printf 'Allure report: %s/target/allure-report/index.html\n' "$PWD"

if [[ "$report_exit" -ne 0 ]]; then
  exit "$report_exit"
fi

exit 0
