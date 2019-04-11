FROM docker-registry.wemove.com/ingrid-java
ENV INGRID_USER=root
ENV RUN_DIRECTLY=true

ADD ingrid-iplug-csw-dsc*-installer.jar /

RUN unzip ingrid-*.jar -d /tmp \
    && mkdir -p /opt/ingrid/ingrid-iplug-csw-dsc \
    && cp -R /tmp/ingrid-iplug-csw-dsc-*/* /opt/ingrid/ingrid-iplug-csw-dsc/ \
    && touch /opt/ingrid/ingrid-iplug-csw-dsc/pattern \
    && rm -Rf /tmp/* \
    && sed -i -r 's/(<AppenderRef.*\/>)/\1<AppenderRef ref="Console" \/>/g' /opt/ingrid/ingrid-iplug-csw-dsc/conf/log4j2.xml \
    && rm ingrid-*.jar

RUN echo jetty.port=80 > /opt/ingrid/ingrid-iplug-csw-dsc/conf/config.override.properties
WORKDIR /opt/ingrid/ingrid-iplug-csw-dsc
EXPOSE 9903

CMD /bin/sh start.sh start
