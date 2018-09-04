package cd.go.plugin.notification.rocketchat.rocket;

import java.util.Objects;

public class RocketChatSettings {
    private final String serverUrl;
    private final String user;
    private final String password;

    public RocketChatSettings(String serverUrl, String user, String password) {
        this.serverUrl = serverUrl;
        this.user = user;
        this.password = password;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;
        if (getClass() != o.getClass())
            return false;
        RocketChatSettings other = (RocketChatSettings) o;
        return Objects.equals(serverUrl, other.serverUrl)
                && Objects.equals(user, other.user)
                && Objects.equals(password, other.password);
    }

}
