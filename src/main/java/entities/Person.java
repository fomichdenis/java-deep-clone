package entities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


// Test class
public class Person {
    private String name;
    private final String originalSecondName;
    private int age;
    private List<String> favoriteBooks;
    private Person spouse;
    private Set<Person> children;
    private Map<RelativeType, Person> relatives = new HashMap<>();
    public static String species = "HUMAN";

    public Person(){
        this.originalSecondName = "DEFAULT";
    }

    public Person(String name, String originalSecondName, int age, List<String> favoriteBooks) {
        this.name = name;
        this.originalSecondName = originalSecondName;
        this.age = age;
        this.favoriteBooks = favoriteBooks;
    }

    public Person(String name, String originalSecondName, int age, List<String> favoriteBooks, Person spouse) {
        this.name = name;
        this.originalSecondName = originalSecondName;
        this.age = age;
        this.favoriteBooks = favoriteBooks;
        this.spouse = spouse;
    }

    public Person(String name, String originalSecondName, int age, List<String> favoriteBooks, Person spouse, Set<Person> children) {
        this.name = name;
        this.originalSecondName = originalSecondName;
        this.age = age;
        this.favoriteBooks = favoriteBooks;
        this.spouse = spouse;
        this.children = children;
    }

    public Person(String name, String originalSecondName, int age, List<String> favoriteBooks,
                  Person spouse, Set<Person> children, Map<RelativeType, Person> relatives) {
        this.name = name;
        this.originalSecondName = originalSecondName;
        this.age = age;
        this.favoriteBooks = favoriteBooks;
        this.spouse = spouse;
        this.children = children;
        this.relatives = relatives;
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
        return "name = " + name + "," +
                "age = " + age + "," +
                "favoriteBooks = " + favoriteBooks + "," +
                "spouse = " + (spouse != null ? spouse.hashCode() : null);
    }

}