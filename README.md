# spring-trace

> A tiny Spring Boot starter that **automatically logs the call tree of slow requests** — which method ate the time, how deep, what percent — with zero APM vendor, zero agent, zero code changes beyond an annotation.

[![CI](https://github.com/SraavanChevireddy/spring-trace/actions/workflows/ci.yml/badge.svg)](https://github.com/SraavanChevireddy/spring-trace/actions/workflows/ci.yml)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-6DB33F.svg)](https://spring.io/projects/spring-boot)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

"The `/checkout` endpoint is sometimes slow" is the start of a long afternoon. You add log lines, redeploy, wait for it to happen again. **spring-trace** flips that around: drop the starter in, annotate the methods you care about, and any request slower than your threshold prints its full timeline to the log automatically — the first time it happens.

```
SLOW REQUEST (612ms >= 500ms threshold):
Trace: GET /checkout  (612ms total)
  - GET /checkout: 612ms (self 22ms)  [100%]
    - CheckoutService.checkout: 590ms (self 40ms)  [96%]
      - InventoryClient.reserve: 120ms  [19%]
      - PaymentClient.charge: 410ms  [66%]
      - OrderRepository.save: 20ms  [3%]
```

At a glance: 66% of the request was the payment call. No dashboards, no sampling, no per-span billing.

## How it works

- A servlet filter opens a **trace** per HTTP request (thread-local).
- An AOP aspect times every `@Traced` bean method and **nests** it under the currently-running span, reconstructing the real call tree.
- When the request finishes, if it was slower than `slow-request-threshold-millis`, the tree is formatted and logged (with self-time and % of total per node).

It's all plain Spring + AspectJ — no bytecode agent, no native hooks.

## Install

Build and install locally (until it's on Maven Central):

```bash
git clone https://github.com/SraavanChevireddy/spring-trace.git
cd spring-trace && ./mvnw clean install
```

Then add the dependency:

```xml
<dependency>
    <groupId>com.sraavan</groupId>
    <artifactId>spring-trace</artifactId>
    <version>0.1.0</version>
</dependency>
```

The starter auto-configures itself — no `@Enable` annotation needed.

## Usage

Annotate the methods you want on the timeline:

```java
@Service
public class CheckoutService {

    @Traced                          // span name = "CheckoutService.checkout"
    public Receipt checkout(Cart cart) { ... }

    @Traced("payment:charge")        // custom span name
    public void charge(Card card) { ... }
}
```

Every HTTP request is already the root span, so you immediately see endpoint-level timing even before annotating anything.

## Configuration

```yaml
spring-trace:
  enabled: true                       # master switch (default true)
  slow-request-threshold-millis: 500  # only log traces at least this slow
```

Set the threshold to `0` to log every request (handy in dev).

## Why not just use an APM?

Datadog/New Relic/etc. are great — and expensive, and overkill when you just want to know *which method is slow* in a service you're actively debugging. spring-trace is the 80/20: the call tree you actually look at, printed to the logs you already have, for free. Keep your APM for production; reach for this when you're iterating.

## Roadmap / contributing

PR ideas: capture outbound `RestClient`/JDBC calls automatically (no annotation), export the tree as JSON, add a `@Traced` class-level option, configurable output sink. Keep the core dependency-light.

## License

[MIT](LICENSE) © Sraavan Chevireddy
