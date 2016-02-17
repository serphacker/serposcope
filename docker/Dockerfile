FROM java:8

ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64

RUN wget https://serposcope.serphacker.com/download/2.2.0/serposcope_2.3.0_all.deb -O /tmp/serposcope.deb
RUN dpkg -i /tmp/serposcope.deb
RUN rm /tmp/serposcope.deb
COPY serposcope /etc/default/serposcope

VOLUME /var/lib/serposcope/

EXPOSE 7134

CMD java -jar /usr/share/serposcope/serposcope.jar >$LOGDIR/startup.log
