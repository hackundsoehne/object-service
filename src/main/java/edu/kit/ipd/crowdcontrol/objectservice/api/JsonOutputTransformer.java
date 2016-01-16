package edu.kit.ipd.crowdcontrol.objectservice.api;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import spark.ResponseTransformer;

/**
 * Transforms protocol buffers into JSON responses.
 *
 * @author Niklas Keller
 */
public class JsonOutputTransformer implements ResponseTransformer {
	private JsonFormat.Printer printer;

	/**
	 * Create an instance with the default printer.
	 */
	public JsonOutputTransformer() {
		this(JsonFormat.printer());
	}

	/**
	 * Create an instance with a custom printer.
	 *
	 * @param printer
	 * 		JSON format printer.
	 */
	public JsonOutputTransformer(JsonFormat.Printer printer) {
		this.printer = printer;
	}

	@Override
	public String render(Object object) {
		if (!(object instanceof MessageOrBuilder)) {
			throw new InternalServerErrorException("Attempt to transform something different than a protocol buffer into JSON.");
		}

		MessageOrBuilder messageOrBuilder = (MessageOrBuilder) object;

		try {
			return this.printer.print(messageOrBuilder);
		} catch (InvalidProtocolBufferException e) {
			throw new InternalServerErrorException("Attempt to transform an invalid protocol buffer into JSON.");
		}
	}
}
