FROM alpine:3.22
RUN apk add --no-cache postgresql18-client bash curl
COPY backup.sh /backup.sh
RUN chmod +x /backup.sh
CMD ["/backup.sh"]