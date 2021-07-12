MCServerPing
===================

MCServerPing is a quick and easy Java API for grabbing Minecraft server information. It allows you to access information sent to the client to display on the server list. Currently, information available is:

 * String and Text Component based MOTD 
 * Favicon
 * Server and protocol versions
 * Player counts and samples
 
Some additional features:

 * Provides a simple built-in SRV resolver
 * Allows you to retrieve Text Component based MOTD
 * Much more

Please leave the copyright notice intact in source if you copy source into your code rather than build path.

If you require compatibility with the previous packets, use the [alpha tag](https://github.com/jamietech/MinecraftServerPing/releases/tag/alpha).

The latest version of MCServerPing is based heavily upon the work of [zh32](https://gist.github.com/zh32/7190955).

## Configuration
Configuration is provided through the `MinecraftPingOptions` class. This class provides the following configuration options:

 * `String` hostname *(hostname of the server to query, **required**)*
 * `int` port *(port of the server to query, **optional** default `25565`)*
 * `int` timeout *(socket timeout in ms, **optional** default `2000`)*
 * `String` charset *(charset for MOTD byte->string, **optional** default `UTF-8`)*

## Example

MinecraftServerPing is very easy to use and implement into any project. Simply clone the repo, compile the code and add it to your build path. Coding with it is as simple as this too:

```java

MCPingOptions options = MCPingOptions.builder()
  .hostname("example.com")
  .port(25565)
  .build();

MCPingResponse data = MCPing.getPing(options);      
System.out.println(data.getDescription().getStrippedText() + "  --  " + data.getPlayers().getOnline() + "/" + data.getPlayers().getMax());

```

An example class is available [here](https://github.com/lucaazalim/MCServerPing/blob/master/src/main/java/br/com/azalim/mcserverping/examples/MCPingExample.java).
