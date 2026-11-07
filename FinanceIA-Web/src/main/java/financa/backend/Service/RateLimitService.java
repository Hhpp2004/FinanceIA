package financa.backend.Service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String key, Bandwidth bandwidth){
        return buckets.computeIfAbsent(key, k -> Bucket.builder().addLimit(bandwidth).build());
    }
}
