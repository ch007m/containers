FROM csanchez/maven:3.8-openjdk-11

ARG USER="cloud"
ARG USER_HOME_DIR="/home/$USER"

ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"

RUN mkdir -p /home/$USER \
    && groupadd -r $USER -g 1000 \
    && useradd -u 1000 -r -g $USER -m -d /home/$USER -s /sbin/nologin -c "Cloud user" $USER \
    && chmod 755 /home/$USER \
    && chown -R $USER:$USER /home/$USER

USER $USER
