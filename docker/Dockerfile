FROM java:8-jre

COPY serposcope /etc/default/serposcope
RUN wget https://serposcope.serphacker.com/download/2.3.0/serposcope_2.3.0_all.deb -O /tmp/serposcope.deb
RUN dpkg --force-confold -i /tmp/serposcope.deb
RUN rm /tmp/serposcope.deb

VOLUME /var/lib/serposcope/

EXPOSE 7134

CMD service serposcope start && tail -F /var/log/serposcope/startup.log
