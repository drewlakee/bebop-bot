package github.drewlakee.vk.domain.groups;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VkGroupsFileConfiguration implements VkGroupsConfiguration {

    private static final Logger log = LoggerFactory.getLogger(VkGroupsFileConfiguration.class);

    /**
     *  Regexp for String.split(): group attributes from files
     *
     *  Format: <group_objective> <group_name> <group_id> <url>
     *  Result of split: String[5] = {"", group_objective, group_name, group_id, url}
     *  TODO: [0] == "" - need to make better regexp
     */
    private static final String groupAttributesSplitRegexp = "(\\s+)?[<>](\\s+<?)?";

    @Override
    public Map<String, VkCustomGroup> loadGroups() {
        return loadGroups("groups");
    }

    public HashMap<String, VkCustomGroup> loadGroups(String filename) {
        return loadGroups(filename, null);
    }

    public HashMap<String, VkCustomGroup> loadGroups(File file) {
        return loadGroups(null, file);
    }

    private HashMap<String, VkCustomGroup> loadGroups(String filename, File file) {
        HashMap<String, VkCustomGroup> groups;

        if (file == null) {
            groups = filterLoadFile(filename);
        } else {
            groups = filterLoadFile(file);
        }

        log.info("[VK] VkGroups founded [" + ((file == null) ? filename : file.getName()) + "]: {}", groups);
        return groups;
    }

    private HashMap<String, VkCustomGroup> filterLoadFile(String filename) {
        return filterLoadFile(null, filename);
    }

    private HashMap<String, VkCustomGroup> filterLoadFile(File file) {
        return filterLoadFile(file, null);
    }

    private HashMap<String, VkCustomGroup> filterLoadFile(File file, String filename) {
        HashMap<String, VkCustomGroup> groups;

        if (file == null) {
            File projectFile = new File(this.getClass().getClassLoader().getResource(filename).getFile());
            groups = readGroupsFile(projectFile);
        } else {
            groups = readGroupsFile(file);
        }

        return groups;
    }

    private HashMap<String, VkCustomGroup> readGroupsFile(File file) {
        HashMap<String, VkCustomGroup> groups = new HashMap<>();
        List<String> allLinesOfFile = readLines(file);

        for(String attributes : allLinesOfFile) {
            String[] groupAttributes = attributes.split(groupAttributesSplitRegexp);

            if (groupAttributes.length == 5) {
                groups.put(groupAttributes[2], new VkCustomGroup(
                        VkGroupObjective.valueOf(groupAttributes[1]),
                        groupAttributes[2],
                        Integer.parseInt(groupAttributes[3]),
                        groupAttributes[4]
                ));
            }
        }

        return groups;
    }

    private List<String> readLines(File file) {
        List<String> allLinesOfFile = new ArrayList<>();

        try {
            allLinesOfFile = Files.readAllLines(Path.of(file.toURI()));
        } catch (IOException e) {
            log.error("[FILE READ] ERROR: " + file.getName() + " - read failed.");
            e.printStackTrace();
        }

        return allLinesOfFile;
    }
}
