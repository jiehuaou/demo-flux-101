package com.example.flux;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

//@SpringBootTest
public class FluxFromTests {

    static class Person{
        private final Integer id;
        private final String firstName;
        private final String lastName;

        @Override
        public String toString() {
            return "Person{" +
                    "id=" + id +
                    ", firstName='" + firstName + '\'' +
                    ", lastName='" + lastName + '\'' +
                    '}';
        }

        Person(Integer id, String firstName, String lastName) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
        }
    }

    @Test
    public void testFromStream(){
        Flux.fromStream(Stream.of(
                new Person(1, "Name01", "Surname01"),
                new Person(2, "Name02", "Surname02"),
                new Person(3, "Name03", "Surname03")
        ))
                .log()
                .subscribe();
    }

    @Test
    public void testFromArray(){
        Person[] arrayPerson = new Person[]{
                new Person(1, "Name01", "Surname01"),
                new Person(2, "Name02", "Surname02"),
                new Person(3, "Name03", "Surname03")
        };
        Flux.fromArray(arrayPerson)
                .log()
                .subscribe();
    }

    @Test
    public void testFromIter(){
        List<Person> iter = Arrays.asList(
                new Person(1, "Name01", "Surname01"),
                new Person(2, "Name02", "Surname02"),
                new Person(3, "Name03", "Surname03")
        );
        Flux.fromIterable(iter)
                .log()
                .subscribe();
    }
}
