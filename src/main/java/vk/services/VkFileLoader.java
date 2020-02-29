package vk.services;

import vk.domain.VkGroupProvider;
import vk.domain.groups.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class VkFileLoader {

    public static void loadVkGroups() {
        VkAudioGroupPool audioGroupPool = new VkAudioGroupPool(loadAudioGroups());
        VkPhotoGroupPool photoGroupPool = new VkPhotoGroupPool(loadPhotoGroups());
        VkGroupProvider.init(audioGroupPool, photoGroupPool);
    }

    public static ArrayList<VkGroup> loadAudioGroups() {
        return loadAudioGroups(null);
    }

    public static ArrayList<VkGroup> loadPhotoGroups() {
        return loadPhotoGroups(null);
    }

    public static ArrayList<VkGroup> loadAudioGroups(File file) {
        ArrayList<VkGroup> groups;

        if (file == null) {
            File defaultFile = new File(
                    VkFileLoader.class.getClassLoader().getResource("audioGroups").getFile()
            );
            groups = readAudioGroupsFile(defaultFile);
        } else
            groups = readAudioGroupsFile(file);

        return groups;
    }

    public static ArrayList<VkGroup> loadPhotoGroups(File file) {
        ArrayList<VkGroup> groups;

        if (file == null) {
            File defaultFile = new File(
                    VkFileLoader.class.getClassLoader().getResource("photoGroups").getFile()
            );
            groups = readPhotoGroupsFile(defaultFile);
        } else
            groups = readPhotoGroupsFile(file);

        return groups;
    }

    private static ArrayList<VkGroup> readAudioGroupsFile(File file) {
        ArrayList<VkGroup> groups = new ArrayList<>();
        List<String> allLinesOfFile = readFile(file);

        for(String groupId : allLinesOfFile)
            groups.add(new VkAudioGroup(Integer.parseInt(groupId)));

        return groups;
    }

    private static ArrayList<VkGroup> readPhotoGroupsFile(File file) {
        ArrayList<VkGroup> groups = new ArrayList<>();
        List<String> allLinesOfFile = readFile(file);

        for(String groupId : allLinesOfFile)
            groups.add(new VkPhotoGroup(Integer.parseInt(groupId)));

        return groups;
    }

    private static List<String> readFile(File file) {
        List<String> allLinesOfFile = new ArrayList<>();

        try {
            allLinesOfFile = Files.readAllLines(Path.of(file.toURI()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return allLinesOfFile;
    }
}
