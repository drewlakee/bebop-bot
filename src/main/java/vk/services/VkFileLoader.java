package vk.services;

import vk.domain.VkGroupProvider;
import vk.domain.groups.VkGroup;
import vk.domain.groups.VkGroupPool;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class VkFileLoader {

    /**
     *  Regexp for String.split(): group attributes from files
     *
     *  Format: <group name> <group_id(or owner_id)> <url>
     *  Result of split: String[4] = {"", group name, group_id, url}
     *  TODO: [0] == "" - need to make better regexp
     */
    private static final String groupAttributesSplitRegexp = "(\\s+)?[<>](\\s+<?)?";

    public static void loadVkGroups() {
        VkGroupPool audioGroupPool = new VkGroupPool(loadGroups("audioGroups"));
        VkGroupPool photoGroupPool = new VkGroupPool(loadGroups("photoGroups"));
        VkGroupPool hostGroupPool = new VkGroupPool(loadGroups("hostGroups"));
        VkGroupProvider.init(audioGroupPool, photoGroupPool, hostGroupPool);
    }

    public static ArrayList<VkGroup> loadGroups(String filename) {
        return loadGroups(filename, null);
    }

    public static ArrayList<VkGroup> loadGroups(File file) {
        return loadGroups(null, file);
    }

    private static ArrayList<VkGroup> loadGroups(String filename, File file) {
        ArrayList<VkGroup> groups;

        if (file == null) {
            groups = filterLoadFile(filename);
        } else
            groups = filterLoadFile(file);

        return groups;
    }

    private static ArrayList<VkGroup> filterLoadFile(String filename) {
        return filterLoadFile(null, filename);
    }

    private static ArrayList<VkGroup> filterLoadFile(File file) {
        return filterLoadFile(file, null);
    }

    private static ArrayList<VkGroup> filterLoadFile(File file, String filename) {
        ArrayList<VkGroup> groups;

        if (file == null) {
            File defaultFile = new File(
                    VkFileLoader.class.getClassLoader().getResource(filename).getFile()
            );
            groups = readGroupsFile(defaultFile);
        } else
            groups = readGroupsFile(file);

        return groups;
    }

    private static ArrayList<VkGroup> readGroupsFile(File file) {
        ArrayList<VkGroup> groups = new ArrayList<>();
        List<String> allLinesOfFile = readLines(file);

        for(String attributes : allLinesOfFile) {
            String[] groupAttributes = attributes.split(groupAttributesSplitRegexp);

            if (groupAttributes.length == 4) {
                groups.add(new VkGroup(
                        groupAttributes[1],
                        Integer.parseInt(groupAttributes[2]),
                        groupAttributes[3]
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
