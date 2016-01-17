package edu.kit.ipd.crowdcontrol.objectservice.api;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.utils.MimeParse;

import java.util.ArrayList;
import java.util.List;

/**
 * Transforms protocol buffers into JSON / protocol buffer responses.
 *
 * @author Niklas Keller
 */
public class OutputTransformer implements Route {
	private static final String TYPE_JSON = "application/json";
	private static final String TYPE_PROTOBUF = "application/protobuf";

	private static final List<String> SUPPORTED_TYPES;

	static {
		SUPPORTED_TYPES = new ArrayList<>();
		SUPPORTED_TYPES.add("application/protobuf");
		SUPPORTED_TYPES.add("application/json"); // Last to be default.
	}

	private Route route;

	public OutputTransformer(Route route) {
		this.route = route;
	}

	@Override
	public String handle(Request request, Response response) throws Exception {
		Object result = this.route.handle(request, response);

		if (result == null) {
			response.status(204);
			return "";
		}

		if (!(result instanceof Message)) {
			throw new InternalServerErrorException("Route did not respond with a protocol buffer.");
		}

		return this.transform(request, response, (Message) result);
	}

	private String transform(Request request, Response response, Message message) {
		String bestMatch = MimeParse.bestMatch(SUPPORTED_TYPES, request.headers("accept"));

		try {
			switch (bestMatch) {
				case TYPE_JSON:
					response.type(TYPE_JSON);
					return JsonFormat.printer().print(message);
				case TYPE_PROTOBUF:
					response.type(TYPE_PROTOBUF);
					return new String(message.toByteArray());
				default:
					throw new BadRequestException("Could not transform into requested mime type: '%s'.", bestMatch);
			}
		} catch (InvalidProtocolBufferException e) {
			throw new InternalServerErrorException("Attempt to transform an invalid protocol buffer into JSON.");
		}
	}
}