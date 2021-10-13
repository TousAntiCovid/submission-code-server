FROM atmoz/sftp

RUN apt-get update && \
    apt-get -y install dos2unix

COPY files/rsa_scs.pub /home/user/.ssh/authorized_keys
COPY files/install.sh /etc/sftp.d/

RUN dos2unix /etc/sftp.d/install.sh && \
    chmod +x /etc/sftp.d/install.sh

EXPOSE 22