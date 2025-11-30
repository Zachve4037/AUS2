package S2.linearHashing;

import S2.heapFile.Block;
import S2.heapFile.IData;
import S2.heapFile.IRecord;

public class LHBlock<T extends IRecord> extends Block<T> {
    private int nextBlock;

    public LHBlock(int capacity, IData<T> type) {
        super(capacity, type);
    }

    public int getNextBlock() {
        return nextBlock;
    }

    public void setNextBlock(int nextBlock) {
        this.nextBlock = nextBlock;
    }
}
