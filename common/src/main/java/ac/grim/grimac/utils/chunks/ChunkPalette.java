package ac.grim.grimac.utils.chunks;

import com.github.retrooper.packetevents.protocol.world.chunk.palette.DataPalette;
import com.github.retrooper.packetevents.protocol.world.chunk.palette.GlobalPalette;
import com.github.retrooper.packetevents.protocol.world.chunk.palette.Palette;
import com.github.retrooper.packetevents.protocol.world.chunk.storage.BaseStorage;
import com.github.retrooper.packetevents.protocol.world.chunk.storage.BitStorage;

public final class ChunkPalette {
    private static final int GLOBAL_BITS_26_3 = 16;

    private ChunkPalette() {
    }

    // Remove after the PacketEvents fork ships upstream commit 36cee94.
    public static void ensureGlobalCapacity(DataPalette data, int state) {
        BaseStorage storage = data.storage;
        if (storage == null) return;

        Palette palette = data.palette;
        int bits = storage.getBitsPerEntry();
        if (palette instanceof GlobalPalette) {
            if (bits >= GLOBAL_BITS_26_3 || state < (1 << bits)) return;
        } else {
            if (bits != data.paletteType.getMaxBitsPerEntryForMap() || palette.size() < (1 << bits)
                    || GlobalPalette.INSTANCE.getBits() >= GLOBAL_BITS_26_3 || palette.stateToId(state) != -1) return;
        }

        int size = data.paletteType.getStorageSize();
        BitStorage widened = new BitStorage(GLOBAL_BITS_26_3, size);
        for (int i = 0; i < size; i++) {
            widened.set(i, palette.idToState(storage.get(i)));
        }
        data.palette = GlobalPalette.INSTANCE;
        data.storage = widened;
    }
}
