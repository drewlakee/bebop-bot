package vk.services;

import vk.domain.VkGroupProvider;
import vk.domain.groups.VkAudioGroupPool;
import vk.domain.groups.VkGroup;
import vk.domain.groups.VkPhotoGroupPool;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class VkFileLoader {

    public static void loadVkGroups() {
        VkAudioGroupPool audioGroupPool = new VkAudioGroupPool(loadAudioGroups("audioGroups"));
        VkPhotoGroupPool photoGroupPool = new VkPhotoGroupPool(loadPhotoGroups("photoGroups"));
        VkGroupProvider.init(audioGroupPool, photoGroupPool);
    }

    public static ArrayList<VkGroup> loadAudioGroups(String filename) {
        return loadAudioGroups(filename, null);
    }

    public static ArrayList<VkGroup> loadPhotoGroups(String filename) {
        return loadPhotoGroups(filename, null);
    }

    public static ArrayList<VkGroup> loadAudioGroups(File file) {
        return loadAudioGroups(null, file);
    }

    public static ArrayList<VkGroup> loadPhotoGroups(File file) {
        return loadPhotoGroups(null, file);
    }

    private static ArrayList<VkGroup> loadAudioGroups(String filename, File file) {
        ArrayList<VkGroup> groups;

        if (file == null) {
            groups = filterLoadFile(filename);
        } else
            groups = filterLoadFile(file);

        return groups;
    }

    private static ArrayList<VkGroup> loadPhotoGroups(String filename, File file) {
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

        for(String groupId : allLinesOfFile)
            groups.add(new VkGroup(Integer.parseInt(groupId)));

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
