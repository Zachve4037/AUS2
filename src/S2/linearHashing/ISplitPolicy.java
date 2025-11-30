package S2.linearHashing;

public interface ISplitPolicy {
    boolean split(long recordsCound, int bucketCount, int blockSize);
    boolean merge(long recordsCound, int bucketCount, int blockSize);
}
