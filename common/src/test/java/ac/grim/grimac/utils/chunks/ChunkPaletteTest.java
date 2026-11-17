package ac.grim.grimac.utils.chunks;

import com.github.retrooper.packetevents.protocol.world.chunk.palette.DataPalette;
import com.github.retrooper.packetevents.protocol.world.chunk.palette.GlobalPalette;
import com.github.retrooper.packetevents.protocol.world.chunk.palette.MapPalette;
import com.github.retrooper.packetevents.protocol.world.chunk.palette.PaletteType;
import com.github.retrooper.packetevents.protocol.world.chunk.storage.BitStorage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ChunkPaletteTest {
    private static final int HIGH_STATE = 35498;

    @Test
    void preservesHighStateWhenFullLocalPaletteGrows() {
        MapPalette local = new MapPalette(8);
        DataPalette data = new DataPalette(local, new BitStorage(8, 4096), PaletteType.CHUNK);
        data.set(0, 0, 0, 0);
        data.set(1, 0, 0, HIGH_STATE);
        for (int state = 2; state < 256; state++) {
            data.set(state & 15, 0, state >> 4, state);
        }

        ChunkPalette.ensureGlobalCapacity(data, 128);
        assertSame(local, data.palette);

        ChunkPalette.ensureGlobalCapacity(data, 256);
        assertEquals(2, data.getAndSet(2, 0, 0, 256));
        assertEquals(16, data.storage.getBitsPerEntry());
        assertEquals(0, data.get(0, 1, 0));
        assertEquals(HIGH_STATE, data.get(1, 0, 0));
        assertEquals(256, data.get(2, 0, 0));
        assertEquals(255, data.get(15, 0, 15));
    }

    @Test
    void acceptsHighStateInExistingGlobalPalette() {
        DataPalette data = new DataPalette(GlobalPalette.INSTANCE, new BitStorage(15, 4096), PaletteType.CHUNK);
        data.set(0, 0, 0, 32767);
        data.set(1, 0, 0, 42);

        ChunkPalette.ensureGlobalCapacity(data, HIGH_STATE);
        assertEquals(42, data.getAndSet(1, 0, 0, HIGH_STATE));
        assertEquals(16, data.storage.getBitsPerEntry());
        assertEquals(32767, data.get(0, 0, 0));
        assertEquals(HIGH_STATE, data.get(1, 0, 0));
    }
}
