package edu.kit.ipd.crowdcontrol.objectservice.api;

import edu.kit.ipd.crowdcontrol.objectservice.proto.Template;
import edu.kit.ipd.crowdcontrol.objectservice.proto.TemplateList;
import spark.Request;
import spark.Response;

public class TemplateHandler {
	public TemplateList getAll(Request request, Response response) {
		return TemplateList.newBuilder()
				.addItems(Template.newBuilder().setId(1).setContent("{{TEST}}").build())
				.addItems(Template.newBuilder().setId(2).setContent("{{TEST}}").build())
				.addItems(Template.newBuilder().setId(3).setContent("{{TEST}}").build())
				.build();
	}

	public Template get(Request request, Response response) {
		int id;

		try {
			id = Integer.parseInt(request.params(":id"));
		} catch (NumberFormatException e) {
			throw new BadRequestException(":id must be a valid integer.");
		}

		return Template.newBuilder().setId(id).setContent("{{TEST}}").build();
	}
}
