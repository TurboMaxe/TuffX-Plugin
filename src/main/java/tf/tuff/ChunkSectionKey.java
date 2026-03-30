package tf.tuff;

import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

@Getter
public final class ChunkSectionKey {
    private final UUID p;
    private final String w;
    private final int x;
    private final int z;
    private final int y;

    public ChunkSectionKey(UUID p, String w, int x, int z, int y) {
        this.p = p;
        this.w = w;
        this.x = x;
        this.z = z;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || o.getClass() != this.getClass()) return false;
        var t = (ChunkSectionKey) o;
        return Objects.equals(p, t.p) &&
                Objects.equals(w, t.w) &&
                x == t.x &&
                z == t.z &&
                y == t.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(p, w, x, z, y);
    }

    @Override
    public String toString() {
        return "ChunkSectionKey[" +
                "p=" + p + ", " +
                "w=" + w + ", " +
                "x=" + x + ", " +
                "z=" + z + ", " +
                "y=" + y + ']';
    }
}
