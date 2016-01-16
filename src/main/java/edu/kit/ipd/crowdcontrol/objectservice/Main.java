package edu.kit.ipd.crowdcontrol.objectservice;

import edu.kit.ipd.crowdcontrol.objectservice.api.JsonOutputTransformer;
import edu.kit.ipd.crowdcontrol.objectservice.api.Router;

public class Main {
	public static void main(String[] args) {
		Router router = new Router(new JsonOutputTransformer(), "application/json");
		router.init();
	}
}
