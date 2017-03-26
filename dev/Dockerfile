FROM java:8-jdk

RUN apt-get update && apt-get install -y nano && apt-get clean
RUN apt-get install -y maven
RUN apt-get remove -y openjdk-7-jre:amd64 openjdk-7-jre-headless:amd64

ENV TERM=xterm

WORKDIR /serposcope
