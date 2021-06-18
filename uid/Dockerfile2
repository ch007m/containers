FROM registry.access.redhat.com/ubi8/openjdk-11

LABEL git="2.27"

USER root
RUN microdnf install git wget

ARG GOSU_VERSION=1.13
RUN wget -O /usr/local/bin/gosu "https://github.com/tianon/gosu/releases/download/$GOSU_VERSION/gosu-amd64" \
 && chmod +x /usr/local/bin/gosu \
 && gosu nobody true

COPY entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod +x /usr/local/bin/entrypoint.sh

ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]