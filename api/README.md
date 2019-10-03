# Poppin api

Poppin API build in Python with Flask.

## Contents

- [Developing](#developing)


## Developing

Before developing in a new terminal session, run

    `source setup.sh`

This sets up pre-push hooks and installs dependencies.

There are a couple of ways you can build the project for development and testing;

- Docker containers. Slow and not automatic. Fully featured testing, "as it is deployed".
- Docker and Python Server. A mix of both solutions. DB is in Docker, Python Server serves Flask.

### Docker Containers

There are two containers at the moment, one for the python server, and one for the database.

To build the containers, run:

    ./buildContainers.sh

To start containers

    ./start.sh

To restart just the server instead of restarting the server and the db

    ./restartServer.sh

To check logs:

    docker logs poppinserver
    or
    docker logs poppindb

If you want to access the db container from the server container, use the url

    poppindb.poppin

If you want to access the server container from the db container, use the url

    poppinserver.poppin

To install docker on Ubuntu:

    sudo apt-get update
    sudo apt-get install docker-ce
    sudo gpasswd -a <your user name here> docker

To access web server:

- Build and start the containers
- The web server should be accessible at localhost:1221
- The db should be accessible at localhost:3301

### Docker and Python Server


