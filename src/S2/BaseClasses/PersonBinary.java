package S2.BaseClasses;

import S2.HeapFile.IRecord;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class PersonBinary implements IRecord {
    private String name;
    private String surname;
    private Date birthDate;
    private String id;

    public PersonBinary(String name, String surname, Date birthDate, String id) {
        this.name = name;
        this.surname = surname;
        this.birthDate = birthDate;
        this.id = id;
    }

    public PersonBinary() {
        this.name = "";
        this.surname = "";
        this.birthDate = null;
        this.id = "";
    }

    @Override
    public boolean equals(IRecord other) {
        if (this == other) return true;
        if (other == null) return false;
        if (!(other instanceof PersonBinary p)) return false;
        return Objects.equals(id, p.id);
    }

    @Override
    public int getSize() {
        return 176;
    }

    @Override
    public byte[] getBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(this.getSize());

        writeString(buffer, name != null ? name : "", 15);
        writeString(buffer, surname != null ? surname : "", 14);
        buffer.putLong(birthDate != null ? birthDate.getTime() : -1L);
        writeString(buffer, id != null ? id : "", 10);

        if (buffer.position() != 176) {
            throw new AssertionError("Buffer position mismatch: " + buffer.position() + " != 176");
        }

        return buffer.array();
    }


    @Override
    public void fromBytes(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        this.name = readString(buffer, 15);
        this.surname = readString(buffer, 14);
        long time = buffer.getLong();
        this.birthDate = time >= 0 ? new Date(time) : null;
        this.id = readString(buffer, 10);
    }

    @Override
    public String toString() {
        String pid = this.getId() != null ? this.getId() : "<null>";
        String name = this.name != null ? this.name : "";
        String surname = this.surname != null ? this.surname : "";
        String birth = this.birthDate != null
                ? new SimpleDateFormat("yyyy-MM-dd").format(this.birthDate)
                : "";
        return String.format("Person[id=%s, name=%s, surname=%s, birth=%s]", pid, name, surname, birth);
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


    public String getId() {
        return this.id;
    }
}
