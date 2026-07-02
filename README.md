# TLM Plugin Examples

Reference plugin implementations for the **DigiCert Trust Lifecycle Manager (TLM) extensibility SDK**.

This repository is a curated collection of runnable, well-documented sample plugins that show 3rd‑party
developers how to extend TLM across its three primary plugin surfaces: **certificate delivery**,
**automation**, and **discovery**. Each example lives in its own folder, builds independently with Maven,
and packages into a distributable ZIP that can be uploaded to a TLM tenant and executed by a TLM Sensor.

> **New here?** Pick the folder that matches your use case, fork it (or copy it out), and start
> customizing. See [Which Example Should I Fork?](#which-example-should-i-fork) below.

---

## Table of Contents

- [What Is a TLM Plugin?](#what-is-a-tlm-plugin)
- [How Plugins Run](#how-plugins-run)
- [The Examples](#the-examples)
  - [Certificate Delivery](#1-certificate-delivery--cert-delivery)
  - [Automation](#2-automation--automation)
  - [Discovery](#3-discovery--discovery)
- [Which Example Should I Fork?](#which-example-should-i-fork)
- [The TLM Plugin SDK](#the-tlm-plugin-sdk)
- [Prerequisites](#prerequisites)
- [Getting the SDK (GitHub Packages Auth)](#getting-the-sdk-github-packages-auth)
- [Quick Start](#quick-start)
- [Repository Layout](#repository-layout)
- [Common Concepts Across All Plugins](#common-concepts-across-all-plugins)
- [Building & Packaging](#building--packaging)
- [Deploying to TLM](#deploying-to-tlm)
- [Support & Resources](#support--resources)
- [License](#license)

---

## What Is a TLM Plugin?

A TLM plugin is a self‑contained Java program that TLM invokes to perform a specific job on your
infrastructure — issuing a CSR, installing a certificate onto a target host, or scanning an environment
for existing certificates. Plugins let you connect TLM to systems, appliances, and workflows that are
specific to *your* organization without waiting on a built‑in integration.

Every plugin in this repository:

- Extends an **abstract workflow base class** provided by the TLM Plugin SDK.
- Is packaged as a **fat JAR** (all dependencies bundled) and distributed as a **ZIP**.
- Ships a **`configuration.json`** UI schema (rendered in the TLM portal) and a **`plugin-meta.json`**
  execution descriptor (read by the Sensor).
- Runs cross‑platform on **Windows and Linux**.

## How Plugins Run

```
┌──────────────┐      JSON request       ┌───────────┐      executes       ┌────────────────────┐
│  TLM Cloud   │ ─────────────────────►  │  Sensor   │ ─────────────────►  │  Plugin (fat JAR)  │
│              │                         │           │                     │  extends Abstract  │
│              │ ◄─────────────────────  │           │ ◄─────────────────  │  *Workflow         │
└──────────────┘      JSON response      └───────────┘      returns        └────────────────────┘
```

1. TLM Cloud initiates a request (e.g. an enrollment, an automation job, or a discovery scan).
2. The **Sensor** — deployed in your network — receives the request and launches the plugin JAR,
   passing a JSON payload on the process boundary.
3. The plugin deserializes the request, runs your custom logic, and returns a JSON response that the
   Sensor relays back to TLM Cloud.

Because the Sensor handles all transport, your plugin only needs to implement the workflow methods
defined by its SDK base class.

---

## The Examples

### 1. Certificate Delivery — [`cert-delivery/`](./cert-delivery)

**Base class:** `AbstractCertificateDeliveryWorkflow` · **SDK:** `plugin-sdk:1.1`

Demonstrates a full certificate delivery flow driven by **Admin Web Requests (AWR)**:

- **`generateCsr`** — creates an RSA/EC key pair and builds a CSR with a configurable subject DN and
  Subject Alternative Names (DNS, IP, email) using Bouncy Castle.
- **`installCertificate`** — downloads a certificate package from a DigiCert delivery URL (via the
  SDK's `DownloadCertificateUtil`, with proxy support) and extracts the end‑entity and ICA
  certificates.

It is also the **most detailed reference** in this repo for handling **dynamic parameters** — custom
key/value data sent from TLM Cloud in the AWR payload — by extending the SDK request DTOs. If you need
to pass profile‑specific data into a plugin, start here.

> **Use it when:** you need TLM to deliver/install certificates into a target system that isn't natively
> supported, or you need fine‑grained control over CSR generation and key handling.

### 2. Automation — [`automation/`](./automation)

**Base class:** `AbstractAutomationWorkflow` · **SDK:** `plugin-sdk:1.1`

Demonstrates end‑to‑end certificate lifecycle automation against a target system:

- Test connection to the target.
- Generate CSRs and self‑signed certificates.
- Validate and install certificates.
- Refresh configuration dynamically (`MyRefreshRequest` / `MyRefreshResponse` DTOs).

> **Use it when:** you want to automate provisioning, rotation, and installation of certificates onto
> appliances, servers, or services (e.g. load balancers, network devices, custom endpoints).

### 3. Discovery — [`discovery/`](./discovery)

**Base class:** `AbstractDiscoveryWorkflow` · **SDK:** `plugin-sdk:1.1`

Demonstrates importing external data into your TLM inventory:

- **Import discovery data** — bring asset and security‑scan data from external sources into your
  certificate inventory.
- **Fetch user data** — authenticate and import user/account data (useful for keeping a single
  connector per account).

> **Use it when:** you have certificate or asset data in a system TLM can't scan directly and you want
> to feed it into TLM's inventory for visibility and management.

---

## Which Example Should I Fork?

| Your goal | Fork this folder | Base workflow class | Key methods |
| --- | --- | --- | --- |
| Deliver/install certificates into a custom target; control CSR & key generation | [`cert-delivery/`](./cert-delivery) | `AbstractCertificateDeliveryWorkflow` | `generateCsr`, `installCertificate` |
| Automate the full lifecycle (issue → validate → install → rotate) on a target system | [`automation/`](./automation) | `AbstractAutomationWorkflow` | test connection, generate/validate/install, refresh |
| Import certificate/asset/user data from external sources into TLM inventory | [`discovery/`](./discovery) | `AbstractDiscoveryWorkflow` | run‑now import, fetch users, refresh |
| Learn how to pass **dynamic parameters** from TLM Cloud into a plugin | [`cert-delivery/`](./cert-delivery) | — | (see its README's "Dynamic Parameters" section) |

**How to fork:** each folder is a standalone Maven project. You can fork this repository and develop in
the relevant subfolder, or copy a single folder out into its own repository. Rename the artifact,
package, and `@WorkflowEntryPoint` name to match your plugin, then implement the workflow methods with
your own logic — the SDK defines the contract, but the actual key management, target integration, and
data mapping are entirely yours.

---

## The TLM Plugin SDK

All examples depend on the **TLM Plugin SDK**, distributed via GitHub Packages from the public
distribution repository:

### 👉 [digicert/tlm-plugins-sdk-dist](https://github.com/digicert/tlm-plugins-sdk-dist)

That repository is the source of truth for the SDK artifact (`com.digicert.tlm:plugin-sdk`), its
versions, the abstract workflow base classes, request/response DTOs, and utilities such as
`PluginUtils` and `DownloadCertificateUtil`. Refer to it for SDK setup, available versions, API
reference, and release notes.

---

## Prerequisites

- **Java 17** or later
- **Maven 3.6+** (each project also ships the Maven wrapper where applicable — `./mvnw` / `mvnw.cmd`)
- A **GitHub Personal Access Token** with the `read:packages` scope, to download the SDK from
  GitHub Packages

## Getting the SDK (GitHub Packages Auth)

The SDK is served from GitHub Packages, so Maven must authenticate. Each project includes a
`settings.xml` that points at `https://maven.pkg.github.com/digicert/tlm-plugins-sdk-dist` and reads
your credentials from environment variables:

**Linux/macOS**
```bash
export GITHUB_ACTOR="your-github-username"
export GITHUB_TOKEN="your-personal-access-token"   # needs read:packages scope
```

**Windows (PowerShell)**
```powershell
$env:GITHUB_ACTOR = "your-github-username"
$env:GITHUB_TOKEN = "your-personal-access-token"
```

> Generate a token at **GitHub → Settings → Developer settings → Personal access tokens** with the
> `read:packages` scope. See [tlm-plugins-sdk-dist](https://github.com/digicert/tlm-plugins-sdk-dist)
> for the authoritative SDK access instructions.

## Quick Start

```bash
# 1. Clone this repository
git clone https://github.com/digicert/tlm-plugin-examples.git
cd tlm-plugin-examples

# 2. Authenticate to GitHub Packages (see above)
export GITHUB_ACTOR="your-github-username"
export GITHUB_TOKEN="your-personal-access-token"

# 3. Build the example you care about (e.g. cert-delivery)
cd cert-delivery
mvn clean package -s settings.xml -U
```

The build produces a fat JAR under `target/` and a distributable ZIP under `plugin-dist/`, ready to
upload to TLM. See each folder's README for the exact artifact names and options.

## Repository Layout

```
tlm-plugin-examples/
├── cert-delivery/        # Certificate delivery example (AbstractCertificateDeliveryWorkflow)
│   ├── src/              # Plugin, helper, runner, extended config & DTOs
│   ├── configuration.json# TLM portal UI schema
│   ├── pom.xml           # Maven project (fat JAR + ZIP assembly)
│   ├── settings.xml      # GitHub Packages auth for the SDK
│   └── README.md         # Deep-dive: CSR generation, install, dynamic parameters
├── automation/           # Automation example (AbstractAutomationWorkflow)
│   ├── src/
│   ├── configuration.json
│   ├── pom.xml
│   ├── settings.xml
│   └── README.md
├── discovery/            # Discovery example (AbstractDiscoveryWorkflow)
│   ├── src/
│   ├── configuration.json
│   ├── pom.xml
│   ├── settings.xml
│   └── README.md
├── LICENSE
└── README.md             # ← you are here
```

Each subfolder is an independent Maven build. There is intentionally **no parent aggregator POM** so
that any folder can be forked and developed in isolation.

## Common Concepts Across All Plugins

Every example shares the same building blocks — learn them once and they apply everywhere:

| File / Concept | Purpose |
| --- | --- |
| `plugin-meta.json` | Execution descriptor read by the Sensor: the `java -jar` command, per‑OS env, `MAX_CHUNK`, `PROCESS_TIMEOUT`. `${project.artifactId}` / `${project.version}` are filled at build time. |
| `configuration.json` | UI schema rendered in the TLM portal — `core_settings` (e.g. sensor selection), `config_settings` (user inputs), and `credential_sets` (fields encrypted at rest). |
| `MyPluginConfiguration` | A Lombok `@Data` POJO mapping the `config_attributes` from `configuration.json`. Sensitive fields (passwords) arrive as `byte[]`. |
| `*Runner` class | The `main()` entry point that bootstraps the `SdkRuntime` and executes the workflow. |
| `@WorkflowEntryPoint` | Annotation naming your workflow so the Sensor can locate and invoke it. |
| Fat JAR + `zip.xml` | Maven Assembly descriptors that bundle all dependencies and produce the uploadable ZIP. |

## Building & Packaging

Each project supports the same commands (run from inside the project folder):

```bash
# Maven directly
mvn clean package -s settings.xml -U

# or the build script (Linux/macOS) — also generates SHA-256 checksums
chmod +x build.sh
./build.sh

# or the Maven wrapper where provided
./mvnw clean package -s settings.xml -U      # Linux/macOS
.\mvnw.cmd clean package -s settings.xml -U  # Windows
```

Output artifacts:

- `target/<artifact>-<version>.jar` — the fat JAR (all dependencies bundled)
- `plugin-dist/<artifact>-<version>.zip` — the distributable package for upload to TLM
- `plugin-dist/checksums` — SHA‑256 checksums for verification

## Deploying to TLM

1. Build the plugin ZIP (above).
2. In the TLM portal, upload the ZIP and configure the plugin using the fields defined in
   `configuration.json` (including selecting the Sensor that will run it).
3. The Sensor downloads and executes the plugin according to `plugin-meta.json`.
4. Drive the plugin through the relevant TLM workflow (enrollment/AWR, automation profile, or
   discovery scan).

Refer to each folder's README and the DigiCert TLM product documentation for portal‑specific steps.

## Support & Resources

- **TLM Plugin SDK (artifacts, versions, API):**
  [digicert/tlm-plugins-sdk-dist](https://github.com/digicert/tlm-plugins-sdk-dist)
- **Per‑example deep dives:** the `README.md` inside
  [`cert-delivery/`](./cert-delivery), [`automation/`](./automation), and
  [`discovery/`](./discovery)
- **Questions / issues:** open an issue in this repository, or contact DigiCert support for
  SDK‑ and product‑level questions.

## License

These projects are provided as examples for DigiCert TLM plugin development. See [`LICENSE`](./LICENSE)
and refer to DigiCert's licensing terms for the TLM Plugin SDK.
