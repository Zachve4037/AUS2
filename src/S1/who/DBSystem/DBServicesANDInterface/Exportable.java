package S1.who.DBSystem.DBServicesANDInterface;

public interface Exportable {
    String serialize();
    void deserialize(String data);
}
