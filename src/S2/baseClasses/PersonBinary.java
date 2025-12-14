package S2.baseClasses;

import S2.heapFile.IRecord;
import S2.linearHashing.IHash;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Objects;

public class PersonBinary implements IRecord, IHash {
    private String name;
    private String surname;
    private Date birthDate;
    private String id;
    private int testCount;
    private PCRTest[] tests;

    public PersonBinary(String name, String surname, Date birthDate, String id) {
        this.name = name;
        this.surname = surname;
        this.birthDate = birthDate;
        this.id = id;
        this.tests = new PCRTest[6];
    }

    public PersonBinary() {
        this.name = "";
        this.surname = "";
        this.birthDate = null;
        this.id = "";
        this.tests = new PCRTest[6];
    }

    public void setId(String id) {
        this.id = id;
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
        // name: 4 + 60 = 64
        // surname: 4 + 56 = 60
        // birthDate: 8
        // id: 4 + 40 = 44
        // testCount: 4
        // tests[6]: 6 * 268 = 1608
        return 1788;
    }

    @Override
    public byte[] getBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(this.getSize());

        writeString(buffer, name != null ? name : "", 15);
        writeString(buffer, surname != null ? surname : "", 14);
        buffer.putLong(birthDate != null ? birthDate.getTime() : -1L);
        writeString(buffer, id != null ? id : "", 10);
        buffer.putInt(this.testCount);

        for (int i = 0; i < 6; i++) {
            if (i < this.testCount && this.tests[i] != null) {
                buffer.put(tests[i].getBytes());
            } else {
                buffer.put(new byte[268]);
            }
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
        this.testCount = buffer.getInt();
        this.tests = new PCRTest[6];

        for (int i = 0; i < 6; i++) {
            byte[] testBytes = new byte[268];
            buffer.get(testBytes);

            if (i < this.testCount) {
                PCRTest test = new PCRTest();
                test.fromBytes(testBytes);
                this.tests[i] = test;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Person[id=").append(id)
                .append(", name=").append(name)
                .append(", surname=").append(surname)
                .append(", birth=").append(birthDate)
                .append(", testCount=").append(testCount)
                .append("]");

        if (testCount > 0) {
            sb.append(" Tests:");
            for (int i = 0; i < testCount; i++) {
                sb.append("\n  ").append(i + 1).append(". ").append(tests[i].toString());
            }
        }

        return sb.toString();
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

    @Override
    public long getHashCode() {
        if (this.id == null || this.id.isEmpty()) {
            return 0;
        }
        long hash = 0;
        for (int i = 0; i < this.id.length(); i++) {
            hash = 31 * hash + this.id.charAt(i);
        }
        return Math.abs(hash);
    }

    public int getTestCount() {
        return this.testCount;
    }

    public void setTestCount(int count) {
        this.testCount = count;
    }

    public String getName() {
        return this.name;
    }

    public String getSurname() {
        return this.surname;
    }

    public void addTest(PCRTest test) {
        if (this.testCount < 6) {
            this.tests[this.testCount] = test;
            this.testCount++;
        }
    }

    public void updateTest(PCRTest updatedTest) {
        for (int i = 0; i < this.testCount; i++) {
            if (this.tests[i] != null && this.tests[i].getId() == updatedTest.getId()) {
                this.tests[i] = updatedTest;
                return;
            }
        }
    }

    public PCRTest[] getTests() {
        return this.tests;
    }

    public void setTests(PCRTest[] tests) {
        this.tests = tests;
        this.testCount = 0;
        for (PCRTest test : tests) {
            if (test != null) {
                this.testCount++;
            }
        }
    }

    public Date getBirthDate() {
        return this.birthDate;
    }
}
