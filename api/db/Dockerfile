FROM postgres:10

RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get install --no-install-recommends -y \
    iputils-ping && \
    mkdir poppin && \
    rm -rf /var/lib/apt/lists/*

ENV POSTGRES_PASSWORD="notwaterloo"
ENV POSTGRES_USER="root"
ENV POSTGRES_DB="poppindb"

COPY . poppin

WORKDIR poppin

#EXPOSE 3306
EXPOSE 5432
