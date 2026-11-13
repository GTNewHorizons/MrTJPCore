package mrtjp.core.block;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.WorldServer;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.junit.Test;

import com.mojang.authlib.GameProfile;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.packet.PacketCustom;
import io.netty.buffer.Unpooled;
import sun.misc.Unsafe;

public class ClientDescriptionWarningTest {

    @Test
    public void limitsWarningsAcrossTilesAndReconnectsWithoutDispatchingDescriptions() throws Exception {
        TestTile first = new TestTile();
        TestTile second = new TestTile();
        TestPlayer sender = player(UUID.randomUUID());
        WarningAppender warnings = new WarningAppender();
        Logger logger = (Logger) LogManager.getLogger("MrTJPCoreMod");
        Level previousLevel = logger.getLevel();
        warnings.start();
        logger.addAppender(warnings);
        logger.setLevel(Level.WARN);
        try {
            for (int i = 0; i < 1000; i++) {
                send(i % 2 == 0 ? first : second, sender, 0);
            }
            assertEquals(1, warnings.messages.size());
            assertEquals(1, first.diagnosticReads + second.diagnosticReads);
            assertTrue(warnings.messages.get(0).contains("malformed payload"));

            // A new player entity with the same UUID must share the cooldown.
            send(second, player(sender.getUniqueID()), 0);
            assertEquals(1, warnings.messages.size());
            assertEquals(1, first.diagnosticReads + second.diagnosticReads);

            send(second, player(UUID.randomUUID()), 0);
            assertEquals(2, warnings.messages.size());
            assertEquals(2, first.diagnosticReads + second.diagnosticReads);
            assertEquals(0, first.descriptionReads + second.descriptionReads);
            assertEquals(0, first.clientActions + second.clientActions);

            send(first, sender, 7);
            assertEquals(1, first.clientActions);
            assertEquals(7, first.lastClientKey);
            assertEquals(0, first.updateReads + second.updateReads);
            assertEquals(2, warnings.messages.size());
        } finally {
            logger.removeAppender(warnings);
            logger.setLevel(previousLevel);
            warnings.stop();
        }
    }

    private static void send(TestTile tile, EntityPlayer player, int key) {
        PacketCustom packet = new PacketCustom(Unpooled.buffer().writeByte(1).writeByte(key));
        try {
            tile.handleClientPacket(packet, player);
        } finally {
            packet.getByteBuf().release();
        }
    }

    private static TestPlayer player(UUID id) throws Exception {
        TestPlayer player = allocate(TestPlayer.class);
        player.profile = new GameProfile(id, "PacketTest");
        return player;
    }

    // These checks need identity and dimension data, without starting a server.
    private static <T> T allocate(Class<T> type) throws Exception {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return type.cast(((Unsafe) field.get(null)).allocateInstance(type));
    }

    private static class TestPlayer extends EntityPlayerMP {

        private GameProfile profile;

        private TestPlayer() {
            super(null, null, null, null);
        }

        @Override
        public UUID getUniqueID() {
            return profile.getId();
        }

        @Override
        public GameProfile getGameProfile() {
            return profile;
        }
    }

    private static class TestTile extends InstancedBlockTile {

        private int diagnosticReads;
        private int descriptionReads;
        private int updateReads;
        private int clientActions;
        private int lastClientKey;

        private TestTile() throws Exception {
            worldObj = allocate(WorldServer.class);
            Field provider = World.class.getDeclaredField("provider");
            provider.setAccessible(true);
            provider.set(worldObj, new WorldProviderSurface());
        }

        @Override
        public Block getBlock() {
            return null;
        }

        @Override
        public String clientDescriptionPacketDetails(PacketCustom packet) {
            diagnosticReads++;
            // No payload follows the key: exercise the malformed diagnostic path.
            packet.readInt();
            throw new AssertionError("Expected an empty description");
        }

        @Override
        public void readDesc(MCDataInput packet) {
            descriptionReads++;
        }

        @Override
        public void read(MCDataInput packet, int key) {
            updateReads++;
        }

        @Override
        public void readFromClient(MCDataInput packet, int key, EntityPlayer player) {
            clientActions++;
            lastClientKey = key;
        }
    }

    private static class WarningAppender extends AbstractAppender {

        private final List<String> messages = new ArrayList<>();

        private WarningAppender() {
            super("ClientDescriptionWarningTest", null, null);
        }

        @Override
        public void append(LogEvent event) {
            if (event.getLevel() == Level.WARN && event.getMarker() != null
                    && event.getMarker().isInstanceOf("SuspiciousPackets")) {
                messages.add(event.getMessage().getFormattedMessage());
            }
        }
    }
}
