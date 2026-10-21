package data;

import model.Profile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProfileManager {

    private final DataStore store;
    private final SettingsManager settings;
    private final List<Profile> profiles = new ArrayList<Profile>();

    public ProfileManager(DataStore store, SettingsManager settings) throws IOException {
        this.store = store;
        this.settings = settings;
        profiles.addAll(store.loadProfiles());
    }

    public List<Profile> getProfiles() {
        return new ArrayList<Profile>(profiles);
    }

    public Profile getActiveProfile() {
        String activeId = settings.get(SettingsManager.ACTIVE_PROFILE, "");
        for (int index = 0; index < profiles.size(); index++) {
            if (profiles.get(index).getId().equals(activeId)) {
                return profiles.get(index);
            }
        }
        return null;
    }

    public void setActiveProfile(Profile profile) throws IOException {
        if (profile == null) {
            settings.set(SettingsManager.ACTIVE_PROFILE, "");
            return;
        }
        settings.set(SettingsManager.ACTIVE_PROFILE, profile.getId());
    }

    public Profile addProfile(String name, String avatarColor) throws IOException {
        Profile profile = new Profile(name, avatarColor);
        profiles.add(profile);
        store.saveProfiles(profiles);
        return profile;
    }

    public void updateProfile(Profile profile, String name, String avatarColor) throws IOException {
        profile.setName(name);
        profile.setAvatarColor(avatarColor);
        store.saveProfiles(profiles);
    }

    public void deleteProfile(Profile profile) throws IOException {
        profiles.remove(profile);
        store.saveProfiles(profiles);
        Profile active = getActiveProfile();
        if (active == null) {
            settings.set(SettingsManager.ACTIVE_PROFILE, "");
        }
        if (profiles.isEmpty()) {
            settings.setBoolean(SettingsManager.FIRST_RUN_DONE, false);
        }
    }

    public boolean isFirstRun() {
        return settings.getBoolean(SettingsManager.FIRST_RUN_DONE, false) == false;
    }

    public void finishFirstRun() throws IOException {
        settings.setBoolean(SettingsManager.FIRST_RUN_DONE, true);
    }
}
