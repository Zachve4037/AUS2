package who.DBSystem.DBServicesANDInterface;

public interface Exportable {
    String serialize();
    void deserialize(String data);
}
