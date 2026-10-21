package ui.components;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class NativeFileDialog {

    public File open(String title, String description, String[] extensions) {
        String os = System.getProperty("os.name").toLowerCase();
        try {
            if (os.contains("win")) {
                return openWindows(title, description, extensions);
            }
            if (os.contains("linux")) {
                return openLinux(title, description, extensions);
            }
            if (os.contains("mac")) {
                return openMac(title);
            }
        } catch (Exception exception) {
            System.out.println("Native dialog failed, using the awt dialog: " + exception.getMessage());
        }
        return fallbackOpen(title);
    }

    public File save(String title, String description, String[] extensions, String defaultName) {
        String os = System.getProperty("os.name").toLowerCase();
        try {
            if (os.contains("win")) {
                return saveWindows(title, description, extensions, defaultName);
            }
            if (os.contains("linux")) {
                return saveLinux(title, description, extensions, defaultName);
            }
            if (os.contains("mac")) {
                return saveMac(title, defaultName);
            }
        } catch (Exception exception) {
            System.out.println("Native dialog failed, using the awt dialog: " + exception.getMessage());
        }
        return fallbackSave(title, defaultName);
    }

    private File openWindows(String title, String description, String[] extensions) throws Exception {
        String script = "[Console]::OutputEncoding = [System.Text.Encoding]::UTF8;"
                + "Add-Type -AssemblyName System.Windows.Forms;"
                + "$dialog = New-Object System.Windows.Forms.OpenFileDialog;"
                + "$dialog.Title = '" + escape(title) + "';"
                + "$dialog.Filter = '" + escape(windowsFilter(description, extensions)) + "';"
                + "$dialog.RestoreDirectory = $true;"
                + "if ($dialog.ShowDialog() -eq [System.Windows.Forms.DialogResult]::OK) { [Console]::Out.Write($dialog.FileName) }";
        return readPath(run(new String[] {
                "powershell", "-NoProfile", "-STA", "-WindowStyle", "Hidden", "-Command", script
        }));
    }

    private File saveWindows(String title, String description, String[] extensions, String defaultName) throws Exception {
        String script = "[Console]::OutputEncoding = [System.Text.Encoding]::UTF8;"
                + "Add-Type -AssemblyName System.Windows.Forms;"
                + "$dialog = New-Object System.Windows.Forms.SaveFileDialog;"
                + "$dialog.Title = '" + escape(title) + "';"
                + "$dialog.Filter = '" + escape(windowsFilter(description, extensions)) + "';"
                + "$dialog.FileName = '" + escape(defaultName) + "';"
                + "$dialog.RestoreDirectory = $true;"
                + "if ($dialog.ShowDialog() -eq [System.Windows.Forms.DialogResult]::OK) { [Console]::Out.Write($dialog.FileName) }";
        return readPath(run(new String[] {
                "powershell", "-NoProfile", "-STA", "-WindowStyle", "Hidden", "-Command", script
        }));
    }

    private File openLinux(String title, String description, String[] extensions) throws Exception {
        if (commandExists("zenity")) {
            List<String> command = new ArrayList<String>();
            command.add("zenity");
            command.add("--file-selection");
            command.add("--title=" + title);
            command.add("--file-filter=" + description + " | " + globList(extensions));
            command.add("--file-filter=All files | *");
            return readPath(run(command.toArray(new String[0])));
        }
        if (commandExists("kdialog")) {
            List<String> command = new ArrayList<String>();
            command.add("kdialog");
            command.add("--title");
            command.add(title);
            command.add("--getopenfilename");
            command.add(System.getProperty("user.home"));
            command.add(kdialogFilter(description, extensions) + "\nAll files (*)");
            return readPath(run(command.toArray(new String[0])));
        }
        throw new UnsupportedOperationException("zenity or kdialog is not installed");
    }

    private File saveLinux(String title, String description, String[] extensions, String defaultName) throws Exception {
        if (commandExists("zenity")) {
            List<String> command = new ArrayList<String>();
            command.add("zenity");
            command.add("--file-selection");
            command.add("--save");
            command.add("--confirm-overwrite");
            command.add("--title=" + title);
            command.add("--filename=" + defaultName);
            command.add("--file-filter=" + description + " | " + globList(extensions));
            return readPath(run(command.toArray(new String[0])));
        }
        if (commandExists("kdialog")) {
            List<String> command = new ArrayList<String>();
            command.add("kdialog");
            command.add("--title");
            command.add(title);
            command.add("--getsavefilename");
            command.add(System.getProperty("user.home") + File.separator + defaultName);
            command.add(kdialogFilter(description, extensions));
            return readPath(run(command.toArray(new String[0])));
        }
        throw new UnsupportedOperationException("zenity or kdialog is not installed");
    }

    private File openMac(String title) throws Exception {
        String script = "POSIX path of (choose file with prompt \"" + escapeApple(title) + "\")";
        return readPath(run(new String[] { "osascript", "-e", script }));
    }

    private File saveMac(String title, String defaultName) throws Exception {
        String script = "POSIX path of (choose file name with prompt \"" + escapeApple(title)
                + "\" default name \"" + escapeApple(defaultName) + "\")";
        return readPath(run(new String[] { "osascript", "-e", script }));
    }

    private String run(String[] command) throws Exception {
        ProcessBuilder builder = new ProcessBuilder(command);
        Process process = builder.start();

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder output = new StringBuilder();
        String line = reader.readLine();
        while (line != null) {
            output.append(line);
            line = reader.readLine();
        }
        reader.close();

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            return null;
        }
        return output.toString().trim();
    }

    private File readPath(String output) {
        if (output == null || output.isEmpty()) {
            return null;
        }
        return new File(output);
    }

    private boolean commandExists(String name) {
        try {
            Process process = new ProcessBuilder("which", name).start();
            return process.waitFor() == 0;
        } catch (Exception exception) {
            return false;
        }
    }

    private String windowsFilter(String description, String[] extensions) {
        StringBuilder pattern = new StringBuilder();
        for (int index = 0; index < extensions.length; index++) {
            if (index > 0) {
                pattern.append(";");
            }
            pattern.append("*.").append(extensions[index]);
        }
        return description + "|" + pattern + "|All files (*.*)|*.*";
    }

    private String globList(String[] extensions) {
        StringBuilder globs = new StringBuilder();
        for (int index = 0; index < extensions.length; index++) {
            if (index > 0) {
                globs.append(" ");
            }
            globs.append("*.").append(extensions[index]);
        }
        return globs.toString();
    }

    private String kdialogFilter(String description, String[] extensions) {
        return description + " (" + globList(extensions) + ")";
    }

    private String escape(String value) {
        return value == null ? "" : value.replace("'", "''");
    }

    private String escapeApple(String value) {
        return value == null ? "" : value.replace("\"", "\\\"");
    }

    private File fallbackOpen(String title) {
        FileDialog fileDialog = new FileDialog((Frame) null, title, FileDialog.LOAD);
        fileDialog.setVisible(true);
        String fileName = fileDialog.getFile();
        if (fileName == null) {
            return null;
        }
        return new File(fileDialog.getDirectory(), fileName);
    }

    private File fallbackSave(String title, String defaultName) {
        FileDialog fileDialog = new FileDialog((Frame) null, title, FileDialog.SAVE);
        fileDialog.setFile(defaultName);
        fileDialog.setVisible(true);
        String fileName = fileDialog.getFile();
        if (fileName == null) {
            return null;
        }
        return new File(fileDialog.getDirectory(), fileName);
    }
}
