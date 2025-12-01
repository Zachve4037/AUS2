package S2.linearHashing;

import S2.heapFile.Block;
import S2.heapFile.IData;
import S2.heapFile.IRecord;

import java.nio.ByteBuffer;

public class MainBlock<T extends IRecord & IHash> extends Block<T> {
    private int recordCount;
    private int chainLength;

    public MainBlock(int capacity, IData<T> type) {
        super(capacity, type);
        this.recordCount = 0;
        this.chainLength = 0;
    }

    public int getRecordCount() {
        return this.recordCount;
    }

    public int getChainLength() {
        return this.chainLength;
    }

    public void setRecordCount(int count) {
        this.recordCount = count;
    }

    public void setChainLength(int length) {
        this.chainLength = length;
    }

    @Override
    public int getSize() {
        return 12 + (this.getData()[0].getSize() * getData().length);
    }

    @Override
    public byte[] getBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(this.getSize());
        buffer.putInt(this.recordCount);
        buffer.putInt(this.chainLength);
        buffer.putInt(this.getValidCount());

        for (int i = 0; i < this.getValidCount(); i++) {
            buffer.put(this.getData()[i].getBytes());
        }

        int recordSize = this.getData()[0].getSize();
        for (int i = this.getValidCount(); i < this.getData().length; i++) {
            buffer.put(new byte[recordSize]);
        }
        return buffer.array();
    }

    @Override
    public void fromBytes(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        this.recordCount = buffer.getInt();
        this.chainLength = buffer.getInt();
        this.setValidCount(buffer.getInt());

        for (int i =0; i < this.getData().length; i++) {
            byte[] recordBytes = new byte[this.getData()[i].getSize()];
            buffer.get(recordBytes);
            getData()[i].fromBytes(recordBytes);
        }
    }
}
