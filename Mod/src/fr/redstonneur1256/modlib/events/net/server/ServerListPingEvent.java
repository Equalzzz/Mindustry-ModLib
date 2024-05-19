package fr.redstonneur1256.modlib.events.net.server;

import arc.Core;
import mindustry.Vars;
import mindustry.core.Version;
import mindustry.game.Gamemode;
import mindustry.gen.Groups;
import mindustry.net.Administration;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static mindustry.Vars.*;

/**
 * Event called when a user ping the Mindustry server, vanilla field length limits does not apply. it is however recommended
 * to respect them as going over might cause string values to be trimmed.
 */
public class ServerListPingEvent {

    /**
     * The address that pinged the server
     */
    public InetAddress address;
    /**
     * Should the server appear offline ?
     * If true the server will appear as offline and no reply will be sent to the ping
     */
    public boolean offline;

    /**
     * Game build
     */
    public int versionBuild;
    /**
     * Game version (official/custom)
     */
    public String versionType;

    /**
     * Server name
     */
    public String name;
    /**
     * Server description
     */
    public String description;
    /**
     * Current server map
     */
    public String map;
    /**
     * Current game wave
     */
    public int wave;
    /**
     * Server gamemode
     */
    public Gamemode gamemode;
    /**
     * Custom game mode to display instead of gamemode
     */
    public String customGamemode;

    /**
     * Current connected player count
     */
    public int playerCount;
    /**
     * Player limit count or -1 to disable
     */
    public int playerLimit;

    public ServerListPingEvent() {

    }

    /**
     * Applies the default mindustry values to the event
     */
    public void setDefaults() {
        versionBuild = Version.build;
        versionType = Version.type;

        name = Vars.headless ? Administration.Config.serverName.string() : Vars.player.name;
        description = Vars.headless && !Administration.Config.desc.string().equals("off") ? Administration.Config.desc.string() : "";
        map = Vars.state.map.name();
        wave = Vars.state.wave;
        gamemode = Vars.state.rules.mode();
        customGamemode = Vars.state.rules.modeName;

        playerCount = Core.settings.getInt("totalPlayers", Groups.player.size());
        playerLimit = Vars.netServer.admins.getPlayerLimit();
    }

    /**
     * Writes the server data to a new ByteBuffer
     *
     * @return the encoded server data
     */
    public ByteBuffer writeServerData() {
        ByteBuffer buffer = ByteBuffer.allocate(512);

        int reservedSpace = 1 + 1 + 4 + 4 + 4 + 1 + 1 + 4 + 1 + (customGamemode != null ? 1 : 0);

        writeString(buffer, name, buffer.remaining() - reservedSpace);
        writeString(buffer, map, buffer.remaining() - reservedSpace);

        buffer.putInt(playerCount);
        buffer.putInt(wave);

        buffer.putInt(versionBuild);
        writeString(buffer, versionType, buffer.remaining() - reservedSpace);

        buffer.put((byte) gamemode.ordinal());
        buffer.putInt(playerLimit);

        writeString(buffer, description, buffer.remaining() - reservedSpace);
        if (customGamemode != null) {
            writeString(buffer, customGamemode, buffer.remaining() - reservedSpace);
        }

        return buffer;
    }

    private static void writeString(ByteBuffer buffer, String string, int maxLength) {
        byte[] bytes = string.getBytes(charset);
        if (bytes.length > maxLength) {
            bytes = Arrays.copyOf(bytes, Math.max(maxLength, 0));
        }
        buffer.put((byte) bytes.length);
        buffer.put(bytes);
    }

}
