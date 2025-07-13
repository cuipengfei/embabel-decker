package com.embabel.template.decker_agent;

import com.embabel.agent.tools.file.PatternSearch;
import kotlin.text.Regex;

import java.util.List;

/**
 * Extends PatternSearch to provide specific methods for searching for symbols in code.
 */
public interface SymbolSearch extends PatternSearch {

    default Regex classPattern(String className) {
        return new Regex(
                "(^|\\s)(class|interface|object|enum\\s+class|data\\s+class|sealed\\s+class|abstract\\s+class)\\s+" +
                        className +
                        "\\b" +
                        "([<(]|\\s|$)"
        );
    }

    default List<PatternMatch> findClassInProject(
            String className,
            String globPattern,
            boolean useParallelSearch
    ) {
        return findPatternInProject(
                classPattern(className),
                globPattern,
                useParallelSearch
        );
    }

    default List<PatternMatch> findClassInProject(String className) {
        return findClassInProject(className, "**/*.{kt,java}", true);
    }
}
