# MCServerPing

A lightweight Java library for retrieving Minecraft server information via the Server List Ping protocol. This library provides access to the same data displayed in the Minecraft client's server list.

## Features

- Server MOTD (Message of the Day) with color code support
- Player count and player samples
- Server version and protocol information
- Base64 encoded favicon
- Built-in SRV record resolution
- Automatic Text Component to legacy text conversion
- Configurable timeouts and connection parameters

## Installation

Add the dependency to your `pom.xml`:

```xml
<dependency>
  <groupId>br.com.azalim</groupId>
  <artifactId>mcserverping</artifactId>
  <version>1.0.9</version>
</dependency>
```

## Usage

### Basic Example

```java
import br.com.azalim.mcserverping.MCPing;
import br.com.azalim.mcserverping.MCPingOptions;
import br.com.azalim.mcserverping.MCPingResponse;

// Simple ping with default settings
MCPingResponse response = MCPing.getPing("mc.hypixel.net");

// With custom options
MCPingOptions options = MCPingOptions.builder()
    .hostname("example.com")
    .port(25565)
    .timeout(3000)
    .build();

MCPingResponse response = MCPing.getPing(options);

// Access server information
System.out.println("MOTD: " + response.getDescription().getStrippedText());
System.out.println("Players: " + response.getPlayers().getOnline() + "/" + response.getPlayers().getMax());
System.out.println("Version: " + response.getVersion().getName());
System.out.println("Ping: " + response.getPing() + "ms");
```

See the [example class](src/main/java/br/com/azalim/mcserverping/examples/MCPingExample.java) for a complete implementation.

### Configuration Options

The `MCPingOptions` class supports the following configuration:

- `hostname` (required) - Server hostname or IP address
- `port` (optional, default: 25565) - Server port
- `timeout` (optional, default: 5000) - Connection timeout in milliseconds
- `readTimeout` (optional, default: 5000) - Read timeout in milliseconds
- `charset` (optional, default: UTF-8) - Character encoding for MOTD
- `protocolVersion` (optional, default: 4) - Minecraft protocol version

### Available Data

The `MCPingResponse` object provides access to:

- **Description**: Server MOTD with `getText()` and `getStrippedText()` methods
- **Players**: Online/max player counts and player samples (if provided)
- **Version**: Protocol version and version name
- **Favicon**: Base64 encoded server icon
- **Ping**: Response time in milliseconds
- **Hostname/Port**: Resolved server address (useful with SRV records)

## Requirements

- Java 8 or higher
- Maven for building

## Credits

This project is based on the original work of:

- **jamietech**: [MinecraftServerPing](https://github.com/jamietech/MinecraftServerPing)
- **zh32**: [Server List Ping implementation](https://gist.github.com/zh32/7190955)
