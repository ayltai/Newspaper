package com.github.ayltai.newspaper.language;

public enum Extractor {
    ENTITIES("entities"),
    TOPICS("topics"),
    WORDS("words"),
    PHRASES("phrases"),
    DEPENDENCY_TREES("dependency-trees"),
    RELATIONS("relations"),
    ENTAILMENTS("entailments"),
    SENSES("senses"),
    SPELLING("spelling");

    private final String name;

    Extractor(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
