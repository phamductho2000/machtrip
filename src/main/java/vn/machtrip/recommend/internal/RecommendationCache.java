package vn.machtrip.recommend.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import vn.machtrip.recommend.Recommendation;
import vn.machtrip.recommend.RecommendationRequest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

/** Redis cache of recommendation results. Fails open: a Redis problem means a cache miss, never an error. */
@Component
class RecommendationCache {

    private static final Logger log = LoggerFactory.getLogger(RecommendationCache.class);
    private static final String KEY_PREFIX = "machtrip:reco:v1:";
    private static final TypeReference<List<Recommendation>> RESULT_TYPE = new TypeReference<>() {
    };

    private final StringRedisTemplate redis;
    private final ObjectMapper json;
    private final RecommendProperties props;

    RecommendationCache(StringRedisTemplate redis, ObjectMapper json, RecommendProperties props) {
        this.redis = redis;
        this.json = json;
        this.props = props;
    }

    /** SHA-256 of the canonical request JSON; tag order and case do not change the key. */
    String keyFor(RecommendationRequest r) {
        var canonical = new RecommendationRequest(r.energy(), r.budgetLevel(),
                RecommendationEngine.normalizeTags(r.tags()), r.partyType(), r.lat(), r.lng(), r.maxDistanceMeters());
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(json.writeValueAsString(canonical).getBytes(StandardCharsets.UTF_8));
            return KEY_PREFIX + HexFormat.of().formatHex(digest);
        } catch (JsonProcessingException | NoSuchAlgorithmException e) {
            throw new IllegalStateException("Cannot build cache key", e);
        }
    }

    Optional<List<Recommendation>> get(String key) {
        try {
            String cached = redis.opsForValue().get(key);
            return cached == null ? Optional.empty() : Optional.of(json.readValue(cached, RESULT_TYPE));
        } catch (RuntimeException | JsonProcessingException e) {
            log.warn("Recommendation cache read failed, treating as miss: {}", e.toString());
            return Optional.empty();
        }
    }

    void put(String key, List<Recommendation> result) {
        try {
            redis.opsForValue().set(key, json.writeValueAsString(result), props.cacheTtl());
        } catch (RuntimeException | JsonProcessingException e) {
            log.warn("Recommendation cache write failed: {}", e.toString());
        }
    }
}
