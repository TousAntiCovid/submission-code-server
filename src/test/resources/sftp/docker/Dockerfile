FROM debian:buster

RUN apt-get update && \
    apt-get -y install dos2unix && \
    apt-get -y install openssh-server && \
    rm -rf /var/lib/apt/lists/* && \
    mkdir -p /var/run/sshd && \
    rm -f /etc/ssh/ssh_host_*key*

COPY files/sshd_config /etc/ssh/sshd_config
COPY files/create-sftp-user /usr/local/bin/
COPY files/entrypoint /
COPY files/rsa_scs.pub /home/user/.ssh/authorized_keys
COPY files/install.sh /etc/sftp.d/

RUN dos2unix entrypoint && \
    dos2unix /etc/sftp.d/install.sh && \
    dos2unix /usr/local/bin/create-sftp-user && \
    chmod +x entrypoint && \
    chmod +x /etc/sftp.d/install.sh && \
    chmod +x /usr/local/bin/create-sftp-user

EXPOSE 22

ENTRYPOINT ["/entrypoint"]