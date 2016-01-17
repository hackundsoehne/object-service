package edu.kit.ipd.crowdcontrol.objectservice.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.protobuf.Message;
import edu.kit.ipd.crowdcontrol.objectservice.proto.Template;
import spark.Route;
import spark.Spark;
import spark.servlet.SparkApplication;

import static spark.Spark.before;
import static spark.Spark.exception;

public class Router implements SparkApplication {
	private Gson gson;
	private TemplateHandler templateHandler;

	public Router() {
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

		before((request, response) -> {
			if (request.headers("accept") == null) {
				throw new BadRequestException("Missing required 'accept' header.");
			}
		});

		put("/templates", templateHandler::put, Template.class);
		get("/templates", templateHandler::getAll);
		get("/templates/:id", templateHandler::get);
		patch("/templates/:id", templateHandler::patch, Template.class);
		delete("/templates/:id", templateHandler::delete);
	}

	private void get(String uri, Route route) {
		Spark.get(uri, new OutputTransformer(route));
	}

	private void put(String uri, Route route, Class<? extends Message> type) {
		Spark.put(uri, new InputTransformer(new OutputTransformer(route), type));
	}

	private void patch(String uri, Route route, Class<? extends Message> type) {
		Spark.patch(uri, new InputTransformer(new OutputTransformer(route), type));
	}

	private void delete(String uri, Route route) {
		Spark.delete(uri, new OutputTransformer(route));
	}
}