FROM ubuntu/prometheus:2.33-22.04_beta
RUN apt-get update \
    && apt-get install -y gettext-base
COPY prometheus.yml docker-entrypoint.sh /
ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["/usr/bin/prometheus", "--config.file=/etc/prometheus/prometheus.yml", "--storage.tsdb.path=/prometheus"]
