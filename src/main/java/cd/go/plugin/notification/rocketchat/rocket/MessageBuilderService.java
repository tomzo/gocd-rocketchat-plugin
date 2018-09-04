package cd.go.plugin.notification.rocketchat.rocket;

import cd.go.plugin.notification.rocketchat.PluginRequest;
import cd.go.plugin.notification.rocketchat.ServerRequestFailedException;
import cd.go.plugin.notification.rocketchat.requests.StageStatusRequest;
import cd.go.plugin.notification.rocketchat.requests.StageStatusRequest.Job;
import com.github.baloise.rocketchatrestclient.model.Attachment;
import com.github.baloise.rocketchatrestclient.model.AttachmentField;
import com.github.baloise.rocketchatrestclient.model.Message;
import com.thoughtworks.go.plugin.api.logging.Logger;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import static java.text.MessageFormat.format;

public class MessageBuilderService {
    private static final Logger LOG = Logger.getLoggerFor(MessageBuilderService.class);

    private static final String STAGE_STATE_FAILED = "Failed";
    private static final String STAGE_STATE_PASSED = "Passed";
    private static final String STAGE_STATE_CANCELLED = "Cancelled";
    private static final String STAGE_STATE_BUILDING = "Building";

    public Message onStageStatusChanged(PluginRequest pluginRequest, StageStatusRequest.Pipeline pipeline) {
        // The request.pipeline object has all the details about the pipeline, materials, stages and jobs
        if(pipeline.stage.state.equals(STAGE_STATE_FAILED)) {
            return onStageFailed(pipeline, pluginRequest);
        }
        else if(pipeline.stage.state.equals(STAGE_STATE_PASSED)) {
            return onStagePassed(pipeline);
        }
        else if(pipeline.stage.state.equals(STAGE_STATE_BUILDING)) {
            return onStageBuilding(pipeline);
        }
        else if(pipeline.stage.state.equals(STAGE_STATE_CANCELLED)) {
            return onStageCancelled(pipeline);
        }
        LOG.warn(format("Skipping message processing for stage {0}/{1} because stage state is unknown - {2}", pipeline.name, pipeline.stage.name, pipeline.stage.state));
        return null;
    }

    private Message onStageBuilding(StageStatusRequest.Pipeline pipeline) {
        return null;
    }

    private Message onStageCancelled(StageStatusRequest.Pipeline pipeline) {
        return null;
    }

    private Message onStagePassed(StageStatusRequest.Pipeline pipeline) {
        return null;
    }

    public String stageFullUrl(StageStatusRequest.Pipeline pipeline, PluginRequest pluginRequest) {
        try {
            String host = pluginRequest.getPluginSettings().getGoServerUrl();
            return new URI(String.format("%s/go/pipelines/%s/%s/%s/%s", host, pipeline.name, pipeline.counter, pipeline.stage.name, pipeline.stage.counter)).normalize().toASCIIString();
        }
        catch(Exception ex) {
            LOG.error("Failed to form an URL to stage", ex);
            return "";
        }
    }

    public String vsmFullUrl(StageStatusRequest.Pipeline pipeline, PluginRequest pluginRequest) {
        try {
            String host = pluginRequest.getPluginSettings().getGoServerUrl();
            return new URI(String.format("%s/go/pipelines/value_stream_map/%s/%s", host, pipeline.name, pipeline.counter)).normalize().toASCIIString();
        }
        catch(Exception ex) {
            LOG.error("Failed to form an URL to VSM", ex);
            return "";
        }
    }

    private String jobConsoleFullUrl(StageStatusRequest.Pipeline pipeline, PluginRequest pluginRequest, Job job) {
        try {
            String host = pluginRequest.getPluginSettings().getGoServerUrl();
            URI link = new URI(String.format("%s/go/tab/build/detail/%s/%s/%s/%s/%s#tab-console", host, pipeline.name, pipeline.counter, pipeline.stage.name, pipeline.stage.counter, job.name));
            return link.normalize().toASCIIString();
        }
        catch(Exception ex) {
            LOG.error("Failed to form an URL to job console", ex);
            return "";
        }
    }

    public String stageRelativeUri(StageStatusRequest.Pipeline pipeline) {
        return pipeline.name + "/" + pipeline.counter + "/" + pipeline.stage.name + "/" + pipeline.stage.counter;
    }

    private Message onStageFailed(StageStatusRequest.Pipeline pipeline, PluginRequest pluginRequest) {
        String topText = getTopMessage(pipeline, pluginRequest);
        Message message = new Message(topText);
        Attachment buildAttachment = new Attachment();
        AttachmentField labelField =  new AttachmentField();
        labelField.setShort(true);
        labelField.setTitle("Label");
        labelField.setValue(format("[{0}]({1})", pipeline.label, vsmFullUrl(pipeline, pluginRequest)));
        // Failed jobs:
        AttachmentField jobs = new AttachmentField();
        jobs.setTitle("Failed Jobs");
        String failedJobsText = getFailedJobsText(pipeline, pluginRequest);
        jobs.setValue(failedJobsText);

        buildAttachment.setFields(new AttachmentField[] { labelField, jobs });
        message.addAttachment(buildAttachment);

        return  message;
    }

    public String getFailedJobsText(StageStatusRequest.Pipeline pipeline, PluginRequest pluginRequest) {
        List<Job> failedJobs = pipeline.stage.jobs.stream()
                .filter(j -> "Failed".equals(j.result) || "Cancelled".equals(j.result))
                .collect(Collectors.toList());
        StringBuilder failedJobsText = new StringBuilder();
        for(int i = 0; i < failedJobs.size(); i++) {
            failedJobsText.append(" - [");
            Job j = failedJobs.get(i);
            failedJobsText.append(j.name);
            failedJobsText.append("](");
            failedJobsText.append(jobConsoleFullUrl(pipeline, pluginRequest, j));
            failedJobsText.append(") ");
            if(j.result.equals("Cancelled"))
                failedJobsText.append("was cancelled");
            if(j.result.equals("Failed"))
                failedJobsText.append("failed");
            if(i < failedJobs.size() -1) {
                failedJobsText.append('\n');
            }
        }
        return failedJobsText.toString();
    }


    public String getTopMessage(StageStatusRequest.Pipeline pipeline, PluginRequest pluginRequest) {
        return String.format("Stage [%s](%s) has failed", stageRelativeUri(pipeline), stageFullUrl(pipeline, pluginRequest));
    }
}
