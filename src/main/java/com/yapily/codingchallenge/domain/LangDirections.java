package com.yapily.codingchallenge.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class LangDirections {
    private static final String ENGLISH = "en";
    private List<String> dirs;

    public Set<String> getLanguagesAvailableForTranslation() {
        return dirs.stream()
                .filter(dir -> dir.startsWith(ENGLISH))
                .map(dir -> dir.substring(dir.length() - 2))
                .collect(Collectors.toSet());
    }
}
