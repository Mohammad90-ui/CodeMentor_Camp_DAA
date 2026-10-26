package com.codementor;

import com.codementor.model.*;
import com.codementor.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

/** Loads sample topics, problems and a demo student the first time the app starts (empty database only). */
@Component
public class DataSeeder implements CommandLineRunner {
    private final TopicRepository topics;
    private final ProblemRepository problems;
    private final UserRepository users;
    private final AttemptRepository attempts;
    private final BookmarkRepository bookmarks;
    private final PasswordEncoder encoder;

    public DataSeeder(TopicRepository topics, ProblemRepository problems, UserRepository users,
                      AttemptRepository attempts, BookmarkRepository bookmarks, PasswordEncoder encoder) {
        this.topics = topics; this.problems = problems; this.users = users;
        this.attempts = attempts; this.bookmarks = bookmarks; this.encoder = encoder;
    }

    // Title | difficulty | minutes | topics (comma separated, DP = Dynamic Programming) | description
    private static final String[] PROBLEMS = {
        "Two Sum|EASY|15|Arrays,Hash Maps|Given an array of integers and a target, return the indices of the two numbers that add up to the target.",
        "Move Zeroes|EASY|10|Arrays|Move all zeroes in an array to the end while keeping the order of the other elements, in place.",
        "Best Time to Buy and Sell Stock|EASY|15|Arrays|Given daily prices, find the maximum profit from one buy followed by one sell.",
        "Maximum Subarray|MEDIUM|25|Arrays,DP|Find the contiguous subarray with the largest sum and return that sum.",
        "Rotate Array|MEDIUM|20|Arrays|Rotate an array to the right by k steps using O(1) extra space.",
        "Merge Intervals|MEDIUM|30|Arrays,Sorting|Merge all overlapping intervals and return the non-overlapping result.",
        "Trapping Rain Water|HARD|45|Arrays|Given elevation heights, compute how much rain water can be trapped between the bars.",
        "Valid Palindrome|EASY|10|Strings|Check whether a string reads the same forwards and backwards after removing non-alphanumeric characters.",
        "Reverse Words in a String|EASY|15|Strings|Reverse the order of the words in a sentence and remove extra spaces.",
        "Valid Anagram|EASY|10|Strings,Hash Maps|Decide whether two strings contain exactly the same characters with the same counts.",
        "Longest Substring Without Repeating Characters|MEDIUM|30|Strings,Hash Maps|Find the length of the longest substring that has no repeated characters.",
        "Longest Palindromic Substring|MEDIUM|35|Strings,DP|Return the longest palindromic substring of a given string.",
        "Minimum Window Substring|HARD|50|Strings,Hash Maps|Find the smallest window of s that contains every character of t.",
        "Binary Search|EASY|15|Searching|Search a sorted array for a target value in O(log n) time and return its index or -1.",
        "Search in Rotated Sorted Array|MEDIUM|30|Searching,Arrays|Search for a target in a sorted array that has been rotated at an unknown pivot.",
        "First and Last Position of Element|MEDIUM|25|Searching,Arrays|Find the first and last index of a target in a sorted array using binary search.",
        "Find Peak Element|MEDIUM|25|Searching,Arrays|Find any index whose value is greater than both neighbours in O(log n).",
        "Median of Two Sorted Arrays|HARD|55|Searching,Arrays|Find the median of two sorted arrays in O(log(m+n)) time.",
        "Sort Colors|MEDIUM|20|Sorting,Arrays|Sort an array of 0s, 1s and 2s in place in a single pass.",
        "Implement Merge Sort|MEDIUM|30|Sorting,Recursion|Implement merge sort and explain why it runs in O(n log n).",
        "Kth Largest Element in an Array|MEDIUM|25|Sorting,Arrays|Find the kth largest element without fully sorting the array if possible.",
        "Sort Characters by Frequency|MEDIUM|20|Sorting,Hash Maps,Strings|Sort the characters of a string in decreasing order of frequency.",
        "Largest Number|MEDIUM|25|Sorting,Strings|Arrange non-negative integers so that they form the largest possible number.",
        "Count Inversions|HARD|45|Sorting,Recursion|Count the pairs (i, j) with i < j and a[i] > a[j] using a modified merge sort.",
        "Fibonacci Number|EASY|10|Recursion,DP|Compute the nth Fibonacci number recursively, then optimise it with memoization.",
        "Generate Parentheses|MEDIUM|30|Recursion|Generate all combinations of n pairs of well-formed parentheses.",
        "Subsets|MEDIUM|25|Recursion|Return all possible subsets (the power set) of a list of distinct integers.",
        "Permutations|MEDIUM|30|Recursion|Return all permutations of a list of distinct integers.",
        "N-Queens|HARD|55|Recursion|Place n queens on an n x n board so that no two attack each other.",
        "Reverse Linked List|EASY|15|Linked Lists|Reverse a singly linked list iteratively and recursively.",
        "Linked List Cycle|EASY|15|Linked Lists|Detect whether a linked list has a cycle using O(1) memory.",
        "Merge Two Sorted Lists|EASY|20|Linked Lists,Sorting|Merge two sorted linked lists into one sorted list.",
        "Remove Nth Node From End|MEDIUM|25|Linked Lists|Remove the nth node from the end of a linked list in one pass.",
        "Contains Duplicate|EASY|10|Hash Maps,Arrays|Return true if any value appears at least twice in an array.",
        "Group Anagrams|MEDIUM|25|Hash Maps,Strings,Sorting|Group the strings that are anagrams of each other.",
        "Top K Frequent Elements|MEDIUM|25|Hash Maps,Sorting|Return the k most frequent elements of an array.",
        "Climbing Stairs|EASY|15|DP|Count the distinct ways to climb n stairs taking 1 or 2 steps at a time.",
        "Coin Change|MEDIUM|35|DP|Find the fewest coins needed to make up a given amount.",
        "Longest Common Subsequence|MEDIUM|35|DP,Strings|Find the length of the longest common subsequence of two strings.",
        "0/1 Knapsack|HARD|50|DP|Choose items with given weights and values to maximise value within a weight limit."
    };

