FROM flaviostutz/maven-prewarmed AS BUILD

ADD / /build/
RUN cd mvn package


FROM openjdk:8-jre-alpine

RUN mkdir /app
WORKDIR /app

COPY --from=BUILD /build/skylight-autopilot/target/skylight-autopilot* /app
COPY --from=BUILD /build/skylight-autopilot-simulation/target/skylight-autopilot-simulation* /app
COPY --from=BUILD /build/skylight-commons-core/target/skylight-commons-core* /app
COPY --from=BUILD /build/skylight-commons-ui/target/skylight-commons-ui* /app
COPY --from=BUILD /build/skylight-cucs/target/skylight-cucs* /app
COPY --from=BUILD /build/skylight-vsm/target/skylight-vsm* /app
COPY --from=BUILD /build/skylight-vsm-simulation/target/skylight-vsm-simulation* /app

RUN ls /app
