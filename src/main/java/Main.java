import entities.Person;
import entities.RelativeType;
import entities.TestEntityWithFinalFields;
import utils.CopyUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class Main {

    public static void main(String[] args) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        /*TestEntityWithFinalFields test = new TestEntityWithFinalFields(1, 100L, "123");
        TestEntityWithFinalFields testEntityWithFinalFields = CopyUtils.deepCopy(test);*/
         String originalSecondName = "Green";
        Person ch1 = new Person("Ch1", originalSecondName, 5, new ArrayList<>(Arrays.asList("book1", "book2")));
        Person ch2 = new Person("Ch2", originalSecondName, 4, new ArrayList<>(Arrays.asList("book3", "book4")));
        Set<Person> children = new HashSet<>(Arrays.asList(ch1, ch2));

        Person mrs = new Person("Misses", originalSecondName, 30, new ArrayList<>(Arrays.asList("123", "345")), null, children);
        Person mr = new Person("Mister", originalSecondName, 30, new ArrayList<>(Arrays.asList("678", "986")), mrs, children);
        mrs.setSpouse(mr);
        ch1.getRelatives().put(RelativeType.FATHER, mr);
        ch1.getRelatives().put(RelativeType.MOTHER, mrs);
        ch2.getRelatives().put(RelativeType.FATHER, mr);
        ch2.getRelatives().put(RelativeType.MOTHER, mrs);

        Person copy = CopyUtils.deepCopy(mr);

        mrs.getFavoriteBooks().add("000");
        mr.getFavoriteBooks().add("001");

        System.out.println(copy);
        System.out.println("end");
    }

}
