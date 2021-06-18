FROM csanchez/maven:3.8-openjdk-11

RUN mkdir -p /home/cloud \
    && groupadd -r cloud -g 1000 \
    && useradd -u 1000 -r -g cloud -m -d /home/cloud -s /sbin/nologin -c "Cloud user" cloud \
    && chmod 755 /home/cloud \
    && chown -R cloud:cloud /home/cloud

USER cloud
