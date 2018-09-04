package cd.go.plugin.notification.rocketchat.rocket;
import cd.go.plugin.notification.rocketchat.PluginRequest;
import cd.go.plugin.notification.rocketchat.PluginSettings;
import cd.go.plugin.notification.rocketchat.ServerRequestFailedException;
import com.github.baloise.rocketchatrestclient.model.Message;
import com.github.baloise.rocketchatrestclient.model.Room;
import com.thoughtworks.go.plugin.api.logging.Logger;
import static java.text.MessageFormat.format;

import com.github.baloise.rocketchatrestclient.RocketChatClient;

import java.io.IOException;

public class RocketChatService {
    private static final Logger LOG = Logger.getLoggerFor(RocketChatService.class);

    private RocketChatClient rc;
    private RocketChatSettings settings;

    public void configure(RocketChatSettings settings) {
        if(this.settings == null || !this.settings.equals(settings)) {
            this.settings = settings;
            this.rc = new RocketChatClient(settings.getServerUrl(), settings.getUser(), settings.getPassword());
            LOG.info("Initialized new rocket chat client");
        }
    }

    public void configure(PluginRequest pluginRequest) throws ServerRequestFailedException {
        // If you need access to settings like API keys, URLs, then call PluginRequest#getPluginSettings
        PluginSettings pluginSettings = pluginRequest.getPluginSettings();
        if(pluginSettings != null) {
            RocketChatSettings settings = new RocketChatSettings(
                    pluginSettings.getApiUrl(),
                    pluginSettings.getApiUser(),
                    pluginSettings.getApiKey(),
                    pluginSettings.getRoom());
            this.configure(settings);
        }
        else {
            LOG.warn("Rocket chat plugin is not configured");
        }
    }

    public void postMessage(Message msg) throws IOException {
        Room room = new Room(this.settings.getRoom(), false);
        this.rc.getChatApi().postMessage(room, msg);
    }
}
