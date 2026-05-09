package com.tripjoy.api.service;

import java.util.Set;

import com.tripjoy.api.entity.Theme;

public interface IThemeService {
    Set<Theme> syncThemes(Set<String> names);
}
