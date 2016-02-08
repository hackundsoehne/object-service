package edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.command;

import com.amazonaws.mturk.requester.doc._2014_08_15.CreateHITResponse;
import com.amazonaws.mturk.requester.doc._2014_08_15.HIT;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import edu.kit.ipd.crowdcontrol.objectservice.Main;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.MTurkConnection;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.MturkRestCommand;
import edu.kit.ipd.crowdcontrol.objectservice.crowdworking.mturk.Utils;
import sun.misc.IOUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Create a HIT
 * @version 1.0
 * @author MarcelHollerbach
 */
public class PublishHIT extends MturkRestCommand<String, CreateHITResponse> {
    /**
     * Tries to publish a hit with the given parameters
     * @param connection connection to use for creating the HIT
     * @param title The title which is displayed representing for the hit
     * @param description The description of the HIT
     * @param reward money a worker gets when he finished the hit
     * @param assignmentDurationInSeconds duration of the assignment before it times out
     * @param lifetimeInSeconds how long a hit is published before it is auto closed
     * @param keywords comma separated list of keywords which are used to find this hit
     * @param maxAssignments maximum number of assignments before this hit is closed
     * @param autoApprovalDelayInSeconds after how many seconds a assignment is approved
     * @param data is attached to the hit and can be accessed later
     * @param initCall the call is executed as js with the standard lib
     */
    public PublishHIT(MTurkConnection connection, Object title,
                      String description, double reward, int assignmentDurationInSeconds,
                      int lifetimeInSeconds, String keywords, int maxAssignments,
                      int autoApprovalDelayInSeconds, String data, String initCall) {
        super(connection,
                "CreateHIT","Minimal","2014-08-15",CreateHITResponse.class,
                () -> {
                    Map<String, Object> values = new HashMap<>();
                    List<String> jsFiles = new ArrayList<>();
                    jsFiles.add("/mturk/worker-ui/mturk.js");

                    values.put("Title", title);
                    values.put("Description", description);
                    values.put("Question",
                            "<HTMLQuestion xmlns=\"http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2011-11-11/HTMLQuestion.xsd\">\n" +
                                    "  <HTMLContent>" +
                                    "<![CDATA[\n" +
                                    "<!DOCTYPE html>\n" +


                                    "<html>\n" +
                                    " <head>\n" +
                                    "  <meta http-equiv='Content-Type' content='text/html; charset=UTF-8'/>\n" +
                                    "  <script type='text/javascript' src='https://s3.amazonaws.com/mturk-public/externalHIT_v1.js'></script>\n" +
                                    "  <script type='text/javascript' src='https://code.jquery.com/jquery-2.0.0.js'></script>" +
                                    " </head>\n" +
                                    " <body onload=\""+initCall+"\">\n" +
                                    "<script type='text/javascript' src='https://worker.pse.kelunik.com/ui/bundle.js'></script>" +
                                    loadFiles(jsFiles).stream().map(s ->  "<script type='text/javascript'>"+s+"</script>").collect(Collectors.joining())+
                                    "   <div id=\"ractive-container\"></div>" +
                                    "   <button onclick='sendFinish()'>Submit</button>" +
                                    " </body>\n" +
                                    "</html>\n" +
                                    "]]>\n" +
                                    "  </HTMLContent>\n" +
                                    "  <FrameHeight>450</FrameHeight>\n" +
                                    "</HTMLQuestion>");
                    values.put("Reward.Amount", reward);
                    values.put("Reward.CurrencyCode", "USD");
                    values.put("AssignmentDurationInSeconds", assignmentDurationInSeconds);
                    values.put("LifetimeInSeconds", lifetimeInSeconds);
                    values.put("Keywords", keywords);
                    values.put("MaxAssignments", maxAssignments);
                    values.put("AutoApprovalDelayInSeconds", autoApprovalDelayInSeconds);
                    values.put("RequesterAnnotation", data);
                    return values;
                },
                createHITResponse -> {
                    // check if we got a hit at all
                    if (createHITResponse.getHIT().size() == 0) {
                        throw new IllegalStateException("No result HIT!");
                    }
                    //get the created HIT
                    HIT created = createHITResponse.getHIT().get(0);

                    //check for errors
                    Utils.handleRequest(created.getRequest());

                    //return HITId
                    return created.getHITId();
                });
    }
    private static List<String> loadFiles(List<String> files) {
        return files.stream().map(s -> {
            try {
                return CharStreams
                        .toString(new InputStreamReader(
                                Main.class.getResourceAsStream(s)
                                , Charsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
                return "";
            }
        })
        .filter(s1 -> !s1.isEmpty())
                .collect(Collectors.toList());
    }
}
