package vk.domain.groups;

import java.util.ArrayList;
import java.util.Random;

public abstract class VkGroupPool {

    private final ArrayList<VkGroup> pool;
    private final Random random;

    protected VkGroupPool(ArrayList<VkGroup> groups) {
        this.pool = groups;
        this.random = new Random();
    }

    public final VkGroup getRandomGroup() {
        int randomIndex = random.nextInt(pool.size());
        return pool.get(randomIndex);
    }
}
