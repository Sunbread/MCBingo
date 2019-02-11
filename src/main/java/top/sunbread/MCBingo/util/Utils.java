package top.sunbread.MCBingo.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import top.sunbread.MCBingo.exceptions.NoSuchTextException;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class Utils {

    public static String getText(String key, VariablePair... vars) {
        InputStream is = Utils.class.getResourceAsStream("/texts.yml");
        InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
        YamlConfiguration texts = YamlConfiguration.loadConfiguration(isr);
        String value = texts.getString(key);
        if (value == null) throw new NoSuchTextException();
        String text = ChatColor.translateAlternateColorCodes('&', value);
        for (VariablePair pair : vars)
            text = text.replace("%" + pair.getName() + "%", pair.getValue());
        return text;
    }

    public static String getMaterialName(Material material) {
        String internalName = material.toString();
        String friendlyName = internalName;
        InputStream is = Utils.class.getResourceAsStream("/material-names.yml");
        InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
        YamlConfiguration materialNames = YamlConfiguration.loadConfiguration(isr);
        String value = materialNames.getString(internalName);
        if (value != null)
            friendlyName = value;
        return friendlyName;
    }

    public static boolean deleteFileOrDirectory(File file) {
        if (!file.exists()) return false;
        if (!file.isDirectory()) return file.delete();
        File[] list = file.listFiles();
        if (list == null) return false;
        boolean deletingSubFilesSuccess = true;
        for (File subFile : list)
            if (!deleteFileOrDirectory(subFile))
                deletingSubFilesSuccess = false;
        return deletingSubFilesSuccess && file.delete();
    }

    public static double clamp(double lowerBound, double number, double upperBound) {
        if (number > upperBound) return upperBound;
        if (number < lowerBound) return lowerBound;
        return number;
    }

}
