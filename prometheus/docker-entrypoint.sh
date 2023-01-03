#!/bin/bash
set -e
envsubst < /prometheus.yml > /etc/prometheus/prometheus.yml
exec "$@"
