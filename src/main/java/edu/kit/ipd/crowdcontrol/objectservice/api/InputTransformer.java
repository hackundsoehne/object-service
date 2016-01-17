package edu.kit.ipd.crowdcontrol.objectservice.api;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import spark.Request;
import spark.Response;
import spark.Route;

import java.lang.reflect.Method;

public class InputTransformer implements Route {
	private Route next;
	private Class<? extends Message> type;

	public InputTransformer(Route next, Class<? extends Message> type) {
		this.next = next;
		this.type = type;
	}

	public Object handle(Request request, Response response) throws Exception {
		String body = request.body();
		String contentType = request.contentType();

		Method method = this.type.getMethod("newBuilder");
		Message.Builder builder = (Message.Builder) method.invoke(null);

		try {
			switch (contentType) {
				case "application/json":
					JsonFormat.parser().merge(body, builder);
					break;
				case "application/protobuf":
					// https://tools.ietf.org/html/draft-rfernando-protocol-buffers-00
					builder.mergeFrom(body.getBytes());
					break;
				default:
					throw new BadRequestException("Content-type must be '%s' or '%s'.", "application/json", "application/protobuf");
			}
		} catch (InvalidProtocolBufferException e) {
			throw new BadRequestException("Invalid protocol buffer.");
		}

		request.attribute("input", builder.build());
		return next.handle(request, response);
	}
}
