package com.tripjoy.api.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripjoy.api.entity.Theme;
import com.tripjoy.api.repository.ThemeRepository;
import com.tripjoy.api.service.IThemeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ThemeService implements IThemeService {

    private final ThemeRepository themeRepository;

    @Override
    @Transactional
    public Set<Theme> syncThemes(Set<String> names) {
        if (names == null || names.isEmpty()) {
            return Collections.emptySet();
        }

        Set<String> cleanedNames = names.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toSet());

        if (cleanedNames.isEmpty()) {
            return Collections.emptySet();
        }

        // Batch fetch
        List<Theme> existingThemes = themeRepository.findByNameIn(cleanedNames);
        Map<String, Theme> existingMap = existingThemes.stream()
                .collect(Collectors.toMap(Theme::getName, t -> t));

        // Sync logic
        return cleanedNames.stream().map(name -> {
            if (existingMap.containsKey(name)) {
                return existingMap.get(name);
            } else {
                Theme newTheme = Theme.builder().name(name).build();
                return themeRepository.save(newTheme);
            }
        }).collect(Collectors.toSet());
    }
}
