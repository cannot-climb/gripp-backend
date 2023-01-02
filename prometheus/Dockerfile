FROM bitnami/prometheus:2.39.1-debian-11-r10
EXPOSE 9090
USER root
RUN apt-get update \
    && apt-get install -y gettext-base
USER 1001
COPY prometheus.yml docker-entrypoint.sh /
ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["/opt/bitnami/prometheus/bin/prometheus", "--config.file=/opt/bitnami/prometheus/conf/prometheus.yml", "--storage.tsdb.path=/opt/bitnami/prometheus/data", "--web.console.libraries=/opt/bitnami/prometheus/conf/console_libraries", "--web.console.templates=/opt/bitnami/prometheus/conf/consoles"]
