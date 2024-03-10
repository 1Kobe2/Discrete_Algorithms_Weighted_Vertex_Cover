package be.ugent.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

public class TestFileDatabase implements Iterable<Map.Entry<String, Integer>> {

    private Map<String, Integer> testFiles;

    public TestFileDatabase() {
        this.testFiles = new HashMap<>(
                Map.ofEntries(

                )
        );
    }

    public int getMaxClique(String name) {
        return testFiles.get(name);
    }

    public Map<String, Integer> getTestFiles() {
        return testFiles;
    }

    @Override
    public Iterator<Map.Entry<String, Integer>> iterator() {
        return this.testFiles.entrySet().iterator();
    }

    @Override
    public void forEach(Consumer<? super Map.Entry<String, Integer>> action) {
        this.testFiles.entrySet().forEach(action);
    }
}