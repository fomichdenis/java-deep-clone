import entities.Person;
import entities.RelativeType;
import entities.TestEntityWithFinalFields;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static utils.CopyUtils.deepClone;

public class Main {

    public static void main(String[] args) throws IllegalAccessException {
        TestEntityWithFinalFields test = new TestEntityWithFinalFields(1, 100L, "NAME");
        TestEntityWithFinalFields test1 = new TestEntityWithFinalFields(1, 100L, "NAME");

        System.out.println(checkTestEntityWithFinalFieldsIsCopiedCorrectly(test, test1));

        String originalSecondName = "Green";
        AtomicLong familyBudget = new AtomicLong(123L);

        //Create children
        Person ch1 = new Person("Ch1", originalSecondName, 5, new ArrayList<>(Arrays.asList("book1", "book2")), familyBudget);
        Person ch2 = new Person("Ch2", originalSecondName, 4, new ArrayList<>(Arrays.asList("book3", "book4")), familyBudget);
        Set<Person> children = new HashSet<>(Arrays.asList(ch1, ch2));

        // Create mr and mrs (spouses)
        Person mrs = new Person("Misses", originalSecondName, 30, new ArrayList<>(Arrays.asList("123", "345")), null, children, familyBudget);
        Person mr = new Person("Mister", originalSecondName, 30, new ArrayList<>(Arrays.asList("678", "986")), mrs, children, familyBudget);
        mrs.setSpouse(mr);

        //set relatives for children
        ch1.getRelatives().put(RelativeType.FATHER, mr);
        ch1.getRelatives().put(RelativeType.MOTHER, mrs);
        ch2.getRelatives().put(RelativeType.FATHER, mr);
        ch2.getRelatives().put(RelativeType.MOTHER, mrs);

        Person copy = deepClone(mr);

        System.out.println("Mr comparing result = " + checkPersonIsCopiedCorrectly(mr, copy));
        System.out.println("Mrs comparing result = " + checkPersonIsCopiedCorrectly(mrs, copy.getSpouse()));

        System.out.println("Original object: " + mr);
        System.out.println("Copied object: " + copy);
    }

    private static boolean checkTestEntityWithFinalFieldsIsCopiedCorrectly(TestEntityWithFinalFields t1, TestEntityWithFinalFields t2) {
        if (t1 == t2) {
            return false;
        }
        return (t1.getLg() == null && t2.getLg() == null || t1.getLg().equals(t2.getLg()))
                && (t1.getName() == null && t2.getName() == null || t1.getName().equals(t2.getName()))
                && (t1.getNum() == null && t2.getNum() == null || t1.getNum().equals(t2.getNum()));
    }



    private static boolean checkPersonIsCopiedCorrectly(Person p1, Person p2) {
        // check that object are different
        if (p1 == p2) {
            return false;
        }
        // check that nulls for p1 are nulls for p2 and nulls for p2 are nulls for p1

        if (p1.getFavoriteBooks() == p2.getFavoriteBooks() || !p1.getFavoriteBooks().equals(p2.getFavoriteBooks())) {
            return false;
        }
        if (!(p1.getRelatives() == null && p2.getRelatives() == null ) && !(p1.getRelatives() != null && p2.getRelatives() != null)) {
            return false;
        }
        if (!(p1.getChildren() == null && p2.getChildren() == null ) && !(p1.getChildren() != null && p2.getChildren() != null)) {
            return false;
        }
        if (!(p1.getSpouse() == null && p2.getSpouse() == null ) && !(p1.getSpouse() != null && p2.getSpouse() != null)) {
            // we don't check spouses in that method to avoid recursion
            return false;
        }
        // check values for not-copied fields (primitives and others)
        boolean result = (p1.getAge() == p2.getAge())
                && (p1.getName() == null && p2.getName() == null || p1.getName().equals(p2.getName()))
                && (p1.getFamilyBudget() == null && p2.getFamilyBudget() == null || p1.getFamilyBudget().get() == p2.getFamilyBudget().get())
                && (p1.getOriginalSecondName() == null && p2.getOriginalSecondName() == null || p1.getOriginalSecondName().equals(p2.getOriginalSecondName()));
        //check children are equal
        if (p1.getChildren() != null) {
            for (Person ch1 : p1.getChildren()) {
                Person ch2 = p2.getChildren().stream()
                        .filter(ch -> ch.getName().equals(ch1.getName()))
                        .findFirst()
                        .orElse(null);
                if (ch2 == null) {
                    return false;
                }
                result = result && checkPersonIsCopiedCorrectly(ch1, ch2);
                if (result) {
                    // check reference is the same
                    if (ch1.getRelatives().get(RelativeType.MOTHER) == p1.getSpouse() && ch2.getRelatives().get(RelativeType.MOTHER) != p2.getSpouse()) {
                        return false;
                    }
                    if (ch1.getRelatives().get(RelativeType.FATHER) == p1 && ch2.getRelatives().get(RelativeType.FATHER) != p2) {
                        return false;
                    }
                }
            }
        }



        return result;
    }

}
