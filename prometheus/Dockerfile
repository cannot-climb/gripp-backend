FROM debian:bullseye AS builder
RUN apt-get update \
    && apt-get install -y gettext-base
COPY .env prometheus.yml /
RUN eval $(grep -v '^#' .env | xargs -d '\n') envsubst < /prometheus.yml > /prometheus-modified.yml

FROM prom/prometheus:v2.39.1
EXPOSE 9090
COPY --from=builder /prometheus-modified.yml /etc/prometheus/prometheus.yml