    @Override
    public void run(String... args) {
        if (topics.count() > 0) return;

        Map<String, Topic> topicMap = new LinkedHashMap<>();
        for (String name : List.of("Arrays", "Strings", "Searching", "Sorting", "Recursion",
                "Linked Lists", "Hash Maps", "Dynamic Programming"))
            topicMap.put(name, topics.save(new Topic(name)));

        Map<String, Problem> byTitle = new HashMap<>();
        for (String row : PROBLEMS) {
            String[] f = row.split("\\|");
            Problem p = new Problem();
            p.setTitle(f[0]);
            p.setDifficulty(Difficulty.valueOf(f[1]));
            p.setEstimatedMinutes(Integer.parseInt(f[2]));
            p.setDescription(f[4]);
            p.setExternalUrl("https://leetcode.com/problemset/?search=" + URLEncoder.encode(f[0], StandardCharsets.UTF_8));
            for (String t : f[3].split(","))
                p.getTopics().add(topicMap.get(t.equals("DP") ? "Dynamic Programming" : t));
            byTitle.put(f[0], problems.save(p));
        }

        // Demo student with a little history: Arrays is weak and unpractised for ~12 days.
        User demo = new User();
        demo.setName("Demo Student");
        demo.setEmail("demo@campus.edu");
        demo.setPasswordHash(encoder.encode("demo1234"));
        demo.setPreferredLanguage("Java");
        demo.setWeeklyGoal(5);
        users.save(demo);

        attempt(demo, byTitle.get("Maximum Subarray"), Result.UNSOLVED, 12, 40, "LOGIC_ERROR", "Could not see the Kadane recurrence.");
        attempt(demo, byTitle.get("Move Zeroes"), Result.PARTIAL, 12, 25, "EDGE_CASE", "Failed when the array had no zeroes.");
        attempt(demo, byTitle.get("Valid Palindrome"), Result.SOLVED, 2, 12, null, null);
        attempt(demo, byTitle.get("Valid Anagram"), Result.SOLVED, 3, 10, null, null);
        attempt(demo, byTitle.get("Binary Search"), Result.SOLVED, 1, 14, null, null);
        attempt(demo, byTitle.get("Sort Colors"), Result.PARTIAL, 5, 30, "CONCEPT_GAP", "Needs the Dutch national flag idea.");
        attempt(demo, byTitle.get("Kth Largest Element in an Array"), Result.UNSOLVED, 5, 35, "TIME_LIMIT", "Sorting was too slow for the follow-up.");
        attempt(demo, byTitle.get("Fibonacci Number"), Result.SOLVED, 4, 8, null, null);

        Bookmark b = new Bookmark();
        b.setUser(demo);
        b.setProblem(byTitle.get("Trapping Rain Water"));
        bookmarks.save(b);
    }

    private void attempt(User u, Problem p, Result r, int daysAgo, int minutes, String mistake, String notes) {
        Attempt a = new Attempt();
        a.setUser(u); a.setProblem(p); a.setResult(r);
        a.setTimeSpentMinutes(minutes); a.setMistakeType(mistake); a.setNotes(notes);
        a.setCreatedAt(LocalDateTime.now().minusDays(daysAgo));
        attempts.save(a);
    }
}
