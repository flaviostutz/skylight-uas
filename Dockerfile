FROM flaviostutz/maven-prewarmed AS BUILD

ADD /src/ /build/
RUN mvn package

# ADD /pom.xml /build

# ADD /skylight-commons-core /build/skylight-commons-core
# RUN cd /build/skylight-commons-core && mvn package

# ADD /skylight-autopilot /build/skylight-autopilot
# RUN cd /build/skylight-autopilot && mvn package

# ADD /skylight-vsm /build/skylight-vsm
# RUN cd /build/skylight-vsm && mvn package

# ADD /skylight-vsm-simulation /build/skylight-vsm-simulation
# RUN cd /build/skylight-vsm-simulation && mvn package

# ADD /skylight-autopilot-simulation /build/skylight-autopilot-simulation
# RUN cd /build/skylight-autopilot-simulation && mvn package

# ADD /skylight-commons-ui /build/skylight-commons-ui
# RUN cd /build/skylight-commons-ui && mvn package

# ADD /skylight-cucs /build/skylight-cucs
# RUN cd /build/skylight-cucs && mvn package


FROM openjdk:14-alpine3.10

RUN apk add libx11
RUN apk add libxext
RUN apk add libxrender
RUN apk add libxtst-dev
RUN apk add freetype-dev
RUN apk add fontconfig
RUN apk add ttf-dejavu

RUN mkdir /app
WORKDIR /app

COPY --from=BUILD /build/skylight-commons-core/target/skylight-commons-core* /app/
COPY --from=BUILD /build/skylight-autopilot/target/skylight-autopilot* /app/
COPY --from=BUILD /build/skylight-vsm/target/skylight-vsm* /app/
COPY --from=BUILD /build/skylight-vsm-simulation/target/skylight-vsm-simulation* /app/
COPY --from=BUILD /build/skylight-autopilot-simulation/target/skylight-autopilot-simulation* /app/
COPY --from=BUILD /build/skylight-commons-ui/target/skylight-commons-ui* /app/
COPY --from=BUILD /build/skylight-cucs/target/skylight-cucs* /app/

RUN ls /app

ADD /startup.sh /

ENTRYPOINT [ "/startup.sh" ]

