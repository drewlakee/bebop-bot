package github.drewlakee.vk.services.content;

import github.drewlakee.vk.domain.attachments.VkAttachment;
import org.jvnet.hk2.annotations.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@Component
public class VkRandomPhotoCachedSearch implements VkContentSearchStrategy {

    private final VkRandomPhotoSearch service;

    private final double CACHED_SEARCH_QUANTITY_THRESHOLD = 3.5d;
    private final int CACHE_BOUND = (int) (100.0d * CACHED_SEARCH_QUANTITY_THRESHOLD);

    private final ConcurrentLinkedQueue<VkAttachment> cachedPhotosQueue = new ConcurrentLinkedQueue<>();

    @Autowired
    public VkRandomPhotoCachedSearch(VkRandomPhotoSearch service) {
        this.service = service;
    }

    @Override
    public List<VkAttachment> search(int quantity) {
        List<VkAttachment> result = new ArrayList<>();
        if (cachedPhotosQueue.isEmpty() || cachedPhotosQueue.size() <= (int) (quantity * CACHED_SEARCH_QUANTITY_THRESHOLD)) {
            List<VkAttachment> searchResult = service.search((int) (quantity * CACHED_SEARCH_QUANTITY_THRESHOLD));
            Collections.shuffle(searchResult);
            searchResult.forEach(attachment -> {
                if (result.size() < quantity) {
                    result.add(attachment);
                } else {
                    cachedPhotosQueue.add(attachment);
                }
            });
        } else {
            cachedPhotosQueue.stream().limit(quantity).forEach(attachment -> result.add(cachedPhotosQueue.poll()));
        }

        while (cachedPhotosQueue.size() >= CACHE_BOUND) {
            cachedPhotosQueue.poll();
        }

        return result;
    }
}
