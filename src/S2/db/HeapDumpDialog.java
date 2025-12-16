package S2.db;

import S2.baseClasses.PCRTest;
import S2.baseClasses.PersonBinary;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class HeapDumpDialog extends JDialog {
    private final File dataFile;
    private final File metaFile;
    private final int blockSize;

    public HeapDumpDialog(Frame owner, File dataFile, File metaFile, int blockSize) {
        super(owner, "Heap Dump: " + dataFile.getName(), true);
        this.dataFile = dataFile;
        this.metaFile = metaFile;
        this.blockSize = blockSize;

        setSize(900, 700);
        setLocationRelativeTo(owner);

        JTextArea textArea = new JTextArea();
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        textArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(closeButton);
        add(bottomPanel, BorderLayout.SOUTH);

        String dump = generateDump();
        textArea.setText(dump);
        textArea.setCaretPosition(0);
    }

    private String generateDump() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Heap Dump ===\n");
        sb.append("Data file: `").append(dataFile.getAbsolutePath()).append("`\n");
        sb.append("Size: ").append(dataFile.length()).append(" bytes\n");
        sb.append("Meta file: `").append(metaFile.getAbsolutePath()).append("`\n");
        sb.append("Size: ").append(metaFile.length()).append(" bytes\n\n");

        Metadata meta = loadMetadata();
        if (meta == null) {
            sb.append("Warning: Metadata file is empty or invalid. Deriving from data file.\n\n");
            meta = deriveMetadataFromDataFile();
            if (meta == null) {
                return sb.append("Failed to derive metadata.\n").toString();
            }
        }

        sb.append("Metadata.totalBlocks = ").append(meta.totalBlocks).append("\n");

        RecordTypeInfo typeInfo = detectRecordType(meta);
        if (typeInfo == null) {
            return sb.append("Failed to detect record type.\n").toString();
        }

        sb.append("Detected record type: ").append(typeInfo.typeName).append("\n");
        sb.append("Record size: ").append(typeInfo.recordSize).append(" bytes\n");
        sb.append("Block size: ").append(typeInfo.blockSize).append(" bytes\n");
        sb.append("Blocking factor: ").append(typeInfo.blockingFactor).append("\n");
        sb.append("Partially free blocks: ").append(meta.partiallyFree).append("\n");
        sb.append("Fully empty blocks: ").append(meta.fullyEmpty).append("\n\n");

        sb.append("Metadata raw (binary, first 64 bytes):\n");
        sb.append(metadataToBinary(meta.rawBytes)).append("\n\n");

        sb.append("Data blocks on disk: ").append(meta.totalBlocks).append("\n\n");

        dumpDataBlocks(sb, meta, typeInfo);

        return sb.toString();
    }

    private Metadata deriveMetadataFromDataFile() {
        try (RandomAccessFile raf = new RandomAccessFile(dataFile, "r")) {
            long fileSize = raf.length();
            if (fileSize == 0) return null;

            if (fileSize % this.blockSize != 0) {
                return null;
            }

            Metadata meta = new Metadata();
            meta.totalBlocks = (int) (fileSize / this.blockSize);
            meta.partiallyFree = new ArrayList<>();
            meta.fullyEmpty = new ArrayList<>();
            meta.rawBytes = new byte[0];
            return meta;

        } catch (IOException e) {
            return null;
        }
    }


    private static class RecordTypeInfo {
        String typeName;
        int recordSize;
        int blockSize;
        int blockingFactor;
    }

    private RecordTypeInfo detectRecordType(Metadata meta) {
        try (RandomAccessFile raf = new RandomAccessFile(dataFile, "r")) {
            if (raf.length() == 0 || meta.totalBlocks == 0) {
                return null;
            }

            long fileSize = raf.length();
            int blockSize = (int) (fileSize / meta.totalBlocks);

            RecordTypeInfo info = new RecordTypeInfo();
            info.blockSize = blockSize;

            int headerSize = this.dataFile.getName().contains("_main") || this.dataFile.getName().contains("_overflow") ? 8 : 4;
            boolean isPeople = dataFile.getName().contains("people");

            if (isPeople) {
                int personRecordSize = 204;
                int blockingFactor = (blockSize - headerSize) / personRecordSize;
                info.typeName = "PersonBinary";
                info.recordSize = personRecordSize;
                info.blockingFactor = blockingFactor;
            } else {
                int testRecordSize = 117;
                int blockingFactor = (blockSize - headerSize) / testRecordSize;
                info.typeName = "PCRTest";
                info.recordSize = testRecordSize;
                info.blockingFactor = blockingFactor;
            }

            return info;

        } catch (IOException e) {
            return null;
        }
    }

    private void dumpDataBlocks(StringBuilder sb, Metadata meta, RecordTypeInfo typeInfo) {
        try (RandomAccessFile raf = new RandomAccessFile(dataFile, "r")) {
            long fileSize = raf.length();
            int maxBlocks = (int) (fileSize / typeInfo.blockSize);

            for (int b = 0; b < maxBlocks; b++) {
                long offset = (long) b * typeInfo.blockSize;
                sb.append("Block ").append(b).append(" @ offset ").append(offset).append("\n");

                raf.seek(offset);
                byte[] blockBytes = new byte[typeInfo.blockSize];
                int bytesRead = raf.read(blockBytes);

                if (bytesRead < 8) {
                    sb.append("  (incomplete block)\n\n");
                    continue;
                }

                int validCount = java.nio.ByteBuffer.wrap(blockBytes, 0, 4).getInt();
                int nextBlockAddr = java.nio.ByteBuffer.wrap(blockBytes, 4, 4).getInt();

                sb.append("  validCount = ").append(validCount).append("\n");
                sb.append("  nextBlockAddr = ").append(nextBlockAddr).append("\n");

                if (validCount < 0 || validCount > typeInfo.blockingFactor) {
                    sb.append("  (invalid validCount)\n\n");
                    continue;
                }

                if ("PersonBinary".equals(typeInfo.typeName)) {
                    dumpPersonBinaryRecords(sb, blockBytes, validCount, typeInfo);
                } else if ("PCRTest".equals(typeInfo.typeName)) {
                    dumpPCRTestRecords(sb, blockBytes, validCount, typeInfo);
                }

                sb.append("\n");
            }
        } catch (IOException e) {
            sb.append("Error reading data blocks: ").append(e.getMessage()).append("\n");
        }
    }

    private void dumpPersonBinaryRecords(StringBuilder sb, byte[] blockBytes, int validCount, RecordTypeInfo typeInfo) {
        int headerSize = 8;
        for (int r = 0; r < typeInfo.blockingFactor; r++) {
            int offset = headerSize + (r * typeInfo.recordSize);
            byte[] recordBytes = new byte[typeInfo.recordSize];
            System.arraycopy(blockBytes, offset, recordBytes, 0, typeInfo.recordSize);

            if (r < validCount) {
                PersonBinary person = new PersonBinary();
                try {
                    person.fromBytes(recordBytes);
                    sb.append("    Record ").append(r).append(": ").append(person.toString()).append("\n");
                } catch (Exception e) {
                    sb.append("    Record ").append(r).append(": (parse error)\n");
                }
            } else {
                sb.append("    Record ").append(r).append(": (empty)\n");
            }
        }
    }

    private void dumpPCRTestRecords(StringBuilder sb, byte[] blockBytes, int validCount, RecordTypeInfo typeInfo) {
        for (int r = 0; r < typeInfo.blockingFactor; r++) {
            int offset = 8 + (r * typeInfo.recordSize);
            byte[] recordBytes = new byte[typeInfo.recordSize];
            System.arraycopy(blockBytes, offset, recordBytes, 0, typeInfo.recordSize);

            if (r < validCount) {
                PCRTest test = new PCRTest();
                try {
                    test.fromBytes(recordBytes);
                    sb.append("    Record ").append(r).append(": ").append(test.toString()).append("\n");
                } catch (Exception e) {
                    sb.append("    Record ").append(r).append(": (parse error: ").append(e.getMessage()).append(")\n");
                }
            } else {
                sb.append("    Record ").append(r).append(": (empty)\n");
            }
        }
    }

    private static class Metadata {
        int totalBlocks;
        List<Integer> partiallyFree;
        List<Integer> fullyEmpty;
        byte[] rawBytes;
    }

    private Metadata loadMetadata() {
        try (RandomAccessFile raf = new RandomAccessFile(metaFile, "r")) {
            if (raf.length() == 0) return null;

            byte[] bytes = new byte[(int) raf.length()];
            raf.readFully(bytes);

            Metadata meta = new Metadata();
            meta.rawBytes = bytes;

            java.io.DataInputStream dis = new java.io.DataInputStream(new java.io.ByteArrayInputStream(bytes));
            meta.totalBlocks = dis.readInt();

            int partialCount = dis.readInt();
            meta.partiallyFree = new ArrayList<>();
            for (int i = 0; i < partialCount; i++) {
                meta.partiallyFree.add(dis.readInt());
            }

            int emptyCount = dis.readInt();
            meta.fullyEmpty = new ArrayList<>();
            for (int i = 0; i < emptyCount; i++) {
                meta.fullyEmpty.add(dis.readInt());
            }

            return meta;
        } catch (IOException e) {
            return null;
        }
    }

    private String metadataToBinary(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        int limit = Math.min(64, bytes.length);
        for (int i = 0; i < limit; i++) {
            if (i > 0 && i % 4 == 0) sb.append(" ");
            sb.append(String.format("%8s", Integer.toBinaryString(bytes[i] & 0xFF)).replace(' ', '0'));
            if (i < limit - 1) sb.append(" ");
        }
        return sb.toString();
    }
}
