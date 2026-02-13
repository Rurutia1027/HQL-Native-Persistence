# Jaeger vs Zipkin Comparison

This project uses **Jaeger** as the trace backend in Docker. Here's how it differs from Zipkin and why both are still
relevant.

---

## In a sentence

- Zipkin: Simple, lightweight trace server; very common with Spring and Brave.
- Jaeger: Cloud-native,CNCF tracing platform; built for scale and Kubernetes; also accepts Zipkin-style spans.

--- 

## Origin and ecosystem

|                 | Zipkin                                     | Jaeger                                                 |
|-----------------|--------------------------------------------|--------------------------------------------------------|
| **Origin**      | OpenZipkin (Twitter-inspired, open-source) | Uber, then CNCF                                        |
| **Governance**  | Community (OpenZipkin)                     | **CNCF graduated** project                             |
| **Typical use** | Single process or small deployment         | Distributed (collectors, storage, query) or all-in-one |

--- 

## Deployment and instrumentation

- **Zipkin**: HTTP/JSON (and Thrift). **Brave** (Java) speaks Zipkin natively; Spring Boot with Micrometer Tracing ofte
  uses the Zipkin reporter.
- **Jaeger**: Jaeger Thrift, gRPC, and **OTLP** (OpenTelemetry). Aligns with OpenTelemetry SDKs and the wider
  cloud-native stack.

**Important**: Jaeger all-in-one can **accept Zipkin-formated spans** (e.g., on port 9411). So we can keep sending
traces in Zipkin format from Brave/Micrometer and view them in the Jaeger UI-no need to switch instrumentation
immediately.


--- 

## When to pick which

- **Choose Zipkin**: when you want the smallest possible trace server and are very happy with a single process and
  Zipkin's UI/API.
- **Choose Jaeger**: when you care about cloud-native tooling, Kubernetes, scaling collectors/storage, or
  OTLP/OpenTelemetry, and still want a single container for local/dev (all-in-one).

In this repo we use **Jaeger** in Docker so the stack is cloud-native-oriented and ready for OTLP later, while remaining
compatible with Zipkin-style spans (e.g., form Spring/Brave) on port 9411.

---

## References

- [Jaeger](https://www.jaegertracing.io/) — docs, APIs, deployment.
- [Zipkin](https://zipkin.io/) — API and Brave instrumentation.
- [Jaeger – Zipkin compatibility](https://www.jaegertracing.io/docs/latest/apis/#zipkin) — sending Zipkin-format spans
  to Jaeger.

