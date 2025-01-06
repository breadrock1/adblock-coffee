package com.example.adblock;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

import com.example.adblock.exception.RustException;

public class AdvtBlockerTest {

    private List<String> rules = new ArrayList<>(List.of(
        "-advertisement-icon.",
        "-advertisement-management/",
        "-advertisement.",
        "-advertisement/script."
    ));

    private AdvtBlocker advtBlocker = AdvtBlocker.createInstance(rules);

    @Test
    public void createInstanceTest() {
        AdvtBlocker blocker = AdvtBlocker.createInstance(rules);
        assertTrue(blocker != null);
    }

    @Test
    public void destroyInstanceTest() {
        AdvtBlocker blocker = AdvtBlocker.createInstance(rules);
        assertTrue(blocker != null);

        blocker.destroyInstance();
        assertTrue(true);
    }

    @Test
    public void checkBaseCaseTest() {
        boolean result = advtBlocker.checkUrls(
            "http://example.com/-advertisement-icon.",
            "http://example.com/helloworld",
            "image"
        );

        assertTrue(result);
    }

    @Test
    public void checkBaseCaseMultipleTimesTest() {
        boolean result1 = advtBlocker.checkUrls(
            "http://example.com/-advertisement-icon.",
            "http://example.com/helloworld",
            "image"
        );
        assertTrue(result1);

        boolean result2 = advtBlocker.checkUrls(
            "http://example.com/-some-icon.",
            "http://example.com/helloworld",
            "image"
        );
        assertFalse(result2);

        boolean result3 = advtBlocker.checkUrls(
            "http://example.com/-advertisement-icon.",
            "http://example.com/helloworld",
            "script"
        );
        assertTrue(result3);

        boolean result4 = advtBlocker.checkUrls(
            "http://example.com/-advertisement-icon.",
            "http://example.com/helloworld",
            "image"
        );
        assertTrue(result4);
    }

    @Test
    public void checkFromEaseRulesFilesTest() {
        try {
            ClassLoader classLoader = LoadLibraryHelper.class.getClassLoader();
            URL easyResource = classLoader.getResource("easylist.txt");
            URL easyPrivResource = classLoader.getResource("easyprivacy.txt");

            List<String> easyRules = Files.readAllLines(Paths.get(easyResource.toString()));
            List<String> easyPrivRules = Files.readAllLines(Paths.get(easyPrivResource.toString()));
            easyRules.addAll(easyPrivRules);

            AdvtBlocker blocker = AdvtBlocker.createInstance(easyRules);
            for (int i = 0; i < 3000; i++) {
                System.out.println(i);
                boolean result = advtBlocker.checkUrls(
                    "http://example.com/-advertisement-icon.",
                    "http://example.com/helloworld",
                    "her"
                );
                assertTrue(result);
            }
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }

    @Test
    public void checkMultipleInstancesTest() {
        AdvtBlocker advtBlocker1 = AdvtBlocker.createInstance(rules);
        AdvtBlocker advtBlocker2 = AdvtBlocker.createInstance(rules);

        assertTrue(advtBlocker1.getInstanceAddress() != advtBlocker2.getInstanceAddress());

        boolean result = advtBlocker1.checkUrls(
            "http://example.com/-advertisement-icon.",
            "http://example.com/helloworld",
            "image"
        );
        assertTrue(result);

        boolean result2 = advtBlocker2.checkUrls(
            "http://example.com/-advertisement-icon.",
            "http://example.com/helloworld",
            "image"
        );
        assertTrue(result);

        advtBlocker1.destroyInstance();
        advtBlocker2.destroyInstance();
    }

    @Test(expected = RustException.class)
    public void caughtRustExceptionTest() {
        AdvtBlocker advtBlocker = AdvtBlocker.createInstance(rules);
        advtBlocker.destroyInstance();
        advtBlocker.checkUrls(
            "http://example.com/-advertisement-icon.",
            "http://example.com/helloworld",
            "image"
        );
    }
}
