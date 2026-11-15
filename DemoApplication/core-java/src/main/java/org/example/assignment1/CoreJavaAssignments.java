package org.example.assignment1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Companion demonstrations for the Core Java assignment. Each demo handles exceptions locally.
 */
public class CoreJavaAssignments {
    public static void main(String[] args) {
        calculateSumOfNumbers();
        exceptionsDemo();
        finalDemo();
        abstractClassDemo();
        interfaceDemo();
        lambdaAndFunctionalInterfaceDemo();
        streamApiDemo();
    }

    static void exceptionsDemo() {
        System.out.println("\\n--- Exceptions ---");
        try (BufferedReader reader = new BufferedReader(new StringReader("checked exception demo"))) {
            System.out.println("Checked I/O: " + reader.readLine());
        } catch (IOException ex) {
            System.err.println("Checked exception: " + ex.getMessage());
        }
        try {
            int value = Integer.parseInt("not-a-number"); // unchecked NumberFormatException
            System.out.println(value);
        } catch (NumberFormatException ex) {
            System.err.println("Unchecked exception: " + ex.getMessage());
        }
        try {
            validateAge(15);
        } catch (InvalidAgeException ex) {
            System.err.println("Custom exception from throw/throws: " + ex.getMessage());
        } finally {
            System.out.println("finally always executes");
        }
    }

    static void validateAge(int age) throws InvalidAgeException {
        if (age < 18) {
            throw new InvalidAgeException("Age must be at least 18");
        }
    }

    static void finalDemo() {
        try {
            final int maxAttempts = 3;
            System.out.println("Final variable: " + maxAttempts);
            FinalChild child = new FinalChild();
            child.printMessage(); // final method cannot be overridden
            System.out.println("Final class instance: " + new LockedConfiguration().value());
        } catch (RuntimeException ex) {
            System.err.println("Final demo failed: " + ex.getMessage());
        }
    }

    static void abstractClassDemo() {
        try {
            Shape shape = new Circle(4);
            System.out.println("Circle area: " + shape.area());
        } catch (RuntimeException ex) {
            System.err.println("Abstract class demo failed: " + ex.getMessage());
        }
    }

    static void interfaceDemo() {
        try {
            Payment payment = amount -> System.out.println("Paid INR " + amount);
            payment.pay(500);
        } catch (RuntimeException ex) {
            System.err.println("Interface demo failed: " + ex.getMessage());
        }
    }

    static void lambdaAndFunctionalInterfaceDemo() {
        try {
            List<String> names = Arrays.asList("Asha", "Ravi", "Meera");
            names.sort((left, right) -> left.compareToIgnoreCase(right));
            names.forEach(name -> System.out.println("Lambda: " + name));
            Function<String, Integer> length = String::length;
            System.out.println("Functional interface result: " + length.apply("Java"));
        } catch (RuntimeException ex) {
            System.err.println("Lambda demo failed: " + ex.getMessage());
        }
    }

    static void streamApiDemo() {
        try {
            List<Integer> values = new ArrayList<>();
            for (int i = 1; i <= 100; i++) values.add(i);
            int evenSum = values.parallelStream().filter(n -> n % 2 == 0)
                    .mapToInt(Integer::intValue).sum();
            Map<Boolean, List<Integer>> partition = values.stream()
                    .collect(Collectors.partitioningBy(n -> n % 2 == 0));
            System.out.println("Bulk data even sum (1..100): " + evenSum);
            System.out.println("Even count: " + partition.get(true).size());
        } catch (RuntimeException ex) {
            System.err.println("Stream API demo failed: " + ex.getMessage());
        }
    }

    static class InvalidAgeException extends Exception {
        InvalidAgeException(String message) {
            super(message);
        }
    }

    static final class LockedConfiguration {
        String value() {
            return "configuration is immutable by inheritance";
        }
    }

    static class Parent {
        final void printMessage() {
            System.out.println("Final method executed");
        }
    }

    static class FinalChild extends Parent {
    }

    abstract static class Shape {
        abstract double area();
    }

    static class Circle extends Shape {
        private final double radius;

        Circle(double radius) {
            this.radius = radius;
        }

        @Override
        double area() {
            return Math.PI * radius * radius;
        }
    }

    @FunctionalInterface
    interface Payment {
        void pay(double amount);
    }

    public static void calculateSumOfNumbers() {
        String str = "{1,a,3,b,z,i,4,p,5,6, 7, k,11,19,q}".replace(" ", "");

        String list = str.chars()
                .filter(Character::isLetter)
                .mapToObj(c -> String.valueOf((char) c))
                .collect(Collectors.joining());


        System.out.println(list);

    }

}
