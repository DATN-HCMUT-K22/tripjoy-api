package com.tripjoy.api.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripjoy.api.entity.Hashtag;
import com.tripjoy.api.repository.HashtagRepository;
import com.tripjoy.api.service.IHashtagService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HashtagService implements IHashtagService {

    private final HashtagRepository hashtagRepository;

    @Override
    @Transactional
    public Set<Hashtag> syncHashtags(Set<String> names) {
        if (names == null || names.isEmpty()) {
            return Collections.emptySet();
        }

        // 1. Clean and lowercase names
        Set<String> cleanedNames = names.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        if (cleanedNames.isEmpty()) {
            return Collections.emptySet();
        }

        // 2. Fetch existing hashtags in one batch
        List<Hashtag> existingHashtags = hashtagRepository.findByNameIn(cleanedNames);
        Map<String, Hashtag> existingMap =
                existingHashtags.stream().collect(Collectors.toMap(Hashtag::getName, h -> h));

        // 3. Create missing hashtags
        Set<Hashtag> finalHashtags = cleanedNames.stream()
                .map(name -> {
                    if (existingMap.containsKey(name)) {
                        return existingMap.get(name);
                    } else {
                        Hashtag newHashtag = Hashtag.builder().name(name).build();
                        return hashtagRepository.save(newHashtag);
                    }
                })
                .collect(Collectors.toSet());

        return finalHashtags;
    }
}
