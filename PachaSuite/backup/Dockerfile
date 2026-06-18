FROM postgres:18-alpine
RUN apk add --no-cache bash curl python3 py3-pip
RUN pip3 install awscli --break-system-packages
COPY backup.sh /backup.sh
RUN chmod +x /backup.sh
CMD ["/backup.sh"]