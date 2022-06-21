package entities;

public class TestEntityWithFinalFields {

    private final Integer num;
    private final Long lg;
    private final String name;

    public TestEntityWithFinalFields(Integer num, Long lg, String name) {
        this.num = num;
        this.lg = lg;
        this.name = name;
    }
}
