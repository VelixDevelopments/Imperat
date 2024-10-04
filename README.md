[![Discord](https://discord.com/api/guilds/1285395980610568192/widget.png)](https://discord.velix.dev/)
[![Maven Central](https://img.shields.io/maven-metadata/v/https/repo1.maven.org/maven2/dev/velix/imperat-core/maven-metadata.xml.svg?label=maven%20central&colorB=brightgreen)](https://search.maven.org/artifact/dev.velix/imperat-core)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Build](https://github.com/VelixDevelopments/Imperat/actions/workflows/build.yml/badge.svg)](https://github.com/VelixDevelopments/Imperat/actions/workflows/build.yml)

# Imperat

**Imperat** is a high-performance, general-purpose command dispatching framework built in Java.
Designed to be platform-agnostic, Imperat is capable of handling massive numbers of commands efficiently.
Whether you're building microservices, game engines, or any other system that requires command dispatching,
Imperat provides a flexible and powerful foundation, that can handle complex command graphs incluing middle optional
arguments,
with a smart algorithm called `SmartUsageResolve`.

## Features

- **Generic Command Dispatching:** Dispatch commands across multiple platforms and systems seamlessly.
- **High Performance:** Engineered to manage large volumes of commands with minimal overhead.
- **Platform-Agnostic:** Works across different platforms, making it suitable for a wide variety of projects.

## Installation

Imperat is available on Maven Central.<br>
You can install it using either Maven or Gradle.<br>
Replace `PLATFORM` with your desired platform and `VERSION` with the latest version available.

### Using Gradle

```gradle
dependencies {
    implementation "dev.velix:imperat-core:VERSION"
    implementation "dev.velix:imperat-PLATFORM:VERSION"
}
```

### Using Maven

```xml

<dependencies>
  <dependency>
    <groupId>dev.velix</groupId>
    <artifactId>imperat-core</artifactId>
    <version>VERSION</version>
  </dependency>

  <dependency>
    <groupId>dev.velix</groupId>
    <artifactId>imperat-PLATFORM</artifactId>
    <version>VERSION</version>
  </dependency>
</dependencies>

```

## Supported platforms

Imperat supports the following platforms:

- Bukkit
- Minestom
- Velocity
- Bungeecord
- CLI (Command Line Interface)

## Documentation

For detailed usage instructions, architecture overview, and API documentation,<br>
visit the official [Imperat Documentation](https://docs.velix.dev/Imperat/).

## Join the Community

If you have any questions, ideas,
or want to connect with other developers using Imperat, join our community on [Discord](https://discord.velix.dev/).

## License

Imperat is released under the MIT License. See `LICENSE` for more information.

## Credits

- Mqzn and iiAhmedYT (the original authors of Imperat) <br>
  Some features/ideas were inspired from [Lamp](https://github.com/Revxrsal/Lamp).
