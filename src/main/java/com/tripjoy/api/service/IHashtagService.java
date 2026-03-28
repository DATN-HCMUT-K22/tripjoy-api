package com.tripjoy.api.service;

import java.util.Set;
import com.tripjoy.api.entity.Hashtag;

public interface IHashtagService {
    Set<Hashtag> syncHashtags(Set<String> names);
}
