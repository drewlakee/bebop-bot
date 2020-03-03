package vk.services;

import vk.domain.groups.GroupObjective;
import vk.domain.groups.VkGroup;
import vk.domain.groups.VkGroupPool;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VkFileLoader {

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

    public static HashMap<String, VkGroup> loadGroups(String filename) {
        return loadGroups(filename, null);
    }

    public static HashMap<String, VkGroup> loadGroups(File file) {
        return loadGroups(null, file);
    }

    private static HashMap<String, VkGroup> loadGroups(String filename, File file) {
        HashMap<String, VkGroup> groups;

        if (file == null) {
            groups = filterLoadFile(filename);
        } else
            groups = filterLoadFile(file);

        return groups;
    }

    private static HashMap<String, VkGroup> filterLoadFile(String filename) {
        return filterLoadFile(null, filename);
    }

    private static HashMap<String, VkGroup> filterLoadFile(File file) {
        return filterLoadFile(file, null);
    }

    private static HashMap<String, VkGroup> filterLoadFile(File file, String filename) {
        HashMap<String, VkGroup> groups;

        if (file == null) {
            File projectFile = new File(
                    VkFileLoader.class.getClassLoader().getResource(filename).getFile()
            );
            groups = readGroupsFile(projectFile);
        } else
            groups = readGroupsFile(file);

        return groups;
    }

    private static HashMap<String, VkGroup> readGroupsFile(File file) {
        HashMap<String, VkGroup> groups = new HashMap<>();
        List<String> allLinesOfFile = readLines(file);

        for(String attributes : allLinesOfFile) {
            String[] groupAttributes = attributes.split(groupAttributesSplitRegexp);

            if (groupAttributes.length == 5) {
                groups.put(groupAttributes[2], new VkGroup(
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
            e.printStackTrace();
        }

        return allLinesOfFile;
    }
}
