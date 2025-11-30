package S2.linearHashing;

public class SplitPolicyImpl implements ISplitPolicy {
    private final double maxDensity;
    private final double minDensity;

    public SplitPolicyImpl(double maxDensity, double minDensity) {
        this.maxDensity = maxDensity;
        this.minDensity = minDensity;
    }

    @Override
    public boolean split(long recordsCount, int bucketCount, int blockSize) {
        double d = (double) recordsCount / (bucketCount * blockSize);
        return d > maxDensity;
    }

    @Override
    public boolean merge(long recordsCount, int bucketCount, int blockSize) {
        double d = (double) recordsCount / (bucketCount * blockSize);
        return d < minDensity;
    }
}
