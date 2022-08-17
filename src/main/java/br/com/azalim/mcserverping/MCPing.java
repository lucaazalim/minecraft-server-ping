/*
 * Copyright 2014 jamietech. All rights reserved.
 * https://github.com/jamietech/MinecraftServerPing
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and contributors and should not be interpreted as representing official policies,
 * either expressed or implied, of anybody else.
 */
package br.com.azalim.mcserverping;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.xbill.DNS.*;
import org.xbill.DNS.Record;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class MCPing {

    private static final Gson GSON = new Gson();
    private static final String SRV_QUERY_PREFIX = "_minecraft._tcp.%s";

    /**
     * Fetches a {@link MCPingResponse} for the supplied hostname.
     * <b>Assumed timeout of 2s and port of 25565.</b>
     *
     * @param address - a valid String hostname
     * @return {@link MCPingResponse}
     * @throws IOException
     */
    public static MCPingResponse getPing(final String address) throws IOException {
        return getPing(MCPingOptions.builder().hostname(address).build());
    }

    /**
     * Fetches a {@link MCPingResponse} for the supplied options.
     *
     * @param options - a filled instance of {@link MCPingOptions}
     * @return {@link MCPingResponse}
     * @throws IOException
     */
    public static MCPingResponse getPing(final MCPingOptions options) throws IOException {

        Preconditions.checkNotNull(options.getHostname(), "Hostname cannot be null.");

        String hostname = options.getHostname();
        int port = options.getPort();

        try {

            Record[] records = new Lookup(String.format(SRV_QUERY_PREFIX, hostname), Type.SRV).run();

            if (records != null) {

                for (Record record : records) {
                    SRVRecord srv = (SRVRecord) record;

                    hostname = srv.getTarget().toString().replaceFirst("\\.$", "");
                    port = srv.getPort();
                }

            }
        } catch (TextParseException e) {
            e.printStackTrace();
        }

        String json;
        long ping = -1;

        try (final Socket socket = new Socket()) {

            long start = System.currentTimeMillis();
            socket.connect(new InetSocketAddress(hostname, port), options.getTimeout());
            ping = System.currentTimeMillis() - start;

            try (DataInputStream in = new DataInputStream(socket.getInputStream());
                 DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                 //> Handshake
                 ByteArrayOutputStream handshake_bytes = new ByteArrayOutputStream();
                 DataOutputStream handshake = new DataOutputStream(handshake_bytes)) {

                handshake.writeByte(MCPingUtil.PACKET_HANDSHAKE);
                MCPingUtil.writeVarInt(handshake, options.getProtocolVersion());
                MCPingUtil.writeVarInt(handshake, options.getHostname().length());
                handshake.writeBytes(options.getHostname());
                handshake.writeShort(options.getPort());
                MCPingUtil.writeVarInt(handshake, MCPingUtil.STATUS_HANDSHAKE);

                MCPingUtil.writeVarInt(out, handshake_bytes.size());
                out.write(handshake_bytes.toByteArray());

                //> Status request
                out.writeByte(0x01); // Size of packet
                out.writeByte(MCPingUtil.PACKET_STATUSREQUEST);

                //< Status response
                MCPingUtil.readVarInt(in); // Size
                int id = MCPingUtil.readVarInt(in);

                MCPingUtil.io(id == -1, "Server prematurely ended stream.");
                MCPingUtil.io(id != MCPingUtil.PACKET_STATUSREQUEST, "Server returned invalid packet.");

                int length = MCPingUtil.readVarInt(in);
                MCPingUtil.io(length == -1, "Server prematurely ended stream.");
                MCPingUtil.io(length == 0, "Server returned unexpected value.");

                byte[] data = new byte[length];
                in.readFully(data);
                json = new String(data, options.getCharset());

                //> Ping
                out.writeByte(0x09); // Size of packet
                out.writeByte(MCPingUtil.PACKET_PING);
                out.writeLong(System.currentTimeMillis());

                //< Ping
                MCPingUtil.readVarInt(in); // Size
                id = MCPingUtil.readVarInt(in);
                MCPingUtil.io(id == -1, "Server prematurely ended stream.");
                MCPingUtil.io(id != MCPingUtil.PACKET_PING, "Server returned invalid packet.");

            }

        }

        JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
        JsonElement descriptionJsonElement = jsonObject.get("description");

        if (descriptionJsonElement.isJsonObject()) {

            // For those versions that work with TextComponent MOTDs

            JsonObject descriptionJsonObject = jsonObject.get("description").getAsJsonObject();

            if (descriptionJsonObject.has("extra")) {
                descriptionJsonObject.addProperty("text", new TextComponent(ComponentSerializer.parse(descriptionJsonObject.get("extra").getAsJsonArray().toString())).toLegacyText());
                jsonObject.add("description", descriptionJsonObject);
            }

        } else {

            // For those versions that work with String MOTDs

            String description = descriptionJsonElement.getAsString();
            JsonObject descriptionJsonObject = new JsonObject();
            descriptionJsonObject.addProperty("text", description);
            jsonObject.add("description", descriptionJsonObject);

        }

        MCPingResponse output = GSON.fromJson(jsonObject, MCPingResponse.class);
        output.setPing(ping);
        output.setHostname(hostname);
        output.setPort(port);
        
        return output;
    }

}
