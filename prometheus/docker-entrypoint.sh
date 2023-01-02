#!/bin/bash
set -e
envsubst < /prometheus.yml > /opt/bitnami/prometheus/conf/prometheus.yml
exec "$@"
