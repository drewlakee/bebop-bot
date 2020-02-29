package vk.services;

import vk.domain.GroupProvider;
import vk.domain.groups.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class VkFileLoader {

    public static void loadVkGroups() {
        AudioGroupPool audioGroupPool = new AudioGroupPool(loadAudioGroups());
        PhotoGroupPool photoGroupPool = new PhotoGroupPool(loadPhotoGroups());
        GroupProvider.init(audioGroupPool, photoGroupPool);
    }

    public static ArrayList<Group> loadAudioGroups() {
        return loadAudioGroups(null);
    }

    public static ArrayList<Group> loadPhotoGroups() {
        return loadPhotoGroups(null);
    }

    public static ArrayList<Group> loadAudioGroups(File file) {
        ArrayList<Group> groups;

        if (file == null) {
            File defaultFile = new File(
                    VkFileLoader.class.getClassLoader().getResource("audioGroups").getFile()
            );
            groups = readAudioGroupsFile(defaultFile);
        } else
            groups = readAudioGroupsFile(file);

        return groups;
    }

    public static ArrayList<Group> loadPhotoGroups(File file) {
        ArrayList<Group> groups;

        if (file == null) {
            File defaultFile = new File(
                    VkFileLoader.class.getClassLoader().getResource("photoGroups").getFile()
            );
            groups = readPhotoGroupsFile(defaultFile);
        } else
            groups = readPhotoGroupsFile(file);

        return groups;
    }

    private static ArrayList<Group> readAudioGroupsFile(File file) {
        ArrayList<Group> groups = new ArrayList<>();
        List<String> allLinesOfFile = readFile(file);

        for(String groupId : allLinesOfFile)
            groups.add(new AudioGroup(Integer.parseInt(groupId)));

        return groups;
    }

    private static ArrayList<Group> readPhotoGroupsFile(File file) {
        ArrayList<Group> groups = new ArrayList<>();
        List<String> allLinesOfFile = readFile(file);

        for(String groupId : allLinesOfFile)
            groups.add(new PhotoGroup(Integer.parseInt(groupId)));

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
