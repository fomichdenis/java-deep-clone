package entities;

public class TestEntityWithFinalFields {

    public Integer getNum() {
        return num;
    }

    public Long getLg() {
        return lg;
    }

    public String getName() {
        return name;
    }

    private final Integer num;
    private final Long lg;
    private final String name;

    public TestEntityWithFinalFields(Integer num, Long lg, String name) {
        this.num = num;
        this.lg = lg;
        this.name = name;
    }
}
