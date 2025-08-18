# Corporate Maven Repository Configuration Guide

## Overview

In corporate environments, direct access to Maven Central is often restricted. Here's how to configure Maven to use internal repositories.

## Configuration Locations

### 1. Global Configuration (Recommended for Companies)
**Location**: `~/.m2/settings.xml` (Linux/macOS) or `C:\Users\{username}\.m2\settings.xml` (Windows)

**Benefits**:
- ✅ Applies to all projects automatically
- ✅ Centralizes corporate repository configuration
- ✅ Doesn't require changes to project files
- ✅ Keeps credentials secure and separate from code

### 2. Project-Level Configuration
**Location**: `pom.xml` in your project root

**Benefits**:
- ✅ Project-specific repository requirements
- ✅ Version controlled with the project
- ❌ May expose repository URLs in code
- ❌ Credentials management is more complex

## Common Corporate Scenarios

### Scenario 1: Nexus Repository Manager
Most companies use Nexus Repository Manager to proxy Maven Central:

```xml
<!-- In ~/.m2/settings.xml -->
<mirrors>
    <mirror>
        <id>nexus</id>
        <mirrorOf>*</mirrorOf>
        <url>https://nexus.company.com/repository/maven-public/</url>
    </mirror>
</mirrors>
```

### Scenario 2: Artifactory
Companies using JFrog Artifactory:

```xml
<!-- In ~/.m2/settings.xml -->
<mirrors>
    <mirror>
        <id>artifactory</id>
        <mirrorOf>*</mirrorOf>
        <url>https://artifactory.company.com/artifactory/maven-virtual/</url>
    </mirror>
</mirrors>
```

### Scenario 3: Multiple Repositories
When you need both internal and external repositories:

```xml
<!-- In pom.xml -->
<repositories>
    <repository>
        <id>company-internal</id>
        <url>https://nexus.company.com/repository/maven-internal/</url>
    </repository>
    <repository>
        <id>company-public</id>
        <url>https://nexus.company.com/repository/maven-public/</url>
    </repository>
</repositories>
```

## Security Considerations

### 1. Credentials Management
**Never put credentials directly in pom.xml!** Use `settings.xml`:

```xml
<!-- In ~/.m2/settings.xml -->
<servers>
    <server>
        <id>corporate-nexus</id>
        <username>${env.NEXUS_USERNAME}</username>
        <password>${env.NEXUS_PASSWORD}</password>
    </server>
</servers>
```

### 2. Environment Variables
Set credentials as environment variables:

```bash
# Linux/macOS
export NEXUS_USERNAME="your-username"
export NEXUS_PASSWORD="your-password"

# Windows
set NEXUS_USERNAME=your-username
set NEXUS_PASSWORD=your-password
```

### 3. Encrypted Passwords
Maven supports password encryption:

```bash
# Generate master password
mvn --encrypt-master-password mypassword

# Encrypt server password
mvn --encrypt-password mypassword
```

## Docker Considerations

### Problem: Docker Build Context
When building in Docker, `~/.m2/settings.xml` isn't available.

### Solution 1: Copy Settings to Container
```dockerfile
# Copy corporate settings
COPY settings.xml /root/.m2/settings.xml
COPY --chown=185 mvnw /code/mvnw
COPY --chown=185 .mvn /code/.mvn
COPY --chown=185 pom.xml /code/pom.xml
```

### Solution 2: Use Build Args
```dockerfile
ARG NEXUS_URL=https://nexus.company.com/repository/maven-public/
ENV MAVEN_OPTS="-Dmaven.repo.remote=${NEXUS_URL}"
```

### Solution 3: Project-Level Configuration
Use the commented repository configuration in your `pom.xml` (already added to your project).

## Testing Your Configuration

### 1. Verify Repository Access
```bash
# Test dependency resolution
./mvnw dependency:resolve

# Verbose output to see which repositories are used
./mvnw dependency:resolve -X
```

### 2. Check Effective Settings
```bash
# Show effective settings
./mvnw help:effective-settings

# Show effective POM
./mvnw help:effective-pom
```

## Common Corporate Repository URLs

| Company Tool | Typical URL Pattern |
|--------------|-------------------|
| **Nexus 3** | `https://nexus.company.com/repository/maven-public/` |
| **Nexus 2** | `https://nexus.company.com/nexus/content/groups/public/` |
| **Artifactory** | `https://artifactory.company.com/artifactory/maven-virtual/` |
| **Azure Artifacts** | `https://pkgs.dev.azure.com/{org}/_packaging/{feed}/maven/v1` |
| **GitHub Packages** | `https://maven.pkg.github.com/{owner}/{repo}` |

## Troubleshooting

### Common Issues:

1. **401 Unauthorized**: Check credentials in `~/.m2/settings.xml`
2. **Connection Refused**: Verify proxy settings
3. **SSL Certificate Issues**: Add `-Dmaven.wagon.http.ssl.insecure=true` (not recommended for production)
4. **Slow Downloads**: Check if corporate firewall is scanning artifacts

### Debug Commands:
```bash
# Show repository information
./mvnw dependency:list-repositories

# Debug dependency resolution
./mvnw dependency:resolve -X -U

# Clear local repository cache
rm -rf ~/.m2/repository
```

## Best Practices

1. **Use Global Settings**: Configure `~/.m2/settings.xml` for corporate repos
2. **Environment Variables**: Use env vars for credentials
3. **Mirror Configuration**: Use mirrors to redirect all traffic to corporate repos
4. **Separate Profiles**: Create different profiles for different environments
5. **Document URLs**: Keep a company wiki with repository URLs and credentials

## For Your Current Project

To use corporate repositories with your Quarkus project:

1. **Option 1 (Recommended)**: Configure `~/.m2/settings.xml` with your company's repository
2. **Option 2**: Uncomment the repository section in your `pom.xml` and update URLs
3. **Option 3**: Use environment variables in Docker builds

The configuration is already prepared in your project - just uncomment and update the URLs in `pom.xml` when needed!
