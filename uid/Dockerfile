FROM registry.access.redhat.com/ubi8/openjdk-11

LABEL git="2.27"

USER root
RUN microdnf install git

COPY uid_entrypoint /usr/local/bin
RUN chmod -R g=u /etc/passwd

USER 185

### User name recognition at runtime w/ an arbitrary uid (e.g: 1000, ...)
ENTRYPOINT [ "/usr/local/bin/uid_entrypoint" ]
