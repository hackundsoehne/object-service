
var ratingAnswers = new Array();
var creativeAnswers = new Array();

function sendFinish() {
    var ratingsText;
    var creativeAnswersText;

    if (ratingAnswers.length !== 0) {
        ratingsText = "You gave the following ratings ";
        for (var rating in ratingAnswers) {
            ratingsText += "\n - Rating "+ratingAnswers[rating].rating+" with feedback "+ratingAnswers[rating].feedback;
        }
    }

    if (creativeAnswers.length !== 0) {
        creativeAnswersText = "\n and the following creative answers ";
        for (var creativeAnswer in creativeAnswers) {
            creativeAnswersText += "\n - "+creativeAnswers[creativeAnswer];
        }
    }

    $('body')
        .append("<form id='submitform' method=post>"
                +"<input type='hidden' value='"+turkGetParam("assignmentId","")+"' name='assignmentId' id='assignmentId'/>"
                +"<textarea name='comment' cols='80' rows='3'>"
                + ratingsText
                +  creativeAnswersText
                +"</textarea></p>"
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
    WorkerUI.init(properties = {
        workerServiceURL: workerServiceUrl,
        platform: platformName,
        osParams: {
                mTurkWorkerId: turkGetParam("workerId", -1),
        },
        experiment: experiment,
        preview: preview
    });

    WorkerUI.onSubmitAnswer(function (viewData, submittedData) {
        console.log("Save answer "+submittedData.answer);
        creativeAnswers.push(submittedData.answer)
    });
    WorkerUI.onSubmitRating(function (viewData, submittedData) {
        for (var rating in submittedData) {
            console.log("Save Rating "+rating.rating+" - "+rating.answer);
            ratingAnswers.push(rating)
        }
    });
    WorkerUI.onFinished(function (data) {
        sendFinish();
    });
    //load initial bits
    WorkerUI.load();
}