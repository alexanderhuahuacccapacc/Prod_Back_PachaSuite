FROM alpine:3.18
RUN apk add --no-cache postgresql16-client bash curl
COPY backup.sh /backup.sh
RUN chmod +x /backup.sh
CMD ["/backup.sh"]