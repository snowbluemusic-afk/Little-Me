FROM python:3.12-slim
WORKDIR /app
COPY relay.py /app/
RUN mkdir /data && chown 65532:65532 /data
USER 65532:65532
ENV LITTLEME_BIND=0.0.0.0 LITTLEME_DB=/data/presence.sqlite3 PORT=8080
VOLUME ["/data"]
EXPOSE 8080
CMD ["python","/app/relay.py"]
