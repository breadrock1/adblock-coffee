package com.example.simple;

import java.util.ArrayList;
import java.util.List;

//import com.example.adblock.AdvtBlocker;

/**
 * Simple example!
 */
public class App {
    static {
        try {
            System.loadLibrary("adblock_coffee");
            System.out.println("dfgdfg");
        } catch (UnsatisfiedLinkError ex) {
            System.err.println(ex.getMessage());
        }
    }

    public static void main(String[] args) {
        List<String> rules = new ArrayList<>(List.of(
            "-advertisement-icon.",
            "-advertisement-management/",
            "-advertisement.",
            "-advertisement/script."
        ));

//        AdvtBlocker blocker = AdvtBlocker.createInstance(rules);
//        boolean result = blocker.checkUrls(
//            "http://example.com/-advertisement-icon.",
//            "http://example.com/helloworld",
//            "image"
//        );
//
//        System.out.println(result);
    }
}
