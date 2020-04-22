package vk.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vk.domain.groups.GroupObjective;
import vk.domain.groups.VkCustomGroup;
import vk.domain.groups.VkGroupPool;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VkGroupsConfigurationService {

    private static final Logger log = LoggerFactory.getLogger(VkGroupsConfigurationService.class);

    /**
     *  Regexp for String.split(): group attributes from files
     *
     *  Format: <group_objective> <group_name> <group_id> <url>
     *  Result of split: String[5] = {"", group_objective, group_name, group_id, url}
     *  TODO: [0] == "" - need to make better regexp
     */
    private static final String groupAttributesSplitRegexp = "(\\s+)?[<>](\\s+<?)?";

    public static void loadVkGroups() {
        VkGroupPool.add(loadGroups("groups"));
    }

    public static HashMap<String, VkCustomGroup> loadGroups(String filename) {
        return loadGroups(filename, null);
    }

    public static HashMap<String, VkCustomGroup> loadGroups(File file) {
        return loadGroups(null, file);
    }

    private static HashMap<String, VkCustomGroup> loadGroups(String filename, File file) {
        HashMap<String, VkCustomGroup> groups;

        if (file == null) {
            groups = filterLoadFile(filename);
        } else
            groups = filterLoadFile(file);

        log.info("[VK] VkGroups founded [" + ((file == null) ? filename : file.getName()) + "]: {}", groups);
        return groups;
    }

    private static HashMap<String, VkCustomGroup> filterLoadFile(String filename) {
        return filterLoadFile(null, filename);
    }

    private static HashMap<String, VkCustomGroup> filterLoadFile(File file) {
        return filterLoadFile(file, null);
    }

    private static HashMap<String, VkCustomGroup> filterLoadFile(File file, String filename) {
        HashMap<String, VkCustomGroup> groups;

        if (file == null) {
            File projectFile = new File(
                    VkGroupsConfigurationService.class.getClassLoader().getResource(filename).getFile()
            );
            groups = readGroupsFile(projectFile);
        } else
            groups = readGroupsFile(file);

        return groups;
    }

    private static HashMap<String, VkCustomGroup> readGroupsFile(File file) {
        HashMap<String, VkCustomGroup> groups = new HashMap<>();
        List<String> allLinesOfFile = readLines(file);

        for(String attributes : allLinesOfFile) {
            String[] groupAttributes = attributes.split(groupAttributesSplitRegexp);

            if (groupAttributes.length == 5) {
                groups.put(groupAttributes[2], new VkCustomGroup(
                        GroupObjective.valueOf(groupAttributes[1]),
                        groupAttributes[2],
                        Integer.parseInt(groupAttributes[3]),
                        groupAttributes[4]
                ));
            }
        }

        return groups;
    }

    private static List<String> readLines(File file) {
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
