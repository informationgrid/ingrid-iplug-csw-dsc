FROM alpine AS builder

COPY ingrid-iplug-csw-dsc*-installer.jar /
RUN unzip -q ingrid-*.jar -d /tmp \
    && export INSTALL_DIR=`ls -d1 /tmp/ingrid-*` \
    && touch $INSTALL_DIR/pattern \
    && mkdir -p $INSTALL_DIR/conf \
    && echo jetty.port=8080 > $INSTALL_DIR/conf/config.override.properties \
    && sed -i -r 's/(<AppenderRef.*\/>)/\1<AppenderRef ref="Console" \/>/g' $INSTALL_DIR/conf/log4j2.xml

FROM docker-registry.wemove.com/ingrid-java:jre-17
ENV INGRID_USER=ingrid
ENV RUN_DIRECTLY=true

# user, group ingrid (1000:1000) is created in base image
COPY --chown=1000:1000 --from=builder /tmp/ingrid-* /opt/ingrid/ingrid-iplug-csw-dsc/

USER ingrid

WORKDIR /opt/ingrid/ingrid-iplug-csw-dsc
EXPOSE 8080

CMD /bin/sh start.sh start
