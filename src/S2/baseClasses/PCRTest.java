package S2.baseClasses;

import S2.heapFile.IRecord;
import S2.linearHashing.IHash;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;

public class PCRTest implements IRecord, IHash {
    private LocalDateTime timeOfTest;
    private String patientId;
    private PersonBinary patient;
    private int testId;
    private boolean result;
    private double value;
    private String note;

    public PCRTest(LocalDateTime timeOfTest, String patientId, int testId, boolean result, double value, String note) {
        this.timeOfTest = timeOfTest;
        this.patientId = patientId;
        this.testId = testId;
        this.result = result;
        this.value = value;
        this.note = note;
    }

    public PCRTest() {
        this.timeOfTest = null;
        this.patientId = "";
        this.testId = 0;
        this.result = false;
        this.value = 0.0;
        this.note = "";
    }

    public void setTestPatient(PersonBinary patient) {
        this.patient = patient;
    }

    @Override
    public boolean equals(IRecord other) {
        if (this == other) return true;
        if (other == null) return false;
        if (!(other instanceof PCRTest p)) return false;
        return this.testId != 0 ? this.testId == p.testId : p.testId == 0;
    }

    @Override
    public int getSize() {
        return 289;
    }

    @Override
    public byte[] getBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(this.getSize());

        buffer.putLong(timeOfTest.toEpochSecond(java.time.ZoneOffset.UTC));
        buffer.putInt(timeOfTest.getNano());
        writeString(buffer, patientId != null ? patientId : "", 10);
        buffer.put(patient != null ? patient.getBytes() : new byte[176]);
        buffer.putInt(testId);
        buffer.put((byte) (result ? 1 : 0));
        buffer.putDouble(value);
        writeString(buffer, note != null ? note : "", 11);

        return buffer.array();
    }

    @Override
    public void fromBytes(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        long seconds = buffer.getLong();
        int nanos = buffer.getInt();
        this.timeOfTest = LocalDateTime.ofEpochSecond(seconds, nanos, java.time.ZoneOffset.UTC);
        this.patientId = readString(buffer, 10);

        byte[] personBytes = new byte[176];
        buffer.get(personBytes);
        PersonBinary person = new PersonBinary();
        person.fromBytes(personBytes);
        setTestPatient(person);

        this.testId = buffer.getInt();
        this.result = buffer.get() != 0;
        this.value = buffer.getDouble();
        this.note = readString(buffer, 11);
    }

    @Override
    public String toString() {
        return "PCRTest{" +
                "timeOfTest=" + timeOfTest +
                ", patientId='" + patientId + '\'' +
                ", patient=" + (patient != null ? patient.toString() : "null") +
                ", testId=" + testId +
                ", result=" + result +
                ", value=" + value +
                ", note='" + note + '\'' +
                '}';
    }

    private void writeString(ByteBuffer buffer, String str, int maxChars) {
        byte[] bytes = str.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        int maxBytes = maxChars * 4;

        if (bytes.length > maxBytes) {
            bytes = java.util.Arrays.copyOf(bytes, maxBytes);
        }

        buffer.putInt(bytes.length);
        buffer.put(bytes);

        for (int i = bytes.length; i < maxBytes; i++) {
            buffer.put((byte) 0);
        }
    }

    private String readString(ByteBuffer buffer, int maxChars) {
        int maxBytes = maxChars * 4;
        int length = buffer.getInt();

        if (length < 0 || length > maxBytes) {
            length = 0;
        }

        int bytesToRead = Math.min(length, maxBytes);
        byte[] bytes = new byte[bytesToRead];
        buffer.get(bytes);

        int remainingPadding = maxBytes - bytesToRead;
        buffer.position(buffer.position() + remainingPadding);

        return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
    }

    @Override
    public long getHashCode() {
        return 0;
    }
}
