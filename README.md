# Obelix & Co. Webshop 🪨

The webshop manages the basket and purchases on port 8080. The quarry manages
menhirs and its database on port 8081. Both use the shared `QuarryApi` contract.
The original menhir endpoints on port 8080 forward requests to the quarry.

## Run locally

Install Java 25 and start Docker Desktop. Start Zipkin from the project folder:

```sh
docker compose up -d zipkin
```

If Docker is unavailable, Zipkin also provides a standalone Java server.
Follow the [Zipkin Java quickstart](https://zipkin.io/pages/quickstart.html#java)
and run its executable JAR with `java -jar zipkin.jar` instead.

Start the quarry and webshop in separate terminals:

```sh
./gradlew :obelix-quarry-impl:bootRun
```

```sh
./gradlew :obelix-webshop:bootRun
```

If your shell cannot find Java installed with Homebrew, run these exports in each terminal first:

```sh
export JAVA_HOME="/opt/homebrew/opt/openjdk@25/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
```

## Follow a purchase in Zipkin

Both applications use Micrometer with the OpenTelemetry bridge and send traces
to Zipkin. Every request is sampled for this local exercise. Spring's configured
WebClient builder passes the trace context from the webshop to the quarry.

1. Open <http://localhost:8080/api/menhirs> and copy the ID of a menhir.
2. Add an offer and buy that menhir, replacing `MENHIR_ID` below:

```sh
curl -X PUT http://localhost:8080/api/basket/offer \
  -H 'Content-Type: application/json' -d '{"name":"boar","count":2}'
curl -X POST http://localhost:8080/api/basket/buy/MENHIR_ID
```

3. Open <http://localhost:9411>, click **Run Query**, and open the purchase trace.
   It should contain spans from both `webshop` and `quarry`, including our custom
   spans `webshop.purchase`, `quarry.find-menhir`, and `quarry.delete-menhir`.
   The `menhir.id` tag identifies the selected stone. Allow a few seconds for export.

The custom spans wrap the existing operations with `Observation.observe()`.
It starts and closes the span, and records an error if the operation throws.
The shared settings are in `obelix-common/src/main/resources/application-common.yaml`.
Set `ZIPKIN_ENDPOINT` to send traces to a different Zipkin server.
Change `quarry.base-url` to use a quarry at a different address.

Stop each application with Ctrl+C and stop Zipkin with:

```sh
docker compose down
```

Menhirs are stored in an in-memory H2 database, so restarting the quarry restores
the three default stones. Zipkin also uses in-memory storage in this setup.

Spring reference: <https://docs.spring.io/spring-boot/reference/actuator/tracing.html>.
