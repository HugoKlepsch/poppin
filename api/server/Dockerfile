FROM python:3-slim-stretch

# https://github.com/dalibo/temboard/commit/ff98d6740ae11345658508b02052294d6cffd448
RUN mkdir -p /usr/share/man/man1 && \
    mkdir -p /usr/share/man/man7 && \
    apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get install -y \
    mariadb-client \
    build-essential \
    postgresql-client \
    libpq-dev \
    iputils-ping \
    curl && \
    rm -rf /var/lib/apt/lists/* && \
    mkdir server

COPY requirements.txt ./
RUN python3 -m venv venv && . venv/bin/activate && pip install --no-cache-dir -r requirements.txt

WORKDIR /server

COPY . ./

RUN ./build.sh

EXPOSE 80

WORKDIR /

CMD ["./server/run.sh"]
