package org.example.assignment1;


import java.util.*;
import java.util.concurrent.*;

public class Main {

    public static void main(String[] args) {

        arrayListDemo();
        copyOnWriteArrayListDemo();
        hashSetDemo();
        treeSetDemo();
        hashMapDemo();
        treeMapDemo();
        concurrentHashMapDemo();
    }

    // 1. ArrayList
    public static void arrayListDemo() {

        System.out.println("\n--- ArrayList ---");

        try {
            List<String> list = new ArrayList<>();

            list.add("Rahul");
            list.add("Amit");
            list.add("Priya");
            list.add("Rahul");

            System.out.println("ArrayList: " + list);

        } catch (Exception e) {
            System.out.println("ArrayList Exception: " + e.getMessage());
        }
    }

    // 2. CopyOnWriteArrayList
    // Better for read-heavy concurrent operation
    public static void copyOnWriteArrayListDemo() {

        System.out.println("\n--- CopyOnWriteArrayList ---");

        try {
            CopyOnWriteArrayList<String> list =
                    new CopyOnWriteArrayList<>();

            list.add("Rahul");
            list.add("Amit");
            list.add("Priya");

            for (String name : list) {

                System.out.println("Reading: " + name);

                if (name.equals("Amit")) {
                    list.add("Rohit");
                }
            }

            System.out.println("Updated List: " + list);

        } catch (Exception e) {
            System.out.println("CopyOnWriteArrayList Exception: "
                    + e.getMessage());
        }
    }

    // 3. HashSet
    public static void hashSetDemo() {

        System.out.println("\n--- HashSet ---");

        try {
            Set<String> set = new HashSet<>();

            set.add("Rahul");
            set.add("Amit");
            set.add("Priya");
            set.add("Rahul");

            System.out.println("HashSet: " + set);

        } catch (Exception e) {
            System.out.println("HashSet Exception: " + e.getMessage());
        }
    }

    // 4. TreeSet
    public static void treeSetDemo() {

        System.out.println("\n--- TreeSet ---");

        try {
            Set<String> set = new TreeSet<>();

            set.add("Rahul");
            set.add("Amit");
            set.add("Priya");
            set.add("Rahul");

            System.out.println("TreeSet: " + set);

        } catch (Exception e) {
            System.out.println("TreeSet Exception: " + e.getMessage());
        }
    }

    // 5. HashMap
    public static void hashMapDemo() {

        System.out.println("\n--- HashMap ---");

        try {
            Map<Integer, String> map = new HashMap<>();

            map.put(101, "Rahul");
            map.put(102, "Amit");
            map.put(103, "Priya");

            // Updating existing key
            map.put(101, "Rohit");

            for (Map.Entry<Integer, String> entry :
                    map.entrySet()) {

                System.out.println(
                        entry.getKey() + " : " + entry.getValue()
                );
            }

        } catch (Exception e) {
            System.out.println("HashMap Exception: " + e.getMessage());
        }
    }

    // 6. TreeMap
    public static void treeMapDemo() {

        System.out.println("\n--- TreeMap ---");

        try {
            Map<Integer, String> map = new TreeMap<>();

            map.put(103, "Priya");
            map.put(101, "Rahul");
            map.put(102, "Amit");

            System.out.println("TreeMap: " + map);

        } catch (Exception e) {
            System.out.println("TreeMap Exception: " + e.getMessage());
        }
    }

    // 7. ConcurrentHashMap
    public static void concurrentHashMapDemo() {

        System.out.println("\n--- ConcurrentHashMap ---");

        try {
            Map<Integer, String> map =
                    new ConcurrentHashMap<>();

            map.put(101, "Rahul");
            map.put(102, "Amit");
            map.put(103, "Priya");

            System.out.println("ConcurrentHashMap: " + map);

        } catch (Exception e) {
            System.out.println("ConcurrentHashMap Exception: "
                    + e.getMessage());
        }
    }
}

