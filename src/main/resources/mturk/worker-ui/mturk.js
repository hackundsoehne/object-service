function sendFinish() {
    $('body')
        .append("<form id='submitform' method=post>"
                +"<input type='hidden' value='"+turkGetParam("assignmentId","")+"' name='assignmentId' id='assignmentId'/>"
                +"<textarea name='comment' cols='80' rows='3'>A Placeholder answer for mturk</textarea></p>"
                +"</form>");
    $('#submitform')
                .attr("action","https://workersandbox.mturk.com/mturk/externalSubmit")
                .submit();

}

function initMturk(platformName, workerServiceUrl, experiment) {
    var preview = false;

    var assignmentID = turkGetParam("assignmentId","ASSIGNMENT_ID_NOT_AVAILABLE");

    //check if this is a preview
    if (assignmentID == "ASSIGNMENT_ID_NOT_AVAILABLE") {
      preview = true
    }

    //init lib for workerService
    CreativeCrowd.init(properties = {
        workerServiceURL: workerServiceUrl,
        platform: platformName,
        osParams: {
                mTurkWorkerId: turkGetParam("workerId", -1),
        },
        experiment: experiment,
        preview: preview
    });
    //load initial bits
    CreativeCrowd.load();
    CreativeCrowd.onSubmitAnswer(function (viewData, submittedData) {
        //TODO log this
    });
    CreativeCrowd.onSubmitCalibration(function (data) {
        //TODO log this
    });
    CreativeCrowd.onFinished(function (data) {
        sendFinish();
    });
}