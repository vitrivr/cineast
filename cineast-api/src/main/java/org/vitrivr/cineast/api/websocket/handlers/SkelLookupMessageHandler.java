package org.vitrivr.cineast.api.websocket.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.vitrivr.cineast.api.messages.pose.SkelLookup;
import org.vitrivr.cineast.api.messages.result.SkelLookupResult;
import org.vitrivr.cineast.api.websocket.handlers.abstracts.StatelessWebsocketMessageHandler;
import org.vitrivr.cineast.core.config.PoseConfig;
import org.vitrivr.cineast.core.data.pose.PoseKeypoints;
import org.vitrivr.cineast.core.pose.SkelProcessor;
import org.vitrivr.cineast.standalone.config.Config;

import java.util.Collections;
import java.util.List;

public class SkelLookupMessageHandler extends StatelessWebsocketMessageHandler<SkelLookup> {
    private static final Logger logger = LogManager.getLogger();

    /**
     * Invoked when a Message of type SkelLookup arrives and requires handling. Uses OpenPose to look up the
     * keypoints of the photo, wraps them in a SkelLookupResult object and writes them to the stream.
     *
     * @param session WebSocketSession for which the message arrived.
     * @param message Message of type a that needs to be handled.
     */
    @Override
    public void handle(Session session, SkelLookup message) {
        PoseConfig poseConfig = Config.sharedConfig().getPose();
        if (poseConfig == null || poseConfig.getModelPath() == null) {
            logger.error("Openpose not run since required value missing from config: pose / modelPath");
            this.write(session, new SkelLookupResult("",
                Collections.emptyList()));
            return;
        }
        SkelProcessor skelProcessor = SkelProcessor.getInstance(poseConfig);
        float[][][] poses = skelProcessor.getPose(message.getImg());
        List<PoseKeypoints> resultList;
        if (poses != null && poses.length >= 1) {
            resultList = Collections.singletonList(new PoseKeypoints(poses[0]));
        } else {
            resultList = Collections.emptyList();
        }
        this.write(session, new SkelLookupResult("", resultList));
    }
}
