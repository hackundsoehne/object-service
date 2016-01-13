package edu.kit.ipd.crowdcontrol.objectservice.template;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class TemplateTest {
	@Test
	public void parse() {
		String template = "Foobar: {{Foobar:Foobar!}}, Baz: {{Baz:Baz!:color}}, {{Baz}}";
		Map<String, Placeholder> placeholderMap = Template.parse(template);

		Set<String> expectedSet = new HashSet<>();
		expectedSet.add("Foobar");
		expectedSet.add("Baz");

		assertEquals(expectedSet, placeholderMap.keySet());

		List<Placeholder.Position> expectedPositions = new ArrayList<>();
		expectedPositions.add(new Placeholder.Position(8, 26));

		assertEquals(new Placeholder("Foobar", "Foobar!", "text", expectedPositions), placeholderMap.get("Foobar"));

		expectedPositions = new ArrayList<>();
		expectedPositions.add(new Placeholder.Position(33, 51));
		expectedPositions.add(new Placeholder.Position(53, 60));

		assertEquals(new Placeholder("Baz", "Baz!", "color", expectedPositions), placeholderMap.get("Baz"));
	}

	@Test
	public void apply() {
		String template = "Foobar: {{Foobar:Foobar!}}, Baz: {{Baz:Baz!:color}}, {{Baz}}";
		String expected = "Foobar: abc, Baz: 123, 123";

		Map<String, String> map = new HashMap<>();
		map.put("Foobar", "abc");
		map.put("Baz", "123");

		assertEquals(expected, Template.apply(template, map));
	}

	@Test (expected = IllegalArgumentException.class)
	public void applyIllegal() {
		Template.apply("{{Foobar}}", new HashMap<>());
	}
}
