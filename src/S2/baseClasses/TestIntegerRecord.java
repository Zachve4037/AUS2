package S2.baseClasses;

import S2.heapFile.IRecord;
import S2.linearHashing.IHash;

import java.nio.ByteBuffer;

public class TestIntegerRecord implements IRecord, IHash {
    private int value;

    public TestIntegerRecord() {
        this.value = 0;
    }

    public TestIntegerRecord(int value) {
        this.value = value;
    }

    @Override
    public boolean equals(IRecord other) {
        if (!(other instanceof TestIntegerRecord)) return false;
        return this.value == ((TestIntegerRecord) other).value;
    }

    @Override
    public int getSize() {
        return 4;
    }

    @Override
    public byte[] getBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(value);
        return buffer.array();
    }

    @Override
    public void fromBytes(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        this.value = buffer.getInt();
    }

    @Override
    public long getHashCode() {
        return Math.abs(value);
    }

    @Override
    public String toString() {
        return "IntegerRecord{" + value + "}";
    }
}
