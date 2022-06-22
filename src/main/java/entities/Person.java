package entities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;


// Test class
public class Person {
    private String name;

    private final String originalSecondName;

    private int age;

    private List<String> favoriteBooks;
    private Person spouse;
    private Set<Person> children;
    private Map<RelativeType, Person> relatives = new HashMap<>();
    public static final String species = "HUMAN";
    private AtomicLong familyBudget;
    private AtomicBoolean someBoolean = new AtomicBoolean(true);

    public Person(){
        this.originalSecondName = "DEFAULT";
    }

    public Person(String name, String originalSecondName, int age, List<String> favoriteBooks, AtomicLong familyBudget) {
        this.name = name;
        this.originalSecondName = originalSecondName;
        this.age = age;
        this.favoriteBooks = favoriteBooks;
        this.familyBudget = familyBudget;
    }

    public Person(String name, String originalSecondName, int age, List<String> favoriteBooks, Person spouse, AtomicLong familyBudget) {
        this.name = name;
        this.originalSecondName = originalSecondName;
        this.age = age;
        this.favoriteBooks = favoriteBooks;
        this.spouse = spouse;
        this.familyBudget = familyBudget;
    }

    public Person(String name, String originalSecondName, int age, List<String> favoriteBooks, Person spouse, Set<Person> children, AtomicLong familyBudget) {
        this.name = name;
        this.originalSecondName = originalSecondName;
        this.age = age;
        this.favoriteBooks = favoriteBooks;
        this.spouse = spouse;
        this.children = children;
        this.familyBudget = familyBudget;
    }

    public Person(String name, String originalSecondName, int age, List<String> favoriteBooks,
                  Person spouse, Set<Person> children, Map<RelativeType, Person> relatives, AtomicLong familyBudget) {
        this.name = name;
        this.originalSecondName = originalSecondName;
        this.age = age;
        this.favoriteBooks = favoriteBooks;
        this.spouse = spouse;
        this.children = children;
        this.relatives = relatives;
        this.familyBudget = familyBudget;
    }

    public String getOriginalSecondName() {
        return originalSecondName;
    }

    public static String getSpecies() {
        return species;
    }

    public AtomicLong getFamilyBudget() {
        return familyBudget;
    }

    public AtomicBoolean getSomeBoolean() {
        return someBoolean;
    }

    public Map<RelativeType, Person> getRelatives() {
        return relatives;
    }

    public void setRelatives(Map<RelativeType, Person> relatives) {
        this.relatives = relatives;
    }

    public Set<Person> getChildren() {
        return children;
    }

    public void setChildren(Set<Person> children) {
        this.children = children;
    }

    public Person getSpouse() {
        return spouse;
    }

    public void setSpouse(Person spouse) {
        this.spouse = spouse;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public List<String> getFavoriteBooks() {
        return favoriteBooks;
    }

    public void setFavoriteBooks(List<String> favoriteBooks) {
        this.favoriteBooks = favoriteBooks;
    }


    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", originalSecondName='" + originalSecondName + '\'' +
                ", age=" + age +
                ", favoriteBooks=" + favoriteBooks +
                ", spouse=" + (spouse == null ? null : spouse.toStringWithoutRecursion()) +
                ", children=" + (children == null ? null : children.stream().map(Person::toStringWithoutRecursion).collect(Collectors.toSet())) +
                ", relatives=" + relatives +
                ", familyBudget=" + familyBudget +
                ", someBoolean=" + someBoolean +
                '}';
    }

    public String toStringWithoutRecursion() {
        return "Person{" +
                "name='" + name + '\'' +
                ", originalSecondName='" + originalSecondName + '\'' +
                ", age=" + age +
                ", favoriteBooks=" + favoriteBooks +
                ", children=" + (children == null ? null : children.stream().map(Person::toStringWithoutRecursion).collect(Collectors.toSet())) +
                ", familyBudget=" + familyBudget +
                ", someBoolean=" + someBoolean +
                '}';
    }
}