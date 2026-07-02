# Test Discovery Plugin

A Java-based discovery plugin for TLM (Trust Lifecycle Manager) that provides scanning capabilities including importing certificate data from other sources and importing user information.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Project Structure](#project-structure)
- [Building the Project](#building-the-project)
- [Configuration](#configuration)
- [Usage](#usage)
- [Development](#development)
- [CI/CD](#cicd)
- [Contributing](#contributing)
- [License](#license)

## 🔍 Overview

The Test Discovery Plugin is designed to integrate with DigiCert's Trust Lifecycle Manager (TLM) platform. It extends the `AbstractDiscoveryWorkflow` class and provides automated certificate management capabilities for both Windows and Linux environments.

## ✨ Features

- **Import Discovery Data**
  - Import asset and security scan data from external sources directly into your inventory

- **Fetch User Data**
  - Authenticate and fetch user and account data. This data helps to keep single connector per account

- **Cross-Platform Support**
  - Windows and Linux execution environments
  - Platform-specific environment configurations

- **Automated Workflows**
  - Test connection functionality
  - Configuration validation
  - Certificate lifecycle discovery

## 🛠 Prerequisites

- **Java 17** or higher
- **Maven 3.6+** for building
- **Git** for version control
- Access to DigiCert TLM Plugin SDK repository

### Dependencies

- DigiCert TLM Plugin SDK (`com.digicert.tlm:plugin-sdk:1.1`)
- SLF4J for logging
- Google Gson for JSON processing
- Lombok for code generation

## 📁 Project Structure

```
test-discovery-plugin/
├── .github/
│   └── workflows/
│       └── build-java-sdk.yml          # GitHub Actions CI/CD pipeline
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/discovery/
│   │   │       ├── MyDiscoveryPlugin.java      # Main plugin implementation
│   │   │       ├── MyDiscoveryPluginRunner.java # Entry point runner
│   │   │       ├── MyPluginConfiguration.java   # Configuration model
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
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Package the plugin
mvn clean package

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
- `target/tlm-plugin-example-discovery-1.0.jar` - The main plugin JAR
- `plugin-dist/tlm-plugin-example-discovery-1.0.zip` - Distribution package
- `plugin-dist/checksums` - SHA256 checksums for verification

## ⚙️ Configuration

### Plugin Configuration

The plugin uses `MyPluginConfiguration` class for configuration:

```java
@Data
public class MyPluginConfiguration {
    private String userName;      // Authentication username
    private String password;      // Authentication password
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

## 🚀 Usage

### Integration with TLM

1. Deploy the plugin ZIP file to your TLM environment
2. Configure the plugin through the TLM management interface
3. Set up discovery workflows using the plugin capabilities

### Available Workflow Operations

- **Test Connection**: Verify connectivity to target systems
- **Run Now**: Import asset, security scan data, user information from external sources
- **Refresh Configuration**: Update plugin configuration dynamically

## 🧑‍💻 Development

### Development Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/digicert/tlm-plugin-example-discovery.git
   cd tlm-plugin-example-discovery
   ```

2. **Import into IDE**
   - The project includes Eclipse configuration files (`.project`, `.classpath`)
   - Can also be imported into IntelliJ IDEA or VS Code

3. **Configure Maven settings**
   - Ensure access to DigiCert's GitHub package repository
   - Set up authentication in your Maven settings

### Code Structure

- **MyDiscoveryPlugin**: Main plugin class extending `AbstractDiscoveryWorkflow`
- **MyDiscoveryPluginRunner**: Entry point for standalone execution
- **MyPluginConfiguration**: Configuration data model

### Adding New Features

1. Extend the `MyDiscoveryPlugin` class with new workflow methods
2. Update the configuration model if needed
3. Add corresponding tests
4. Update documentation

### Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=MyDiscoveryPluginTest

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

- **v1.0.0** - Initial release with basic discovery workflow capabilities

---

**Note**: This plugin is designed to work with DigiCert's Trust Lifecycle Manager platform. Ensure you have the necessary access and licensing before deployment.
