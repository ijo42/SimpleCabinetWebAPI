package pro.gravit.simplecabinet.web.dto;

import pro.gravit.simplecabinet.web.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UserDto {
    public final long id;
    public final String username;
    public final UUID uuid;
    public final User.Gender gender;
    public final String status;
    public final LocalDateTime registrationDate;
    public final List<UserGroupDto> groups;
    public final UserTexture skin;
    public final UserTexture cloak;

    public UserDto(long id, String username, UUID uuid, User.Gender gender, String status, LocalDateTime registrationDate, List<UserGroupDto> groups, UserTexture skin, UserTexture cloak) {
        this.id = id;
        this.username = username;
        this.uuid = uuid;
        this.gender = gender;
        this.status = status;
        this.registrationDate = registrationDate;
        this.groups = groups;
        this.skin = skin;
        this.cloak = cloak;
    }

    public static class UserTexture {
        public final String url;
        public final String digest;
        public final Map<String, String> metadata;

        public UserTexture(String url, String digest, Map<String, String> metadata) {
            this.url = url;
            this.digest = digest;
            this.metadata = metadata;
        }
    }
}
