package who;

public interface Exportable {
    String serialize();
    void deserialize(String data);
}
