package cd.go.plugin.notification.rocketchat.rocket;

import cd.go.plugin.notification.rocketchat.PluginRequest;
import cd.go.plugin.notification.rocketchat.PluginSettings;
import cd.go.plugin.notification.rocketchat.ServerRequestFailedException;
import cd.go.plugin.notification.rocketchat.requests.StageStatusRequest;
import com.github.baloise.rocketchatrestclient.model.Attachment;
import com.github.baloise.rocketchatrestclient.model.AttachmentField;
import com.github.baloise.rocketchatrestclient.model.Message;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MessageBuilderServiceTest {

    private PluginRequest pluginRequest;
    StageStatusRequest.Pipeline pipeline;
    private MessageBuilderService service;

    @Before
    public void setUp() throws ServerRequestFailedException {
        pluginRequest = mock(PluginRequest.class);
        PluginSettings settings = new PluginSettings();
        settings.setGoServerUrl("http://go.ai-traders.com");
        when(pluginRequest.getPluginSettings()).thenReturn(settings);

        pipeline = new StageStatusRequest.Pipeline();
        pipeline.name = "pipe";
        pipeline.counter = "3";
        pipeline.label = "abc";
        pipeline.stage = new StageStatusRequest.Stage();
        pipeline.stage.name = "stage";
        pipeline.stage.counter = "1";
        pipeline.stage.state = "Failed";
        pipeline.stage.jobs = new ArrayList<StageStatusRequest.Job>();
        StageStatusRequest.Job j1 = new StageStatusRequest.Job();
        j1.result = "Failed";
        j1.name = "job1";
        pipeline.stage.jobs.add(j1);
        StageStatusRequest.Job j2 = new StageStatusRequest.Job();
        j2.result = "Cancelled";
        j2.name = "job2";
        pipeline.stage.jobs.add(j2);

        service = new MessageBuilderService();
    }

    @Test
    public void shouldBuildShortStageUri() {
        String uri = service.stageRelativeUri(pipeline);
        assertThat(uri, is("pipe/3/stage/1"));
    }

    @Test
    public void shouldBuildFullStageUri() {
        String uri = service.stageFullUrl(pipeline, pluginRequest);
        assertThat(uri, is("http://go.ai-traders.com/go/pipelines/pipe/3/stage/1"));
    }

    @Test
    public void shouldBuildTopMessageWithLinkToStage() {
        String message = service.getTopMessage(pipeline, pluginRequest);
        assertThat(message, is("Stage [pipe/3/stage/1](http://go.ai-traders.com/go/pipelines/pipe/3/stage/1) has failed"));
    }

    @Test
    public void shouldAttachBuildDetailsToFinalMessage() {
        Message message = service.onStageStatusChanged(pluginRequest, pipeline);
        Attachment first = message.getAttachments()[0];
        AttachmentField label = first.getFields()[0];
        assertThat(label.getTitle(), is("Label"));
        assertThat(label.getValue(), is("[abc](http://go.ai-traders.com/go/pipelines/value_stream_map/pipe/3)"));
    }

    @Test
    public void shouldBuildFailedJobsText() {
        String text = service.getFailedJobsText(pipeline, pluginRequest);
        assertThat(text, is(" - [job1](http://go.ai-traders.com/go/tab/build/detail/pipe/3/stage/1/job1#tab-console) failed\n" +
        " - [job2](http://go.ai-traders.com/go/tab/build/detail/pipe/3/stage/1/job2#tab-console) was cancelled"));
    }
}
