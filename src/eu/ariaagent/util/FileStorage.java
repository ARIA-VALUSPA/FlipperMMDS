package eu.ariaagent.util;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by adg on 04/08/2016.
 *
 */
public class FileStorage {

    private Pattern pattern = Pattern.compile("\\{[^\\}]+\\}");

    private static FileStorage instance;

    public static FileStorage getInstance() {
        if (instance == null) {
            instance = new FileStorage();
        }
        return instance;
    }

    private HashMap<String, FilePointer> map = new HashMap<>();

    private FileStorage() {
    }

    public FilePointer getFileFromValue(String value) {
        Matcher matcher = pattern.matcher(value);
        if (matcher.find()) {
            int start = matcher.start() + 1;
            int end = matcher.end() - 1;
            if (start < end) {
                String filePath = value.substring(start, end);
                return getFile(filePath, true);
            }
        }
        return getFile(value, false);
    }

    public FilePointer getFile(String path) {
        return getFile(path, true);
    }

    private FilePointer getFile(String value, boolean path) {
        FilePointer filePointer = map.get(value);
        if (filePointer == null) {
            if (path) {
                filePointer = new FilePointer(value);
            } else {
                filePointer = FilePointer.CreateFromSpeech(value);
            }
            map.put(filePointer.getFilePath(), filePointer);
        }
        return filePointer;
    }
}
