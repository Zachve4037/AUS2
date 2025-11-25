package S2.DB;

import S2.BaseClasses.PersonBinary;
import S2.HeapFile.Block;
import S2.HeapFile.IData;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class HeapDumpDialog extends JDialog {
    private final File dataFile;
    private final File metaFile;
    private final JTextArea outputArea;

    private static final int RECORD_SIZE = 176;

    public HeapDumpDialog(Frame owner, File dataFile, File metaFile) {
        super(owner, "Heap Dump: " + (dataFile != null ? dataFile.getName() : "unknown"), true);
        this.dataFile = dataFile;
        this.metaFile = metaFile;

        setSize(1000, 800);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        add(new JScrollPane(outputArea), BorderLayout.CENTER);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshBtn = new JButton("Refresh");
        JButton closeBtn = new JButton("Close");
        top.add(new JLabel("Data: " + (dataFile != null ? dataFile.getName() : "<none>") + "  Meta: " + (metaFile != null ? metaFile.getName() : "<none>")));
        top.add(refreshBtn);
        top.add(closeBtn);
        add(top, BorderLayout.NORTH);

        refreshBtn.addActionListener(a -> refresh());
        closeBtn.addActionListener(a -> setVisible(false));

        refresh();
    }

    private void refresh() {
        try {
            String dump = buildDump();
            outputArea.setText(dump);
            outputArea.setCaretPosition(0);
        } catch (Exception ex) {
            outputArea.setText("Failed to produce dump: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private String buildDump() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Heap Dump ===\n\n");
        sb.append("Data file: `").append(dataFile.getAbsolutePath()).append("`\n");
        sb.append("Size: ").append(dataFile.exists() ? dataFile.length() : 0).append(" bytes\n");
        sb.append("Meta file: `").append(metaFile.getAbsolutePath()).append("`\n");
        sb.append("Size: ").append(metaFile.exists() ? metaFile.length() : 0).append(" bytes\n\n");

        int metaTotalBlocks = -1;
        List<Integer> partials = new ArrayList<>();
        List<Integer> empties = new ArrayList<>();
        byte[] metaRaw = null;
        boolean metaTruncated = false;

        if (metaFile.exists()) {
            try (RandomAccessFile metaRaf = new RandomAccessFile(metaFile, "r");
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                byte[] buf = new byte[4096];
                int r;
                while ((r = metaRaf.read(buf)) != -1) {
                    baos.write(buf, 0, r);
                }
                metaRaw = baos.toByteArray();

                try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(metaRaw))) {
                    metaTotalBlocks = dis.readInt();
                    int partialCount = dis.readInt();
                    for (int i = 0; i < partialCount; i++) partials.add(dis.readInt());
                    int emptyCount = dis.readInt();
                    for (int i = 0; i < emptyCount; i++) empties.add(dis.readInt());
                } catch (EOFException eof) {
                    metaTruncated = true;
                }
            } catch (IOException ex) {
                metaTruncated = true;
            }
        } else {
            sb.append("(no metadata file present)\n\n");
        }

        sb.append("Metadata.totalBlocks = ").append(metaTotalBlocks >= 0 ? metaTotalBlocks : "<unknown>");
        if (metaTruncated) sb.append("  (truncated)");
        sb.append("\n");

        int blockSize = 512;
        if (dataFile.exists() && metaTotalBlocks > 0) {
            long candidate = dataFile.length() / metaTotalBlocks;
            int minAllowed = 4 + RECORD_SIZE;
            if (candidate >= minAllowed && candidate <= Integer.MAX_VALUE) {
                blockSize = (int) candidate;
            }
        }

        int blockingFactor = Math.max(1, (blockSize - 4) / RECORD_SIZE);

        long fileBlocks = dataFile.exists() ? (dataFile.length() / blockSize) : 0;
        Set<Integer> partialSet = new TreeSet<>();
        Set<Integer> emptySet = new TreeSet<>();
        int maxAllowed = (int) Math.max(-1, fileBlocks - 1);

        for (int p : partials) if (p >= 0 && p <= maxAllowed) partialSet.add(p);
        for (int e : empties)  if (e >= 0 && e <= maxAllowed) emptySet.add(e);

        sb.append("Detected blockSize = ").append(blockSize).append(", blockingFactor = ").append(blockingFactor).append("\n");
        sb.append("Partially free blocks: ").append(partialSet).append("\n");
        sb.append("Fully empty blocks: ").append(emptySet).append("\n\n");

        if (metaRaw != null) {
            sb.append("Metadata raw (binary, first 64 bytes): ").append(bytesToBinary(metaRaw, 0, Math.min(64, metaRaw.length))).append("\n\n");
        }

        if (!dataFile.exists()) {
            sb.append("(no data file present)\n");
            return sb.toString();
        }

        try (RandomAccessFile raf = new RandomAccessFile(dataFile, "r")) {
            fileBlocks = raf.length() / blockSize;
            sb.append("Data blocks on disk: ").append(fileBlocks).append("\n\n");

            if (metaTotalBlocks > fileBlocks) {
                sb.append(String.format("Warning: metadata claims %d blocks but data file contains %d blocks. Showing only on-disk blocks.\n\n", metaTotalBlocks, fileBlocks));
            }

            int maxIndex = (int) (fileBlocks - 1);
            if (maxIndex < 0) {
                sb.append("(no blocks present on disk)\n");
                return sb.toString();
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            for (int b = 0; b <= maxIndex; b++) {
                long offset = (long) b * blockSize;
                sb.append("Block ").append(b).append(" @ offset ").append(offset);

                if (offset + 4 > raf.length()) {
                    sb.append("  (not present on disk)\n\n");
                    continue;
                }

                raf.seek(offset);
                byte[] blockBytes = new byte[blockSize];
                int actuallyRead = raf.read(blockBytes);
                if (actuallyRead <= 0) {
                    sb.append("  (unable to read)\n\n");
                    continue;
                }
                if (actuallyRead < blockSize) {
                    Arrays.fill(blockBytes, actuallyRead, blockSize, (byte) 0);
                }

                Block<PersonBinary> block = new Block<>(blockingFactor, (IData<PersonBinary>) () -> new PersonBinary());
                try {
                    block.fromBytes(blockBytes);
                } catch (Exception ex) {
                    sb.append("  (failed to parse block: ").append(ex.getMessage()).append(")\n\n");
                    continue;
                }

                int validCount = block.getValidCount();
                boolean corrupted = false;
                if (validCount < 0 || validCount > blockingFactor) {
                    corrupted = true;
                    validCount = Math.max(0, Math.min(blockingFactor, validCount));
                }

                sb.append(" -> validCount=").append(validCount)
                        .append(" / capacity=").append(blockingFactor)
                        .append("  [meta-partial=").append(partialSet.contains(b))
                        .append(", meta-empty=").append(emptySet.contains(b))
                        .append("]");
                if (corrupted) sb.append("  (CORRUPTED: clamped)");
                sb.append("\n");

                sb.append("  Header raw (4 bytes): ").append(bytesToBinary(blockBytes, 0, 4)).append("\n");

                PersonBinary[] recs = block.getData();
                for (int r = 0; r < blockingFactor; r++) {
                    PersonBinary p = recs[r];
                    String status = r < validCount ? "VALID" : "EMPTY";
                    String id = "<empty-id>";
                    String summary = "<no-summary>";
                    try {
                        if (r < validCount && p != null) {
                            int recOffset = 4 + r * RECORD_SIZE;
                            int recLen = Math.max(0, Math.min(RECORD_SIZE, blockBytes.length - recOffset));
                            byte[] recBytes = new byte[recLen];
                            if (recLen > 0) System.arraycopy(blockBytes, recOffset, recBytes, 0, recLen);

                            PersonBinary tmp = new PersonBinary();
                            tmp.fromBytes(recBytes);
                            if (tmp.getId() != null && !tmp.getId().isEmpty()) id = tmp.getId();
                            summary = tmp.toString() != null ? tmp.toString() : "<present>";
                        }
                    } catch (Exception ex) {
                    }

                    sb.append(String.format("  [%d] %s - id=%s\n", r, status, id));
                    sb.append("      summary: ").append(summary).append("\n");

                    int recOffset = 4 + r * RECORD_SIZE;
                    int recLen = Math.max(0, Math.min(RECORD_SIZE, blockBytes.length - recOffset));
                    if (recLen > 0) {
                        sb.append("      raw: ").append(bytesToBinary(blockBytes, recOffset, Math.min(32, recLen))).append("\n");
                    } else {
                        sb.append("      raw: <empty>\n");
                    }
                }

                sb.append("\n");
            }
        }

        return sb.toString();
    }

    private static String bytesToBinary(byte[] bytes, int off, int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = off; i < off + len && i < bytes.length; i++) {
            sb.append(String.format("%8s", Integer.toBinaryString(bytes[i] & 0xFF)).replace(' ', '0'));
            if (i < off + len - 1) sb.append(" ");
        }
        return sb.toString();
    }
}
