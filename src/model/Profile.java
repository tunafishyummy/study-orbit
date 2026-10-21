package model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Profile {

    private final String id;
    private String name;
    private String avatarColor;
    private final LocalDateTime created;

    public Profile(String name, String avatarColor) {
        this(UUID.randomUUID().toString(), name, avatarColor, LocalDateTime.now());
    }

    public Profile(String id, String name, String avatarColor, LocalDateTime created) {
        this.id = id == null ? UUID.randomUUID().toString() : id;
        this.name = name == null ? "" : name;
        this.avatarColor = avatarColor == null ? "#6151D9" : avatarColor;
        this.created = created == null ? LocalDateTime.now() : created;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? "" : name;
    }

    public String getAvatarColor() {
        return avatarColor;
    }

    public void setAvatarColor(String avatarColor) {
        this.avatarColor = avatarColor == null ? "#6151D9" : avatarColor;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    @Override
    public String toString() {
        return name;
    }
}
