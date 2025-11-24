package S2.BaseClasses;

import S2.HeapFile.IRecord;

import java.io.*;
import java.util.Date;

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
        if (!(other instanceof PersonBinary)) return false;
        PersonBinary p = (PersonBinary) other;
        return id != null ? id.equals(p.id) : p.id == null;
    }

    @Override
    public int getSize() {
        return getBytes().length;
    } // zle KOLEGA, dat tam fixnu velkost (15 znakov, 14, datum, 10)

    // vyskusat zapisat osobu, kt meno je max dlzka mena (ľ+ýšžťť+áýžľášýčž) (peťo) aby nimi bol zapisany cely blok
    @Override
    public byte[] getBytes() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {
            dos.writeUTF(name != null ? name : "");
            dos.writeUTF(surname != null ? surname : "");
            dos.writeLong(birthDate != null ? birthDate.getTime() : -1L);
            dos.writeUTF(id != null ? id : "");
            dos.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void fromBytes(byte[] bytes) {
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes))) {
            this.name = dis.readUTF();
            this.surname = dis.readUTF();
            long time = dis.readLong();
            this.birthDate = time >= 0 ? new Date(time) : null;
            this.id = dis.readUTF();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
