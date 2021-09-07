package br.com.azalim.mcserverping.examples;

import br.com.azalim.mcserverping.MCPing;
import br.com.azalim.mcserverping.MCPingOptions;
import br.com.azalim.mcserverping.MCPingResponse;
import br.com.azalim.mcserverping.MCPingResponse.Description;
import br.com.azalim.mcserverping.MCPingResponse.Player;
import br.com.azalim.mcserverping.MCPingResponse.Players;
import br.com.azalim.mcserverping.MCPingResponse.Version;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class MCPingExample {

    public static void main(String[] args) {

        MCPingOptions options = MCPingOptions.builder()
                .hostname("redesky.com")
                .build();

        MCPingResponse reply;

        try {
            reply = MCPing.getPing(options);
        } catch (IOException ex) {
            System.out.println(options.getHostname() + " is down or unreachable.");
            return;
        }

        System.out.println(String.format("Full response from %s:", options.getHostname()));
        System.out.println();

        Description description = reply.getDescription();

        System.out.println("Description:");
        System.out.println("    Raw: " + description.getText());
        System.out.println("    No color codes: " + description.getStrippedText());
        System.out.println();

        Players players = reply.getPlayers();

        System.out.println("Players: ");
        System.out.println("    Online count: " + players.getOnline());
        System.out.println("    Max players: " + players.getMax());
        System.out.println();

        // Can be null depending on the server
        List<Player> sample = players.getSample();

        if (sample != null) {
            System.out.println("    Players: " + players.getSample().stream()
                    .map(player -> String.format("%s@%s", player.getName(), player.getId()))
                    .collect(Collectors.joining(", "))
            );
            System.out.println();
        }

        Version version = reply.getVersion();

        System.out.println("Version: ");

        // The protocol is the version number: http://wiki.vg/Protocol_version_numbers
        System.out.println("    Protocol: " + version.getProtocol());
        System.out.println("    Name: " + version.getName());
        System.out.println();

        System.out.println(String.format("Favicon: %s", reply.getFavicon()));

    }

}
