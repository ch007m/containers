FROM centos:centos7

RUN yum install -y epel-release && \
    yum update -y && \
    yum install -y nss_wrapper gettext && \
    yum clean all -y

# ENV USER_NAME=root
#     NSS_WRAPPER_PASSWD=/tmp/passwd
#     NSS_WRAPPER_GROUP=/tmp/group
#
# RUN touch ${NSS_WRAPPER_PASSWD} ${NSS_WRAPPER_GROUP} \
#     && chgrp 0 ${NSS_WRAPPER_PASSWD} ${NSS_WRAPPER_GROUP} \
#     && chmod g+rw ${NSS_WRAPPER_PASSWD} ${NSS_WRAPPER_GROUP}

RUN mkdir -p /usr/local/share
COPY passwd.template /usr/local/share/passwd.template
# ADD group.template /group.template
ADD nss_wrapper.sh /usr/local/bin/nss_wrapper.sh

CMD ["/usr/local/bin/nss_wrapper.sh"]