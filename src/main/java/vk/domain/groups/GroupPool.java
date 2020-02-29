package vk.domain.groups;

import java.util.ArrayList;
import java.util.Random;

public abstract class GroupPool {

    private final ArrayList<Group> pool;
    private final Random random;

    protected GroupPool(ArrayList<Group> groups) {
        this.pool = groups;
        this.random = new Random();
    }

    public final Group getRandomGroup() {
        int randomIndex = random.nextInt(pool.size());
        return pool.get(randomIndex);
    }
}
