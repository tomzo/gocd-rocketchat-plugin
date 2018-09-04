/*
 * Copyright 2018 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cd.go.plugin.notification.rocketchat.executors;

import cd.go.plugin.notification.rocketchat.PluginRequest;
import cd.go.plugin.notification.rocketchat.RequestExecutor;
import cd.go.plugin.notification.rocketchat.requests.StageStatusRequest;
import cd.go.plugin.notification.rocketchat.rocket.MessageBuilderService;
import cd.go.plugin.notification.rocketchat.rocket.RocketChatService;
import com.github.baloise.rocketchatrestclient.model.Message;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.Arrays;
import java.util.HashMap;

import static java.text.MessageFormat.format;

public class StageStatusRequestExecutor implements RequestExecutor {
    private static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

    private final StageStatusRequest request;
    private RocketChatService chat;
    private MessageBuilderService messageBuilderService;
    private final PluginRequest pluginRequest;

    public StageStatusRequestExecutor(StageStatusRequest request, RocketChatService chat, MessageBuilderService messageBuilderService, PluginRequest pluginRequest) {
        this.request = request;
        this.chat = chat;
        this.messageBuilderService = messageBuilderService;
        this.pluginRequest = pluginRequest;
    }

    @Override
    public GoPluginApiResponse execute() throws Exception {
        HashMap<String, Object> responseJson = new HashMap<>();
        try {
            sendNotification();
            responseJson.put("status", "success");
        } catch (Exception e) {
            responseJson.put("status", "failure");
            responseJson.put("messages", Arrays.asList(e.getMessage()));
        }
        return new DefaultGoPluginApiResponse(200, GSON.toJson(responseJson));
    }

    protected void sendNotification() throws Exception {
        Message msg = messageBuilderService.onStageStatusChanged(pluginRequest, request.pipeline);
        if(msg != null) {
            chat.postMessage(msg);
        }
    }
}
