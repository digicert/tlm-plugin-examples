# Test Automation Plugin

A Java-based automation plugin for TLM (Trust Lifecycle Manager) that provides certificate lifecycle management capabilities including CSR generation, certificate validation, installation, and configuration management.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Project Structure](#project-structure)
- [Building the Project](#building-the-project)
- [Configuration](#configuration)
- [Secrets Manager & Authentication](#secrets-manager--authentication)
- [Usage](#usage)
- [Development](#development)
- [CI/CD](#cicd)
- [Contributing](#contributing)
- [License](#license)

## 🔍 Overview

The Test Automation Plugin is designed to integrate with DigiCert's Trust Lifecycle Manager (TLM) platform. It extends the `AbstractAutomationWorkflow` class and provides automated certificate management capabilities for both Windows and Linux environments.

## ✨ Features

- **Certificate Lifecycle Management**
  - Certificate Signing Request (CSR) generation
  - Self-signed certificate generation
  - Certificate validation and installation
  - Configuration refresh capabilities

- **Cross-Platform Support**
  - Windows and Linux execution environments
  - Platform-specific environment configurations

- **Security Features**
  - RSA 2048-bit key generation
  - SHA256withRSA signature algorithm
  - BouncyCastle cryptographic provider integration

- **Automated Workflows**
  - Test connection functionality
  - Configuration validation
  - Certificate lifecycle automation

## 🛠 Prerequisites

- **Java 17** or higher
- **Maven 3.6+** for building
- **Git** for version control
- Access to DigiCert TLM Plugin SDK repository

### Dependencies

- DigiCert TLM Plugin SDK (`com.digicert.tlm:plugin-sdk:1.0-SNAPSHOT`)
- BouncyCastle Cryptographic Library
- SLF4J for logging
- Google Gson for JSON processing
- Lombok for code generation

## 📁 Project Structure

```
test-automation-plugin/
├── .github/
│   └── workflows/
│       └── build-java-sdk.yml          # GitHub Actions CI/CD pipeline
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/automation/
│   │   │       ├── MyAutomationPlugin.java      # Main plugin implementation
│   │   │       ├── MyAutomationPluginRunner.java # Entry point runner
│   │   │       ├── MyPluginConfiguration.java   # Configuration model
│   │   │       ├── MyRefreshRequest.java        # Refresh request DTO
│   │   │       └── MyRefreshResponse.java       # Refresh response DTO
│   │   └── resources/
│   │       └── plugin-meta.json                 # Plugin metadata
│   └── test/
│       ├── java/                                # Test source directory
│       └── resources/                           # Test resources
├── target/                                      # Maven build output
├── plugin-dist/                                 # Distribution artifacts
├── build.sh                                     # Build script
├── pom.xml                                      # Maven configuration
├── settings.xml                                 # Maven settings
├── zip.xml                                      # Assembly configuration
└── README.md                                    # This file
```

## 🔨 Building the Project

### Using Maven

```bash
# Build with custom settings
mvn clean package -s settings.xml
```

### Using Build Script

```bash
# Make the script executable (Linux/macOS)
chmod +x build.sh

# Run the build script
./build.sh
```

The build script will:
1. Clean and package the project using Maven
2. Generate SHA256 checksums for distribution files
3. Create distribution artifacts in the `plugin-dist/` directory

### Build Artifacts

After building, you'll find:
- `target/test-automation-plugin-1.0.0.jar` - The main plugin JAR
- `plugin-dist/test-automation-plugin-1.0.0.zip` - Distribution package
- `plugin-dist/checksums` - SHA256 checksums for verification

## ⚙️ Configuration

### Plugin Configuration

The plugin uses `MyPluginConfiguration` class for configuration:

```java
@Data
public class MyPluginConfiguration {
    private String userName;      // Authentication username
    private String password;      // Authentication password
    private String managementIp;  // Management interface IP
    private String managementPort;// Management interface port
}
```

### Plugin Metadata

Configuration is defined in `plugin-meta.json`:

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
        "env": ["JAVA_HOME=${TG_JAVA_HOME}", "Path=${JAVA_HOME}\\bin;${Path}"]
      },
      {
        "os": "linux", 
        "type": "exe",
        "exe": "java",
        "args": ["-jar", "${project.artifactId}-${project.version}.jar"],
        "env": ["JAVA_HOME=${TG_JAVA_HOME}", "PATH=${JAVA_HOME}/bin:${PATH}"]
      }
    ]
  }
}
```

### Environment Variables

The plugin supports the following environment variables:

- `TG_JAVA_HOME` - Java installation path
- `MAX_CHUNK` - Maximum chunk size (default: 15)
- `PROCESS_TIMEOUT` - Process timeout (default: 30m)

## 🔐 Secrets Manager & Authentication

This section is the primary reference for **third‑party developers** who want to build their
own TLM automation plugin using this repository as a template. It explains how the plugin
authenticates to a managed system, how secrets are declared and delivered, and what you must
take care of when you customize the authentication for your own target system.

There are two complementary approaches:

1. **TLM built‑in credential handling** – the mechanism this reference plugin uses today.
   Credentials are declared in `configuration.json`, stored/encrypted by TLM's secrets
   manager, and injected into your plugin at runtime.
2. **External secrets manager integration** – the mode selected via the
   `authentication_method` field. Instead of the literal secret, the operator picks a
   registered **Secrets Manager (PAM) connector** and enters a **vault reference**; the
   connector (BeyondTrust, CyberArk, or Delinea) resolves it and injects the secret at runtime.

### 1. Built‑in Credential Handling (used by this plugin)

#### How it works end‑to‑end

```
configuration.json (schema)  ─▶  TLM UI renders fields  ─▶  TLM Secrets Manager
        (declares fields)              (operator input)        (encrypts "sensitive" fields)
                                                                        │
                                                                        ▼
   MyPluginConfiguration  ◀──  PluginConfiguration.getExtendedConfig()  ◀──  values injected
      (typed POJO)                    (at plugin execution)                  (decrypted)
```

1. **Declare** each credential/connection field in `configuration.json`.
2. TLM renders those fields in the management UI and collects operator input.
3. Fields marked as `sensitive` are encrypted and stored by TLM's **secrets manager**.
4. At execution time TLM decrypts and injects the values into your plugin as a typed object,
   available via `PluginConfiguration.getExtendedConfig()` (see `MyAutomationPlugin`
   constructor).

#### `configuration.json` structure

The schema drives the entire authentication surface. In this reference plugin:

| Section               | Purpose                                                                                   | Example in this repo                          |
| --------------------- | ----------------------------------------------------------------------------------------- | --------------------------------------------- |
| `metadata`            | Author/ownership information.                                                              | author, email                                 |
| `core_settings`       | Platform‑level attributes bound to `core_attributes.*`.                                    | `core_attributes.sensor_id`                   |
| `config_settings`     | Connection/authentication fields bound to `config_attributes.*`.                          | authentication_method, userName, password, managementIp, managementPort, pam_connector_id |
| `additional_settings` | Optional extra fields.                                                                     | (empty)                                        |
| `credential_sets`     | Marks how fields are treated by the secrets manager (`sensitive`) and uniqueness (`unique`). | password → sensitive, managementIp → unique   |

Each field in `config_settings` maps **one‑to‑one** to a property on the extended
configuration POJO (`MyPluginConfiguration`):
```json
{
  "name": "config_attributes.password",
  "label": "password",
  "type": "password",
  "editable": true,
  "optional": false,
  "validation": [ { "rule": "requiredRule" } ],
  "required": true
}
```

```java
@Data
public class MyPluginConfiguration {
    private String userName;       // config_attributes.userName
    private String password;       // config_attributes.password   (secret)
    private String managementIp;   // config_attributes.managementIp
    private String managementPort; // config_attributes.managementPort
}
```

#### `credential_sets` — the secrets contract

```json
{
  "credential_sets": [
    { "name": "sensitive", "fields": [ { "name": "config_attributes.password" } ] },
    { "name": "unique",    "fields": [ { "name": "config_attributes.managementIp" } ] }
  ]
}
```

- **`sensitive`** – every field listed here is treated as a secret by TLM: encrypted at rest,
  masked in the UI, and never returned in plaintext to unauthorized callers. **Put every
  password, token, API key, or private key field here.**
- **`unique`** – fields that uniquely identify a managed target/connection. TLM uses these to
  prevent duplicate connections and to correlate a configuration with a specific system.

#### Consuming credentials at runtime

TLM injects the decrypted configuration into the plugin constructor:

```java
public MyAutomationPlugin(SdkContext context,
                          PluginConfiguration<MyPluginConfiguration> configuration) {
    MyPluginConfiguration cfg = configuration.getExtendedConfig();
    // cfg.getUserName(), cfg.getPassword(), cfg.getManagementIp(), cfg.getManagementPort()
    // Use these to authenticate to your managed system.
}
```

### 2. External Secrets Manager Integration (via `authentication_method`)

> **⚠️ Prerequisite — add the Secrets Manager (PAM) connector first.**
> If your plugin connector uses the **Secrets Manager** authentication method, you must first
> add the **external Secrets Manager (PAM) connector** in TLM. Once it exists, TLM
> **automatically detects** it and lists it in the `secrets_manager_connector` dropdown
> (populated by the `PamConnectors` options provider). Adding the plugin connector before the
> PAM connector is registered leaves the dropdown empty (nothing to detect/bind to).
>
> **Order of operations:**
> 1. Add the external Secrets Manager (PAM) connector.
> 2. Add your developer plugin connector, set **Authentication method → *Self-authentication
>    (Secrets manager)*** — TLM auto-detects the PAM connector from step 1.
> 3. Enter the username and the **PAM vault reference** in the secret field(s).

#### Schema-driven authentication switch

The reference `configuration.json` exposes an **`authentication_method`** selector with two
options, and uses `conditional_group` to show the right fields for each choice:

| `authentication_method` value              | Fields shown (`conditional_group`)                         | Secret handling                                  |
| ------------------------------------------ | ---------------------------------------------------------- | ------------------------------------------------ |
| `Self authentication` (Direct input)       | `userName`, `password` (`type: password`, masked)          | Operator types the real secret; stored by TLM.   |
| `Self authentication secrets manager`      | `pam_connector_id`, `userName`, `password` (`type: input`) | Operator enters a **PAM vault reference**; TLM/PAM connector resolves it at runtime. |

```json
{
  "name": "config_attributes.authentication_method",
  "type": "select",
  "options": [
    { "value": "Self authentication",                 "label": "Self-authentication (Direct input)" },
    { "value": "Self authentication secrets manager",  "label": "Self-authentication (Secrets manager)" }
  ],
  "value": "Self authentication"
}
```

#### Selecting the PAM connector and entering references

When the *Secrets manager* mode is chosen, the `pam_connector_id` field (label
`secrets_manager_connector`) is shown. It is a `select` populated by the `PamConnectors`
options provider — this is where TLM's **auto-detected** connectors appear. Provider-specific
`conditional_banners` tell the operator which reference format to use:

| Provider (`match_field: provider`) | Vault reference format                                             |
| ---------------------------------- | ----------------------------------------------------------------- |
| **BeyondTrust**                    | `SystemName/AccountName`                                           |
| **CyberArk**                       | The object / secret name                                          |
| **Delinea**                        | The numeric secret id, optionally followed by `/field-slug`       |

The secret field then carries help text such as *"Enter the PAM vault reference that contains
the password. For BeyondTrust, use the format SystemName/AccountName."*

#### Consuming the resolved secret at runtime

**No plugin code changes are required for this.** The reference `MyPluginConfiguration` POJO
keeps its original four fields (`userName`, `password`, `managementIp`, `managementPort`) — the
`authentication_method` and `pam_connector_id` fields are consumed by TLM/the UI to drive the
flow and are **not** needed on the POJO. The connector (BeyondTrust, CyberArk, Delinea) has its
own plugin that connects to the vault, fetches the secret for the given reference, and TLM
injects the **resolved secret into the existing `password` field** via a ticket at runtime. Your
plugin reads it exactly as before:

```java
public MyAutomationPlugin(SdkContext context,
                          PluginConfiguration<MyPluginConfiguration> configuration) {
    MyPluginConfiguration cfg = configuration.getExtendedConfig();

    // In "Secrets manager" mode, the PAM connector has already resolved the
    // reference (e.g. "SystemName/AccountName") and TLM injected the real secret here:
    String secret = cfg.getPassword();

    // authenticate to the managed system using `secret`
}
```

> This means enabling secrets-manager auth is a **`configuration.json`-only change** — TLM
> sends the required parameters to the existing plugin POJO. Because the PAM connector performs
> the resolution, your plugin needs no direct vault connectivity. The SDK still ships proxy
> support (`com.digicert.tlm.plugin.ProxyInitializer`) for any other outbound calls; honor
> `PROCESS_TIMEOUT` and the standard proxy environment.

### ✅ What third‑party developers must take care of

- **Keep names in sync.** Every `config_attributes.<name>` in `configuration.json` must match a
  field on your extended configuration POJO. A mismatch means the value silently arrives as
  `null`. (This rule is called out in the `MyPluginConfiguration` Javadoc.)
- **Declare every secret in `credential_sets.sensitive`.** Anything not listed there may be
  stored/displayed as plaintext.
- **Use `"type": "password"` for real secrets** (Direct-input mode) so the UI masks them. For
  **Secrets manager** mode the field holds a *reference* (not the secret itself), so it uses
  `"type": "input"` with descriptive help text — see the two `password` entries in
  `configuration.json`.
- **Drive visibility with `conditional_group`.** Gate each field on the chosen
  `authentication_method` value so operators only see the fields relevant to their mode.
- **Add POJO fields only for values you actually read in code.** In this reference plugin the
  secret arrives in the existing `password` field, so **no POJO/code change is needed** for
  secrets-manager auth — it is a `configuration.json`-only change. Add a property (e.g.
  `authenticationMethod`) only if your own plugin logic needs to branch on it; otherwise TLM
  simply ignores unmapped fields on the POJO.
- **Mark identity fields as `unique`** so TLM can de‑duplicate connections.
- **Use the correct PAM reference format** per provider: BeyondTrust `SystemName/AccountName`,
  CyberArk object/secret name, Delinea numeric secret id (optionally `/field-slug`). Surface
  this with `conditional_banners` (as in `configuration.json`) so operators enter it correctly.
- **Handle rotation & failure.** TLM resolves the reference at runtime, so rotation in the
  vault is picked up automatically — don't cache the resolved value longer than needed, and
  fail closed with a clear `WorkflowExecutionException` if a secret cannot be resolved.
- **Mind FIPS & crypto providers.** This plugin registers BouncyCastle; keep your crypto and
  TLS settings consistent with the target environment (see `MAX_CHUNK`, `PROCESS_TIMEOUT`).
- **Re‑generate the distribution** after changing `configuration.json` or the POJO, and keep
  the two shipped together (see [Building the Project](#building-the-project)).

### 🔌 Provider-specific details (BeyondTrust, CyberArk, Delinea)

The Secrets Manager (PAM) connector — a separate plugin selected via `pam_connector_id` — is
what actually talks to the vault. As a plugin developer you mainly need to know **(a) the
reference format your operators must enter** and **(b) the vault-side setup/permissions** so a
reference resolves. The connectors retrieve the credential at runtime and TLM injects the
resolved value into your `password` field (fail-fast: if resolution fails, your plugin does not
run).

> TLS note: connectors default `verifySsl=false` (trust-all) for test convenience. **Set it to
> `true` for any real deployment.**

#### BeyondTrust — Password Safe

- **Reference format:** `SystemName/AccountName` (split on the first `/`; both halves required),
  e.g. `TestF5/svc-f5`. Directory accounts use UPN or `Domain\AccountName`.
- **How the connector retrieves** (REST base `…/BeyondTrust/api/public/v3`, auth header
  `Authorization: PS-Auth key=<apiKey>; runas=<userName>; pwd=[<password>];`):
  `POST /Auth/SignAppin` → `GET /ManagedAccounts?systemName=&accountName=` →
  `POST /Requests` → `GET /Credentials/{requestId}` → `PUT /Requests/{requestId}/Checkin` →
  `POST /Auth/Signout`.
- **Vault setup / permissions:** register an API application key; grant the `runas` user
  credential-retrieval (Requestor) rights on the target Managed System/Account; the account
  must be **Enabled for API Access**; use an **auto-approve access policy** so `POST /Requests`
  does not block (otherwise `GET /Credentials` returns *"request not yet approved"*).

#### CyberArk — Central Credential Provider (CCP / AIM)

- **Reference format:** a single `objectName` (the Safe is fixed in the connector config, so no
  `/` split), e.g. `TestPasswordCredential`.
- **How the connector retrieves:**
  `GET /AIMWebService/api/Accounts?AppID=<appId>&Safe=<safeName>&Object=<objectName>` (mutual
  TLS if a client certificate is configured); the secret is the response **`Content`** field.
- **Vault setup / permissions:** create a CCP **Application ID**; add it as a Safe member with
  **Retrieve accounts**; configure its allowed authentication (client certificate / allowed
  machines-IP / OS user) to match the sensor host. For mTLS, export the client cert+key as a
  PKCS#12 and base64-encode it into the connector's `clientCertificate`.
- **Note:** CCP is the *agentless web service* (what this uses). The locally-installed
  *Credential Provider (AIM CP)* uses a local SDK/CLI instead of REST.

#### Delinea — Secret Server (standalone) & Platform

- **Reference format:** `{secretId}/{fieldSlug}` — `secretId` is the **numeric** Secret Server
  id (required); `fieldSlug` defaults to `password` when omitted, e.g. `42/password`,
  `42/username`, or `100`.
- **How the connector retrieves:**
  - *Standalone:* `POST /oauth2/token` (`grant_type=password`, optional `domain`) →
    `GET /api/v1/secrets/{secretId}/fields/{fieldSlug}`.
  - *Platform:* `POST /identity/api/oauth2/token/xpmplatform`
    (`grant_type=client_credentials`, scope `xpmheadless`) → vault discovery
    `GET /vaultbroker/api/vaults` → secret field fetch on the discovered Secret Server.
- **Vault setup / permissions:** *Standalone* — a service account with **View/Read** on the
  target Secret and Web Services / ROPC enabled. *Platform* — an OAuth **client-credentials**
  app (Service User) with `xpmheadless`, Vault Broker discovery rights, and read access to the
  target Secret.

### 🧭 End-to-end walkthrough (this plugin)

This is the full path to see secrets-manager auth working with **this** example plugin — no
plugin code changes, only the `configuration.json` in this repo:

1. **Add the PAM connector.** In TLM, add the external Secrets Manager (PAM) connector for your
   provider (BeyondTrust / CyberArk / Delinea) and supply its vault connection (base URL, app
   key / app id / client credentials, TLS). Run its **Test Connection** to confirm the vault is
   reachable and authorized.
2. **Build & upload this plugin.** `mvn clean package -s settings.xml` and deploy
   `plugin-dist/test-automation-plugin-1.0.0.zip` (JAR + `plugin-meta.json`) to TLM.
3. **Add the plugin connector** and set **Authentication method → *Self-authentication (Secrets
   manager)***. The `secrets_manager_connector` dropdown (populated by the `PamConnectors`
   provider) now lists the connector from step 1 — select it.
4. **Enter the reference, not the secret.** In the username/`password` fields enter the
   provider-specific **vault reference** (e.g. `TestF5/svc-f5` for BeyondTrust). The
   `conditional_banners` in `configuration.json` show the correct format per provider.
5. **Bind a sensor** (see below) and **run a workflow** (e.g. Test Connection / Generate CSR).
   TLM runs the PAM `fetchSecret` task first, then injects the resolved secret into this
   plugin's `password` field.
6. **Verify:** the workflow succeeds, `cfg.getPassword()` inside the plugin holds the real
   secret, and **no raw secret** ever appears in the plugin configuration.

### 🧪 Testing, permissions & sensor setup

**Sensor setup (required for a real run):**

- A **sensor** must be installed and registered in TLM and reachable; once registered it
  appears in the `Sensors` options provider (bound via `core_attributes.sensor_id` in
  `configuration.json`).
- The sensor host must have **network egress** to the vault `baseUrl` (open firewall / proxy),
  and must **trust the vault's TLS CA** (or use `verifySsl:false` only for internal test PKI).
- The sensor runs the plugin as `java -jar <artifact>.jar <EntryPoint>`, so **Java 17** must be
  available on the sensor host (`TG_JAVA_HOME`).

**Permissions checklist:**

| Area | What's needed |
| ---- | ------------- |
| TLM | Rights to add a PAM connector, add/configure the plugin connector, and run automation workflows. |
| BeyondTrust | API key + `runas` user with credential-retrieval rights and an **auto-approve** policy on the target account. |
| CyberArk | CCP **App ID** authorized on the Safe with **Retrieve**; auth method (client cert / IP / OS user) matching the sensor host. |
| Delinea | *Standalone:* service account with Read on the Secret + ROPC enabled. *Platform:* client-credentials app with `xpmheadless` + vault read. |

**Testing tiers:**

1. **Connector Test Connection** — validate the PAM connector reaches and authenticates to the
   vault before wiring up this plugin.
2. **Reference resolution** — with the plugin set to *Secrets manager* mode, run a lightweight
   workflow (Test Connection) and confirm the resolved secret is injected (log a redacted
   confirmation, never the value).
3. **Full workflow** — run Generate CSR / Install Certificate end to end to confirm the plugin
   authenticates to the managed system using the resolved credential.
4. **Negative test** — point the reference at a non-existent object/account and confirm the
   workflow fails fast with a clear error and **does not** fall through with an empty secret.

## 🚀 Usage

### Integration with TLM

1. Deploy the plugin ZIP file to your TLM environment
2. Configure the plugin through the TLM management interface
3. Set up automation workflows using the plugin capabilities

### Available Workflow Operations

- **Test Connection**: Verify connectivity to target systems
- **Generate CSR**: Create certificate signing requests
- **Validate Certificate**: Verify certificate properties and validity
- **Install Certificate**: Deploy certificates to target systems
- **Refresh Configuration**: Update plugin configuration dynamically

## 🧑‍💻 Development

### Development Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/digicert/tlm-plugin-example-automation.git
   cd tlm-plugin-example-automation
   ```

2. **Import into IDE**
   - The project includes Eclipse configuration files (`.project`, `.classpath`)
   - Can also be imported into IntelliJ IDEA or VS Code

3. **Configure Maven settings**
   - Ensure access to DigiCert's GitHub package repository
   - Set up authentication in your Maven settings

### Code Structure

- **MyAutomationPlugin**: Main plugin class extending `AbstractAutomationWorkflow`
- **MyAutomationPluginRunner**: Entry point for standalone execution
- **MyPluginConfiguration**: Configuration data model
- **MyRefreshRequest/Response**: DTOs for configuration refresh operations

### Adding New Features

1. Extend the `MyAutomationPlugin` class with new workflow methods
2. Update the configuration model if needed
3. Add corresponding tests
4. Update documentation

### Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=MyAutomationPluginTest

# Run with verbose output
mvn test -X
```

## 🔄 CI/CD

The project includes GitHub Actions workflow for automated building and deployment:

### Workflow Features

- **Automated Building**: Triggers on workflow dispatch
- **Security Scanning**: VirusTotal integration for malware detection
- **Artifact Generation**: Creates distribution packages
- **Release Management**: Automated draft releases with checksums

### Workflow Configuration

Located in `.github/workflows/build-java-sdk.yml`:

- Uses Ubuntu latest runner
- Java 17 with Temurin distribution
- Maven build with custom settings
- VirusTotal scanning for security
- Automated release creation

### Manual Trigger

The workflow can be manually triggered from the GitHub Actions tab in the repository.

## 🤝 Contributing

1. **Fork the repository**
2. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. **Make your changes**
4. **Add tests** for new functionality
5. **Ensure all tests pass**
   ```bash
   mvn test
   ```
6. **Submit a pull request**

### Coding Standards

- Follow Java naming conventions
- Use Lombok annotations for reducing boilerplate
- Add appropriate logging using SLF4J
- Include unit tests for new features
- Update documentation as needed

### Pull Request Guidelines

- Provide clear description of changes
- Include test coverage for new features
- Ensure CI/CD pipeline passes
- Update documentation if applicable

## 📄 License

This project is licensed under the terms specified by DigiCert. Please refer to the license file or contact DigiCert for licensing information.

## 📞 Support

For support and questions:

- Create an issue in the GitHub repository
- Contact the DigiCert TLM team
- Refer to the TLM Plugin SDK documentation

## 🔖 Version History

- **v1.0.0** - Initial release with basic automation workflow capabilities

---

**Note**: This plugin is designed to work with DigiCert's Trust Lifecycle Manager platform. Ensure you have the necessary access and licensing before deployment.
