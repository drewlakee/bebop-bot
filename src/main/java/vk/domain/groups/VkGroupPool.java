package vk.domain.groups;

import java.util.ArrayList;
import java.util.Random;

public class VkGroupPool {

    private final ArrayList<VkGroup> pool;
    private final Random random;

    public VkGroupPool(ArrayList<VkGroup> groups) {
        this.pool = groups;
        this.random = new Random();
    }

    public final ArrayList<VkGroup> getPool() {
        return pool;
    }

    public final VkGroup getRandomGroup() {
        int randomIndex = random.nextInt(pool.size());
        return pool.get(randomIndex);
    }
}
