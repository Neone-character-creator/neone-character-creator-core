package io.github.thisisnozaku.charactercreator.data.pdf;

import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class PdfCache {
    private PassiveExpiringMap<String, byte[]> cache = new PassiveExpiringMap<>(
            60_000
    );

    public String addToCache(byte[] pdfData){
        String uuid = UUID.randomUUID().toString();
        cache.put(uuid, pdfData);
        return uuid;
    }

    public Optional<byte[]> getFromCache(String uuid) {
        return Optional.ofNullable(cache.get(uuid));
    }
}
