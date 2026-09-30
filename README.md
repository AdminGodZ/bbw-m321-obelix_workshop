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

## Monitor the menhir inventory

The quarry registers one gauge, `quarry.menhirs`, which reads
`MenhirRepository.count()` when metrics are requested. It becomes `quarry_menhirs`
in Prometheus. There is no separate counter to update after a purchase.

With Docker Desktop and the quarry running, start the monitoring services:

```sh
docker compose up -d prometheus grafana
```

The quarry exposes metrics at <http://localhost:8081/actuator/prometheus>.
Prometheus scrapes them every five seconds using `host.docker.internal`, the
Docker Desktop address for services running on your Mac.
Open <http://localhost:9090>, enter `quarry_menhirs`, and run the query.
The initial value is 3; after buying a stone it becomes 2 on the next scrape.

Grafana's Prometheus data source is configured automatically. Create the panel:

1. Open <http://localhost:3000>. On the first run, log in with `admin` / `admin`
   and choose a new password.
2. Create a dashboard and add a visualization using the **Prometheus** data source.
3. Switch the query editor to **Code** and enter `quarry_menhirs`.
4. Choose **Stat**, enable the **Instant** query, and name the panel **Available menhirs**.
5. Save the dashboard and set its refresh interval to **5s**.

The named `grafana-data` volume preserves your password and dashboard across
`docker compose down` and subsequent starts. All three infrastructure services
can be started together with `docker compose up -d` and stopped with
`docker compose down`.

References: [Spring metrics](https://docs.spring.io/spring-boot/reference/actuator/metrics.html)
and [Grafana data source provisioning](https://grafana.com/docs/grafana/latest/administration/provisioning/).

## Code quality and versions

The project uses Java 25, Spring Boot 4.1.1, Gradle 9.8.0, and the Spring dependency
management plugin 1.1.7. Application dependency versions come from Spring Boot's
dependency set. Local variables use `var` where Java permits it; fields, method
parameters, and return types keep their declared types.

Run the tests and build both executable applications with:

```sh
./gradlew test bootJar
```

The tests cover purchasing, the original webshop URLs, quarry inventory, and the
Prometheus gauge changing after deletion.

The assignment's SonarLint check still requires a valid scan in SonarQube for IDE
(formerly SonarLint). Analyze the Java source and test files in all four modules.
A temporary standalone scan was attempted on 2026-09-30, but its engine and
analyzer APIs were incompatible, so it did not produce a valid lint result.
The build and tests must not be treated as proof of zero SonarLint warnings.
