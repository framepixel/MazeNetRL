FROM ubuntu:18.04

RUN apt-get update -y && \
    apt-get upgrade -y && \
    apt-get install -y ca-certificates \
     wget \
     curl \
     git \
     apt-transport-https \
     vim \
     openjdk-11-jdk-headless \
     maven && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

VOLUME /build
WORKDIR /build
CMD [mvn,install]

