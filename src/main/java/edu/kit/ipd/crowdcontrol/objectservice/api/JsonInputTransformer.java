package edu.kit.ipd.crowdcontrol.objectservice.api;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import spark.Request;
import spark.Response;
import spark.Route;

import java.lang.reflect.Method;

public class JsonInputTransformer implements Route {
	private Route next;
	private Class<? extends Message> type;
	private JsonFormat.Parser parser;

	public JsonInputTransformer(Route next, Class<? extends Message> type) {
		this(next, type, JsonFormat.parser());
	}

	public JsonInputTransformer(Route next, Class<? extends Message> type, JsonFormat.Parser parser) {
		this.next = next;
		this.type = type;
		this.parser = parser;
	}

	public Object handle(Request request, Response response) throws Exception {
		if (!request.contentType().equals("application/json")) {
			throw new BadRequestException("Content-type must be application/json.");
		}

		String body = request.body();

		Method method = this.type.getMethod("newBuilder");
		Message.Builder builder = (Message.Builder) method.invoke(null);

		try {
			parser.merge(body, builder);
		} catch (InvalidProtocolBufferException e) {
			throw new BadRequestException("Invalid protocol buffer.");
		}

		request.attribute("input", builder.build());
		return next.handle(request, response);
	}
}
