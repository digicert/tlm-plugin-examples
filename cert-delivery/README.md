# TLM Certificate Delivery Plugin Example

A sample DigiCert Trust Lifecycle Manager (TLM) plugin that demonstrates how to implement a certificate delivery workflow. This plugin provides functionality for generating Certificate Signing Requests (CSRs) and downloading/installing certificates from DigiCert's certificate delivery service.

## Table of Contents

- [TLM Certificate Delivery Plugin Example](#tlm-certificate-delivery-plugin-example)
  - [Table of Contents](#table-of-contents)
  - [Overview](#overview)
  - [Features](#features)
  - [Architecture](#architecture)
  - [Prerequisites](#prerequisites)
  - [Project Structure](#project-structure)
  - [Getting Started](#getting-started)
    - [Clone the Repository](#clone-the-repository)
    - [Configure GitHub Token](#configure-github-token)
    - [Build the Plugin](#build-the-plugin)
  - [Configuration](#configuration)
    - [Plugin Metadata (plugin-meta.json)](#plugin-metadata-plugin-metajson)
    - [UI Configuration (configuration.json)](#ui-configuration-configurationjson)
    - [Extended Configuration](#extended-configuration)
  - [Plugin Actions](#plugin-actions)
    - [generateCsr](#generatecsr)
    - [installCertificate (dispatchCertificate)](#installcertificate-dispatchcertificate)
  - [Dynamic Parameters from TLM Cloud](#dynamic-parameters-from-tlm-cloud)
    - [What Are Dynamic Parameters?](#what-are-dynamic-parameters)
    - [How They Arrive in the Plugin](#how-they-arrive-in-the-plugin)
    - [Real-World Example: Full Request with Dynamic Parameters](#real-world-example-full-request-with-dynamic-parameters)
    - [Mapping Dynamic Parameters by Extending the Request DTO](#mapping-dynamic-parameters-by-extending-the-request-dto)
      - [Step 1: Define a POJO for Nested Parameters (if needed)](#step-1-define-a-pojo-for-nested-parameters-if-needed)
      - [Step 2: Create an Extended Request Class](#step-2-create-an-extended-request-class)
      - [Step 3: Deserialize to the Extended Class in Your Plugin](#step-3-deserialize-to-the-extended-class-in-your-plugin)
      - [Complete File Layout](#complete-file-layout)
      - [Supported Dynamic Parameter Types](#supported-dynamic-parameter-types)
  - [Customization](#customization)
    - [Changing Plugin Name](#changing-plugin-name)
    - [Adding New Configuration Fields](#adding-new-configuration-fields)
    - [Modifying CSR Generation](#modifying-csr-generation)
  - [GitHub Actions Workflow](#github-actions-workflow)
    - [Setting Up Secrets](#setting-up-secrets)
    - [Triggering a Build](#triggering-a-build)
    - [Release Artifacts](#release-artifacts)
  - [Development](#development)
    - [Running Locally](#running-locally)
    - [Testing the Plugin](#testing-the-plugin)
  - [Troubleshooting](#troubleshooting)
    - [Build Failures](#build-failures)
    - [Runtime Issues](#runtime-issues)
    - [Logging](#logging)
  - [Dependencies](#dependencies)
  - [License](#license)
  - [Support](#support)

---

## Overview

This plugin extends `AbstractCertificateDeliveryWorkflow` from the TLM Plugin SDK to implement a certificate delivery workflow. It handles:

1. **CSR Generation**: Creates a new RSA/EC key pair and generates a CSR with configurable subject DN and Subject Alternative Names (SANs)
2. **Certificate Installation**: Downloads certificate packages from DigiCert and extracts end-entity and intermediate CA certificates

The plugin is packaged as a fat JAR, distributed as a ZIP archive, and executed by the TLM Sensor on the target machine (Windows or Linux). Communication between TLM Cloud and the plugin is orchestrated through the Sensor, which passes JSON payloads to the plugin's workflow actions.

## Features

- CSR generation with configurable key algorithm (RSA, EC), key size, and signature algorithm
- Subject Alternative Names (SANs) support: DNS names, IP addresses, email addresses
- Proper handling of special characters in Distinguished Names (DN) using Bouncy Castle
- Certificate download from DigiCert certificate delivery URLs via SDK utility with proxy support
- Automatic extraction of end-entity and ICA certificates from ZIP packages
- Secure storage of private keys and certificates in a temporary directory
- Cross-platform support (Windows and Linux)
- Support for dynamic parameters sent from TLM Cloud during Admin Web Requests (AWR)

## Architecture

```
┌──────────────┐       JSON payloads       ┌───────────┐      executes       ┌──────────────────────┐
│  TLM Cloud   │ ──────────────────────── > │  Sensor   │ ─────────────── > │  Plugin (fat JAR)    │
│  (AWR)       │                            │   │                    │  MyCertDeliveryPlugin│
└──────────────┘                            └───────────┘                    └──────────────────────┘
      │                                                                            │
      │  Sends: standard fields + dynamic params                                   │  Returns: CSR / cert
      └────────────────────────────────────────────────────────────────────────────┘
```

**Flow:**
1. TLM Cloud initiates an Admin Web Request (AWR).
2. The Sensor receives the request and invokes the plugin JAR.
3. The plugin receives a `JsonNode` request containing both standard fields (e.g. `subjectDn`, `keyAlgorithm`) and any **dynamic parameters** configured in the AWR profile.
4. The plugin deserializes the request into an extended DTO that includes both standard and dynamic parameter fields.
5. The plugin processes the request, generates CSR or installs certificates, and returns the response.

## Prerequisites

- **Java 17** or later
- **Maven 3.6+** (or use the included Maven wrapper `./mvnw` / `mvnw.cmd`)
- Internet access to `https://digicert.github.io/tlm-plugins-sdk-dist` (the public SDK repository — no authentication required)

## Project Structure

```
tlm-plugin-example-certdelivery/
├── .github/
│   └── workflows/
│       └── build-java-sdk.yml              # GitHub Actions CI/CD workflow
├── .mvn/
│   └── wrapper/
│       └── maven-wrapper.properties        # Maven wrapper configuration
├── src/
│   ├── main/
│   │   ├── java/com/example/certdelivery/
│   │   │   ├── MyCertDeliveryPlugin.java           # Main plugin – generateCsr & installCertificate
│   │   │   ├── MyCertDeliveryPluginHelper.java     # Helper – CSR generation, cert extraction, downloads
│   │   │   ├── MyCertDeliveryPluginRunner.java     # Entry point – bootstraps SDK runtime
│   │   │   └── extended/configuration/
│   │   │       └── MyPluginConfiguration.java      # POJO for config_attributes (username, password)
│   │   └── resources/
│   │       └── plugin-meta.json                    # Plugin metadata (name, version, execution config)
│   └── test/java/com/example/                      # Test classes
├── plugin-dist/                    # Build output – distributable ZIP
│   └── checksums                   # SHA-256 checksums of build artifacts
├── build.sh                        # Linux/macOS build script with token validation
├── configuration.json              # UI configuration schema for TLM portal
├── plugin-fat-jar.xml              # Maven Assembly – fat JAR descriptor
├── pom.xml                         # Maven project definition
├── settings.xml                    # Maven settings – GitHub Packages authentication
└── zip.xml                         # Maven Assembly – ZIP distribution descriptor
```

## Getting Started

### Clone the Repository

```bash
git clone https://github.com/digicert/tlm-plugin-example-certdelivery.git
cd tlm-plugin-example-certdelivery
```

### Get the SDK

The TLM Plugin SDK is hosted on a public GitHub Pages Maven repository at
`https://digicert.github.io/tlm-plugins-sdk-dist`. **No authentication is required** — the included
`settings.xml` already points Maven at this repository, so there is nothing to configure.

### Build the Plugin

**Using the build script (Linux/macOS):**
```bash
chmod +x build.sh
./build.sh
```

The build script runs the Maven build and generates SHA-256 checksums in `plugin-dist/checksums` after a successful build.

**Using Maven directly (Windows):**
```powershell
.\mvnw.cmd clean package -s settings.xml -U
```

**Using Maven directly (Linux/macOS):**
```bash
./mvnw clean package -s settings.xml -U
```

After a successful build, the plugin artifacts will be in:
- `target/test-certdelivery-plugin-1.0-SNAPSHOT.jar` — The fat JAR (all dependencies bundled)
- `plugin-dist/test-certdelivery-plugin-1.0-SNAPSHOT.zip` — The distributable ZIP package for upload to TLM

---

## Configuration

### Plugin Metadata (plugin-meta.json)

The `plugin-meta.json` file in `src/main/resources/` defines how the Sensor executes the plugin. Maven token replacement fills in `${project.artifactId}` and `${project.version}` at build time.

```json
{
  "plugin": {
    "name": "${project.artifactId}",
    "version": "${project.version}",
    "executions": [
      {
        "os": "windows",
        "type": "exe",
        "exe": "java",
        "args": ["-jar", "${project.artifactId}-${project.version}.jar"],
        "env": [
          "JAVA_HOME=${TG_JAVA_HOME}",
          "Path=${JAVA_HOME}\\bin;${Path}",
          "MAX_CHUNK=15",
          "PROCESS_TIMEOUT=30m"
        ]
      },
      {
        "os": "linux",
        "type": "exe",
        "exe": "java",
        "args": ["-jar", "${project.artifactId}-${project.version}.jar"],
        "env": [
          "JAVA_HOME=${TG_JAVA_HOME}",
          "PATH=${JAVA_HOME}/bin:${PATH}",
          "MAX_CHUNK=15",
          "PROCESS_TIMEOUT=30m"
        ]
      }
    ]
  }
}
```

| Field             | Description                                         |
| ----------------- | --------------------------------------------------- |
| `name`            | Plugin artifact name (used by Sensor for discovery) |
| `version`         | Plugin version                                      |
| `os`              | Target operating system (`windows` or `linux`)      |
| `type`            | Execution type (`exe`)                              |
| `exe`             | Executable to invoke (`java`)                       |
| `args`            | Command-line arguments passed to the executable     |
| `env`             | Environment variables set before execution          |
| `MAX_CHUNK`       | Maximum number of concurrent workflow items         |
| `PROCESS_TIMEOUT` | Timeout for the plugin process (e.g. `30m`)         |

### UI Configuration (configuration.json)

The `configuration.json` file defines the UI schema for configuring the plugin in the TLM portal. It includes metadata, core settings (sensor selection), config settings (credentials), and credential sets (for sensitive field encryption).

```json
{
  "metadata": {
    "author": "John Doe",
    "email": "John.Doe@digicert.com"
  },
  "core_settings": [
    {
      "name": "core_attributes.sensor_id",
      "label": "sensor_id",
      "type": "select",
      "editable": true,
      "optional": false,
      "size": "l",
      "validation": [{ "rule": "alphaNumericRule" }],
      "options_provider": "Sensors",
      "required": true
    }
  ],
  "config_settings": [
    {
      "name": "config_attributes.userName",
      "label": "username",
      "type": "input",
      "editable": true,
      "optional": false,
      "size": "l",
      "validation": [{ "rule": "requiredRule" }],
      "required": true
    },
    {
      "name": "config_attributes.password",
      "label": "password",
      "type": "password",
      "editable": true,
      "optional": false,
      "size": "l",
      "validation": [{ "rule": "requiredRule" }],
      "required": true
    }
  ],
  "additional_settings": [],
  "credential_sets": [
    {
      "name": "sensitive",
      "fields": [
        { "name": "config_attributes.password" }
      ]
    }
  ]
}
```

| Section               | Purpose                                                  |
| --------------------- | -------------------------------------------------------- |
| `metadata`            | Author and contact information                           |
| `core_settings`       | Required platform settings (e.g. sensor selection)       |
| `config_settings`     | Plugin-specific settings entered by the user             |
| `additional_settings` | Optional extra settings (empty in this example)          |
| `credential_sets`     | Fields that should be encrypted at rest (e.g. passwords) |

### Extended Configuration

The `MyPluginConfiguration` class is a Lombok `@Data` POJO that maps to the `config_attributes` from `configuration.json`:

```java
@Data
public class MyPluginConfiguration {
  private String userName;
  private byte[] password;
}
```

> **Note:** The `password` field is `byte[]` because credential-set fields are delivered as encrypted byte arrays by the SDK.

Access the extended configuration in the plugin constructor:

```java
public MyCertDeliveryPlugin(SdkContext context, PluginConfiguration<MyPluginConfiguration> configuration) {
  Optional.ofNullable(configuration.getExtendedConfig()).ifPresent(cfg -> {
    String username = cfg.getUserName();
    byte[] password = cfg.getPassword();
    // Use credentials for authentication with external systems
  });
}
```

---

## Plugin Actions

> **Important:** The `generateCsr` and `installCertificate` methods shown here are **example implementations**. You must provide your own implementation of these methods based on your specific certificate delivery requirements. The SDK defines the abstract contract via `AbstractCertificateDeliveryWorkflow`; how you generate keys, build CSRs, and handle certificate installation is entirely up to you.

### generateCsr

Generates a Certificate Signing Request (CSR) with a new key pair.

> This example uses Bouncy Castle to generate the key pair and CSR. You should replace or adapt this logic to suit your own key management and CSR generation requirements.

**Method:** `generateCsr(JsonNode request)`

**Example Input:**
```json
{
  "subjectDn": "CN=windowsagent.test\u0000L=CA\u0000ST=CA\u0000O=Digicert\u0000C=US\u0000emailAddress=testacc@mail.com",
  "dnsNames": "windowsagent.test",
  "signatureAlgorithm": "sha256",
  "keyAlgorithm": "RSA",
  "keySize": "2048",
  "flowId": "df571b3f-153c-406a-ad47-ca729f2e7ed5",
  "connectorId": "0af36868-bcbf-4967-aed8-a742ad7a332e",
  "ipAddresses": "",
  "emails": ""
}
```

**Standard Parameters:**

| Field                | Type   | Description                                                   | Example                                      |
| -------------------- | ------ | ------------------------------------------------------------- | -------------------------------------------- |
| `subjectDn`          | String | Subject DN with fields separated by `\u0000` (null character) | `"CN=example.com\u0000O=DigiCert\u0000C=US"` |
| `keyAlgorithm`       | String | Key algorithm (`RSA` or `EC`)                                 | `"RSA"`, `"EC"`                              |
| `keySize`            | String | Key size in bits                                              | `"2048"`, `"4096"`                           |
| `signatureAlgorithm` | String | Hash algorithm for signing                                    | `"sha256"`, `"sha384"`, `"sha512"`           |
| `dnsNames`           | String | Comma-separated DNS names for SAN                             | `"example.com,www.example.com"`              |
| `ipAddresses`        | String | Comma-separated IP addresses for SAN                          | `"192.168.1.1"`                              |
| `emails`             | String | Comma-separated emails for SAN                                | `"admin@example.com"`                        |
| `flowId`             | String | Unique workflow identifier (UUID) generated by the system     | `"df571b3f-153c-406a-ad47-ca729f2e7ed5"`     |
| `connectorId`        | String | Connector identifier from TLM                                 | `"0af36868-bcbf-4967-aed8-a742ad7a332e"`     |

**Subject DN Format:**

The `subjectDn` field uses the null character (`\u0000`) as the field separator. Supported attribute types:

| Attribute     | OID                    | Aliases                                 |
| ------------- | ---------------------- | --------------------------------------- |
| Common Name   | `2.5.4.3`              | `CN`, `COMMONNAME`                      |
| Country       | `2.5.4.6`              | `C`, `COUNTRYNAME`                      |
| State         | `2.5.4.8`              | `ST`, `STATE`, `STATEORPROVINCENAME`    |
| Locality      | `2.5.4.7`              | `L`, `LOCALITY`, `LOCALITYNAME`         |
| Organization  | `2.5.4.10`             | `O`, `ORGANIZATION`, `ORGANIZATIONNAME` |
| Org Unit      | `2.5.4.11`             | `OU`, `ORGANIZATIONALUNIT`              |
| Email Address | `1.2.840.113549.1.9.1` | `E`, `EMAIL`, `EMAILADDRESS`            |

**Output:**
- `GenerateCsrResponse` containing the CSR in PEM format

**Files Created:**
- `<tempDir>/MyCertDeliveryPlugin/<flowId>/request.csr` — The generated CSR
- `<tempDir>/MyCertDeliveryPlugin/<flowId>/request.key` — The private key

---

### installCertificate (dispatchCertificate)

Downloads and installs a certificate from DigiCert's certificate delivery service.

> You must implement this method yourself based on how your target system expects certificates to be installed. For **downloading** the certificate package from the `certificateLink` URL, the SDK provides a built-in utility — `DownloadCertificateUtil` — that handles the HTTP download with proxy support. You can use it as shown in this example and focus your custom logic on what happens after the download (e.g. installing into a key store, writing to a specific directory, invoking a system API, etc.).

**Method:** `installCertificate(JsonNode request)`

**Example Input:**
```json
{
  "certificateLink": "https://demo.one.digicert.com/mpki/api/v1/ts/artifact/060c12d8-4c2d-4c77-8d20-ee99b9ae70e6",
  "flowId": "31c9bcef-3f6a-46fc-b1a1-c5ce51243ac8",
  "filename": "06feb202601.ssltechs.net"
}
```

**Parameters:**

| Field             | Type   | Description                                             | Example                                  |
| ----------------- | ------ | ------------------------------------------------------- | ---------------------------------------- |
| `certificateLink` | String | URL to download the certificate ZIP package             | `"https://demo.one.digicert.com/..."`    |
| `flowId`          | String | Unique workflow identifier (UUID), matches the CSR step | `"31c9bcef-3f6a-46fc-b1a1-c5ce51243ac8"` |
| `filename`        | String | Base filename for saving certificates                   | `"06feb202601.ssltechs.net"`             |

**Output:**
- `DispatchCertificateResponse` containing end-entity and ICA certificate content (PEM format)

**Files Created:**
- `<tempDir>/MyCertDeliveryPlugin/<flowId>/certificate.zip` — Downloaded certificate package
- `<tempDir>/MyCertDeliveryPlugin/<flowId>/<filename>.crt` — End-entity certificate
- `<tempDir>/MyCertDeliveryPlugin/<flowId>/<filename>_ica.crt` — Intermediate CA certificate

**Expected ZIP Structure:**
| Entry Pattern | Description                 |
| ------------- | --------------------------- |
| `*.cer`       | End-entity certificate      |
| `*_ica.cer`   | Intermediate CA certificate |
| `*.p7b`       | PKCS#7 bundle (skipped)     |

---

## Dynamic Parameters from TLM Cloud

### What Are Dynamic Parameters?

When TLM Cloud sends an **Admin Web Request (AWR)** to the plugin via the Sensor, the JSON payload can include **dynamic parameters** in addition to the standard fields. These are custom key-value pairs configured in the AWR profile on TLM Cloud that get appended to the request JSON at runtime.

Dynamic parameters allow you to pass arbitrary, profile-specific data to your plugin. They can be of any JSON type: strings, integers, booleans, or nested objects.

### How They Arrive in the Plugin

The plugin's action methods receive a `JsonNode request` parameter. Standard fields like `subjectDn`, `keyAlgorithm`, etc. are part of the SDK's `GenerateCsrRequest` DTO. **Dynamic parameters are additional fields present in the same JSON object** that the SDK DTO does not know about.

To handle them, you **extend the SDK's request DTO** (e.g. `GenerateCsrRequest`) with your own class that declares fields matching the dynamic parameter names. The SDK's `PluginUtils.convertWrappedObject()` will then deserialize both the standard and dynamic fields into your extended class automatically.

### Real-World Example: Full Request with Dynamic Parameters

Here is a real log entry showing a `generateCsr` request that includes three dynamic parameters (`int_param`, `basic_param`, `nested_param`) sent from TLM Cloud during an AWR:

```
2026-02-16 22:20:44:065 [main] INFO com.example.certdelivery.MyCertDeliveryPlugin -
  GenerateCsr request: {
    "subjectDn": "CN=xcddummy.com\u0000L=lehi\u0000O=ab\u0000C=US\u0000emailAddress=ab@ab.com",
    "dnsNames": "xcddummy.com",
    "signatureAlgorithm": "sha256",
    "keyAlgorithm": "RSA",
    "keySize": "2048",
    "flowId": "cc202e3a-6e42-412a-a994-fbfc2f3b3da6",
    "connectorId": "0af36868-bcbf-4967-aed8-a742ad7a332e",
    "ipAddresses": "",
    "emails": "",
    "int_param": 1,
    "basic_param": "basic value",
    "nested_param": {
      "attr_1": "aab"
    }
  }
```

In the above request:

| Parameter      | Type    | Value               | Description                            |
| -------------- | ------- | ------------------- | -------------------------------------- |
| `int_param`    | Integer | `1`                 | A simple integer dynamic parameter     |
| `basic_param`  | String  | `"basic value"`     | A simple string dynamic parameter      |
| `nested_param` | Object  | `{"attr_1": "aab"}` | A nested JSON object dynamic parameter |

### Mapping Dynamic Parameters by Extending the Request DTO

The recommended approach is to **extend** the SDK's `GenerateCsrRequest` class with your own class that declares fields for each dynamic parameter. The SDK's deserialization will automatically populate both the inherited standard fields and your custom fields.

#### Step 1: Define a POJO for Nested Parameters (if needed)

If any dynamic parameter is a nested JSON object, create a POJO for it:

```java
package com.example.certdelivery.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class NestedParam {
  @JsonProperty("attr_1")
  private String attr1;
}
```

#### Step 2: Create an Extended Request Class

Extend `GenerateCsrRequest` and add fields matching the dynamic parameter names from the AWR payload. Use `@JsonProperty` when the JSON key contains underscores or differs from the Java field name:

```java
package com.example.certdelivery.dto;

import com.digicert.tlm.workflows.certdelivery.dto.GenerateCsrRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MyGenerateCsrRequest extends GenerateCsrRequest<JsonNode> {

  /** A simple integer dynamic parameter. */
  @JsonProperty("int_param")
  private Integer intParam;

  /** A simple string dynamic parameter. */
  @JsonProperty("basic_param")
  private String basicParam;

  /** A nested object dynamic parameter. */
  @JsonProperty("nested_param")
  private NestedParam nestedParam;
}
```

> **Note:** `@JsonIgnoreProperties(ignoreUnknown = true)` ensures that any additional fields not declared in your class are silently ignored instead of causing deserialization errors.

#### Step 3: Deserialize to the Extended Class in Your Plugin

In your plugin's action method, use `PluginUtils.convertWrappedObject()` with your extended class instead of the base `GenerateCsrRequest`:

```java
@Override
public Response<JsonNode> generateCsr(JsonNode request) throws WorkflowExecutionException {
  try {
    log.info("GenerateCsr request: {}", objectMapper.writeValueAsString(request));

    // Deserialize into extended DTO — both standard AND dynamic fields are populated
    MyGenerateCsrRequest csrRequest = PluginUtils.convertWrappedObject(
      request, MyGenerateCsrRequest.class
    );

    // Access standard fields (inherited from GenerateCsrRequest)
    String subjectDn = csrRequest.getSubjectDn();
    String keyAlgorithm = csrRequest.getKeyAlgorithm();
    log.info("Subject DN: {}, Key Algorithm: {}", subjectDn, keyAlgorithm);

    // Access dynamic parameters (declared in MyGenerateCsrRequest)
    log.info("int_param: {}", csrRequest.getIntParam());        // 1
    log.info("basic_param: {}", csrRequest.getBasicParam());    // "basic value"

    if (csrRequest.getNestedParam() != null) {
      log.info("nested_param.attr_1: {}", csrRequest.getNestedParam().getAttr1()); // "aab"
    }

    // Use dynamic params in your business logic
    // For example, route to different key stores, set custom headers, etc.

    // ... rest of CSR generation logic ...
  } catch (Exception e) {
    log.error("Failed to generate CSR", e);
    throw new WorkflowExecutionException("Failed to generate CSR: " + e.getMessage(), e);
  }
}
```

#### Complete File Layout

After adding the extended DTOs, your project structure looks like:

```
src/main/java/com/example/certdelivery/
├── MyCertDeliveryPlugin.java
├── MyCertDeliveryPluginHelper.java
├── MyCertDeliveryPluginRunner.java
├── dto/
│   ├── MyGenerateCsrRequest.java      # Extends GenerateCsrRequest with dynamic params
│   └── NestedParam.java               # POJO for the nested_param object
└── extended/configuration/
    └── MyPluginConfiguration.java
```

#### Supported Dynamic Parameter Types

| JSON Type | Java Field Type       | Example JSON Value  | Example Java Access                      |
| --------- | --------------------- | ------------------- | ---------------------------------------- |
| String    | `String`              | `"basic value"`     | `csrRequest.getBasicParam()`             |
| Integer   | `Integer` / `int`     | `1`                 | `csrRequest.getIntParam()`               |
| Long      | `Long` / `long`       | `9999999999`        | `csrRequest.getLongParam()`              |
| Double    | `Double` / `double`   | `3.14`              | `csrRequest.getDoubleParam()`            |
| Boolean   | `Boolean` / `boolean` | `true`              | `csrRequest.getFlagParam()`              |
| Object    | Custom POJO           | `{"attr_1": "aab"}` | `csrRequest.getNestedParam().getAttr1()` |
| Array     | `List<T>`             | `["a", "b", "c"]`   | `csrRequest.getListParam()`              |
| Any       | `JsonNode` (fallback) | any                 | `csrRequest.getRawParam()`               |

> **Tip:** If you do not know the parameter names at build time or they vary per AWR profile, you can declare a `Map<String, Object>` field annotated with `@JsonAnySetter` to capture all unmapped fields dynamically.

---

## Customization

### Changing Plugin Name

1. **Update `pom.xml`:**
   ```xml
   <artifactId>my-custom-plugin</artifactId>
   <name>my-custom-plugin</name>
   ```

2. **Update the `@WorkflowEntryPoint` annotation in `MyCertDeliveryPlugin.java`:**
   ```java
   @WorkflowEntryPoint(name = "MyCustomPlugin")
   public class MyCertDeliveryPlugin extends AbstractCertificateDeliveryWorkflow {
   ```

3. **Update the temp directory name** in `generateCsr` and `installCertificate` methods:
   ```java
   String pluginName = "MyCustomPlugin";
   ```

4. **Update the runner class** and package as needed.

### Adding New Configuration Fields

1. **Add field to `MyPluginConfiguration.java`:**
   ```java
   @Data
   public class MyPluginConfiguration {
     private String userName;
     private byte[] password;
     private String serverUrl;  // New field
   }
   ```

2. **Add corresponding entry in `configuration.json`:**
   ```json
   {
     "name": "config_attributes.serverUrl",
     "label": "Server URL",
     "type": "input",
     "editable": true,
     "optional": false,
     "size": "l",
     "validation": [{ "rule": "requiredRule" }],
     "required": true
   }
   ```

3. **If the field is sensitive**, add it to the `credential_sets` section:
   ```json
   {
     "name": "sensitive",
     "fields": [
       { "name": "config_attributes.password" },
       { "name": "config_attributes.serverUrl" }
     ]
   }
   ```

### Modifying CSR Generation

The `MyCertDeliveryPluginHelper` class contains all CSR generation logic:

- **Change key generation algorithm**: Modify `generateCSRWithSANs()` method
- **Add custom X.509 extensions**: Use Bouncy Castle's `ExtensionsGenerator` to add extensions before building the CSR
- **Change DN attribute handling**: Modify `buildX500Name()` and `getOidForAttributeType()` to support additional OIDs
- **Modify signature algorithm mapping**: Update `buildSignatureAlgorithmName()` for additional algorithms

---

## GitHub Actions Workflow

The project includes a GitHub Actions workflow (`.github/workflows/build-java-sdk.yml`) that:

1. Checks out the code and sets up JDK 17 (Temurin)
2. Builds the plugin using Maven via `build.sh`
3. Scans build artifacts with VirusTotal for malware
4. Creates a draft GitHub release with checksums and scan report

### Setting Up Secrets

Configure these secrets in your GitHub repository (Settings > Secrets and variables > Actions):

| Secret         | Description                              |
| -------------- | ---------------------------------------- |
| `GITHUB_TOKEN` | Automatically provided by GitHub Actions |
| `VT_API_KEY`   | VirusTotal API key for malware scanning  |

### Triggering a Build

The workflow uses `workflow_dispatch`, meaning you trigger it manually:

1. Go to your repository on GitHub
2. Click on **Actions** tab
3. Select **Maven Java Plugin publish** workflow
4. Click **Run workflow**
5. Select the branch and click **Run workflow**

### Release Artifacts

After a successful build, the workflow creates a draft release containing:

- `test-certdelivery-plugin-1.0-SNAPSHOT.zip` — Plugin distribution package
- `checksums` — SHA-256 checksums for verification
- `vt-report.md` — VirusTotal scan report

---

## Development

### Running Locally

You can test the plugin locally using the runner class:

```bash
# Build first
./mvnw clean package -s settings.xml -U

# Run the plugin (it starts the SDK runtime and waits for input)
java -jar target/test-certdelivery-plugin-1.0-SNAPSHOT.jar
```

### Testing the Plugin

The entry point is `MyCertDeliveryPluginRunner`:

```java
public class MyCertDeliveryPluginRunner {
  public static void main(String[] args) throws IOException {
    // 1. Initialize the SDK runtime with the package name (lowercase)
    SdkRuntime runtime = new SdkRuntime(
      MyCertDeliveryPluginRunner.class.getPackageName().toLowerCase()
    );

    // 2. Create a new context object
    SdkContext context = new SdkContext();

    // 3. Execute the plugin
    runtime.execute("MyCertDeliveryPlugin", context);
  }
}
```

To test specific actions, the SDK runtime reads JSON from stdin. You can pipe test payloads:

```bash
echo '{"action":"generateCsr","data":{"subjectDn":"CN=test.com","keyAlgorithm":"RSA","keySize":"2048","signatureAlgorithm":"sha256","dnsNames":"test.com","ipAddresses":"","emails":"","flowId":"test-flow-id"}}' | java -jar target/test-certdelivery-plugin-1.0-SNAPSHOT.jar
```

---

## Troubleshooting

### Build Failures

**Error: "Could not resolve dependencies"**
- Confirm the SDK repository URL in `settings.xml` is correct: `https://digicert.github.io/tlm-plugins-sdk-dist`
- Ensure the build passes `-s settings.xml` so Maven uses the bundled repository configuration
- Check network connectivity to `https://digicert.github.io` (no authentication is required)

### Runtime Issues

**CSR generation fails with "No such provider: BC"**
- The Bouncy Castle provider is registered via a `static` block in `MyCertDeliveryPluginHelper`
- Verify `bcprov-jdk18on` and `bcpkix-jdk18on` are included in the fat JAR

**Certificate download fails**
- Verify the `certificateLink` URL is accessible from the machine running the plugin
- Check network connectivity and firewall/proxy settings
- The SDK's `DownloadCertificateUtil` supports proxy configurations

**Dynamic parameters not appearing in the deserialized request**
- Verify the AWR (Admin Web Request) profile on TLM Cloud has the parameters configured correctly
- Ensure your extended request class (e.g. `MyGenerateCsrRequest`) declares fields with `@JsonProperty` annotations matching the JSON key names exactly
- Add `@JsonIgnoreProperties(ignoreUnknown = true)` to avoid failures from unexpected fields
- Check plugin logs — the raw `JsonNode` is logged at INFO level before deserialization

### Logging

The plugin uses SLF4J with `slf4j-simple` as the backend. Enable debug logging by setting the environment variable:

```bash
PLUGIN_LOG_LVL=DEBUG   # Enforced by the Sensor when the Sensor itself is set to DEBUG
```

All request/response payloads are logged at INFO level by default:
```
GenerateCsr request: {"subjectDn":"CN=...","keyAlgorithm":"RSA",...,"int_param":1,"basic_param":"basic value"}
```

---

## Dependencies

| Dependency       | Version   | Purpose                                     |
| ---------------- | --------- | ------------------------------------------- |
| `plugin-sdk`     | `1.1`     | TLM Plugin SDK (workflows, DTOs, utilities) |
| `lombok`         | `1.18.30` | Reduces boilerplate (`@Data`, `@Slf4j`)     |
| `slf4j-simple`   | `2.0.9`   | Logging implementation                      |
| `gson`           | `2.8.9`   | JSON serialization (used by SDK internals)  |
| `bcprov-jdk18on` | `1.79`    | Bouncy Castle cryptographic provider        |
| `bcpkix-jdk18on` | `1.79`    | Bouncy Castle PKIX (CSR generation)         |
| `commons-lang3`  | `3.14.0`  | `Pair` utility class                        |

---

## License

This project is provided as an example for DigiCert TLM plugin development. Please refer to DigiCert's licensing terms for the TLM Plugin SDK.

---

## Support

For questions about:
- **TLM Plugin SDK**: Contact DigiCert support
- **This example plugin**: Open an issue in the repository
