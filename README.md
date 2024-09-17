
# Imperat

**Imperat** is a high-performance, general-purpose command dispatching framework built in Java.
Designed to be platform-agnostic, Imperat is capable of handling massive numbers of commands efficiently.
Whether you're building microservices, game engines, or any other system that requires command dispatching,
Imperat provides a flexible and powerful foundation.

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
    implementation "dev.velix.imperat:Imperat-PLATFORM:VERSION"
}
```

### Using Maven

```xml

<dependency>
    <groupId>dev.velix.imperat</groupId>
    <artifactId>Imperat-PLATFORM</artifactId>
    <version>VERSION</version>
</dependency>
```

## Supported platforms

Imperat supports the following platforms:

- Bukkit/Spigot/Paper
- Bungeecord

## Documentation

For detailed usage instructions, architecture overview, and API documentation,
visit the official [Imperat Documentation](https://docs.velix.dev/Imperat/).

## Join the Community

If you have any questions, ideas,
or want to connect with other developers using Imperat, join our community on [Discord](https://discord.gg/MX9MkaDT5W).

## License

Imperat is released under the MIT License. See `LICENSE` for more information.

## Credits

- Mqzn and iiAhmedYT (the original authors for Imperat) <br>
  Some features/ideas were inspired from [Lamp](https://github.com/Revxrsal/Lamp).