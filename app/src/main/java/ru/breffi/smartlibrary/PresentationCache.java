package ru.breffi.smartlibrary;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import ru.breffi.story.domain.models.PresentationEntity;

public class PresentationCache {

    private static Map<Integer, PresentationEntity> sDataMap = new HashMap<>();

    public static void put(PresentationEntity presentationEntity) {
        sDataMap.put(presentationEntity.getId(), presentationEntity);
    }

    @Nullable
    public static PresentationEntity peek(int id) {
        return sDataMap.get(id);
    }

    @Nullable
    public static PresentationEntity pop(int id) {
        PresentationEntity presentationEntity = sDataMap.get(id);
        sDataMap.remove(id);
        return presentationEntity;
    }
}
