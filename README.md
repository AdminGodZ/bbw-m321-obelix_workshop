# Obelix & Co. Webshop 🪨

The webshop coordinates purchases on port 8080. The quarry owns menhirs and its
database on port 8081. The basket service owns the basket in memory on port 8082.
The original menhir and basket endpoints remain available on port 8080 and
forward requests to the appropriate service.

The six Gradle modules are `obelix-common`, `obelix-quarry-api`,
`obelix-quarry-impl`, `obelix-basket-api`, `obelix-basket-impl`, and `obelix-webshop`.
The API modules contain the shared contracts and DTOs; the implementation modules
run as separate applications. The webshop depends on both API modules and talks
to the other applications through Spring HTTP proxies.

## Run locally

Install Java 25 and start Docker Desktop. Start Zipkin from the project folder:

```sh
docker compose up -d zipkin
```

If Docker is unavailable, Zipkin also provides a standalone Java server.
Follow the [Zipkin Java quickstart](https://zipkin.io/pages/quickstart.html#java)
and run its executable JAR with `java -jar zipkin.jar` instead.

Start the quarry, basket and webshop in three separate terminals:

```sh
./gradlew :obelix-quarry-impl:bootRun
```

```sh
./gradlew :obelix-basket-impl:bootRun
```

```sh
./gradlew :obelix-webshop:bootRun
```

## Follow a purchase in Zipkin

All three applications use Micrometer with the OpenTelemetry bridge and send traces
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
   It should contain spans from `webshop`, `quarry`, and `basket`, including our custom
   spans `webshop.purchase`, `quarry.find-menhir`, `quarry.delete-menhir`,
   `basket.read`, and `basket.clear`.
   The `menhir.id` tag identifies the selected stone. Allow a few seconds for export.

The custom spans wrap the existing operations with `Observation.observe()`.
It starts and closes the span, and records an error if the operation throws.
The shared settings are in `obelix-common/src/main/resources/application-common.yaml`.
Set `ZIPKIN_ENDPOINT` to send traces to a different Zipkin server.
Change `quarry.base-url` to use a quarry at a different address.
Change `basket.base-url` to use a basket service at a different address.

The basket service offers `GET /api/basket`, `PUT /api/basket/offer`, and
`DELETE /api/basket`. The webshop forwards these routes. You can inspect the
current basket at <http://localhost:8080/api/basket> or directly at
<http://localhost:8082/api/basket>.

As in the original exercise, there is one shared basket for Obelix. The basket
service stores its state in memory, so restarting it empties the basket.
The webshop calculates the offer's value, asks the quarry to remove the purchased
stone, and then asks the basket service to clear the accepted offer. A rejected
offer leaves the basket untouched. These HTTP calls are separate operations;
this exercise does not add a transaction spanning the three services.

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

Run the tests and build all three executable applications with:

```sh
./gradlew test bootJar
```

The tests cover purchasing, the original webshop URLs, basket offer/read/reset,
invalid basket input, quarry inventory, and the Prometheus gauge changing after deletion.

A local SonarLint scan on 2026-10-03 checked all 22 Java source and test files in
the six modules using Java 25 and the default standalone Java rules. The final
result was zero issues, with no unresolved types or missing file results.
The scan used SonarSource's language server 6.0.2.79770 and Java analyzer
8.44.0.48651, with the project's compiled classes and dependency classpaths.
A temporary control file produced the expected `java:S106` warning, confirming
that the analyzer was active. No rules were disabled to obtain the result.

[The scan report](quality/sonarlint-report.json) records the versions and hashes
of the scanned files. It is a snapshot of those files, not a check of future edits.
The scan tools ran separately from the editor, so Zed can remain the only IDE.
