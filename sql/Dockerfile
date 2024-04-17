FROM postgres:latest
ENV POSTGRES_PASSWORD cpop0522
ENV POSTGRES_DB parking
COPY init.sql /docker-entrypoint-initdb.d/init.sql
EXPOSE 5433:5432