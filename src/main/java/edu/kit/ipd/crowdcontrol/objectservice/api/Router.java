package edu.kit.ipd.crowdcontrol.objectservice.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Template;
import spark.ResponseTransformer;
import spark.servlet.SparkApplication;

import static spark.Spark.*;

public class Router implements SparkApplication {
	private ResponseTransformer transformer;
	private String contentType;
	private Gson gson;

	private TemplateHandler templateHandler;

	/**
	 * Creates a new router instance.
	 *
	 * @param transformer
	 * 		Transforms protocol buffers into the desired output format.
	 * @param contentType
	 * 		Content type of the transformed response.
	 */
	public Router(ResponseTransformer transformer, String contentType) {
		this.transformer = transformer;
		this.contentType = contentType;

		this.gson = new GsonBuilder().setPrettyPrinting().create();
		this.templateHandler = new TemplateHandler();
	}

	@Override
	public void init() {
		exception(BadRequestException.class, (exception, request, response) -> {
			response.status(400);
			response.type("application/json");
			response.body(gson.toJson(new ErrorResponse("badRequest", exception.getMessage())));
		});

		exception(InternalServerErrorException.class, (exception, request, response) -> {
			response.status(500);
			response.type("application/json");
			response.body(gson.toJson(new ErrorResponse("internalServerError", exception.getMessage())));
		});

		get("/templates", templateHandler::getAll, this.transformer);
		get("/templates/:id", templateHandler::get, this.transformer);
		put("/templates", new JsonInputTransformer(templateHandler::put, Template.class), this.transformer);
		patch("/templates", new JsonInputTransformer(templateHandler::patch, Template.class), this.transformer);

		// Be sure to set right content type!
		after((request, response) -> response.type(this.contentType));

		// Replace automatic Jetty server header.
		after((request, response) -> response.header("server", "CrowdControl"));
	}
}