package edu.kit.ipd.crowdcontrol.objectservice.template;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class PlaceholderTest {
	@Test
	public void equals() {
		List<Placeholder.Position> positions = new ArrayList<>();
		positions.add(new Placeholder.Position(0, 1));

		Placeholder placeholder = new Placeholder("Foo", "", "text", positions);

		assertEquals(new Placeholder("Foo", "", "text", positions), placeholder);
		assertNotEquals(new Placeholder("Foo", "", "", positions), placeholder);
		assertNotEquals(new Placeholder("Fo", "", "text", positions), placeholder);
		assertNotEquals(new Placeholder("Foo", "Foo", "text", positions), placeholder);
		assertNotEquals(new Placeholder("Foo", "", "text", new ArrayList<>()), placeholder);

		assertNotEquals(placeholder, null);
		assertNotEquals(placeholder, new Object());
		assertEquals(placeholder, placeholder);

		Placeholder.Position position = new Placeholder.Position(0, 1);
		assertNotEquals(position, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void throwNull() {
		new Placeholder(null, null, null, null);
	}

	@Test
	public void compareTo() {
		assertEquals(-1, new Placeholder.Position(0, 2).compareTo(new Placeholder.Position(1, 3)));
		assertEquals(1, new Placeholder.Position(1, 3).compareTo(new Placeholder.Position(0, 2)));
		assertEquals(-1, new Placeholder.Position(1, 1).compareTo(new Placeholder.Position(1, 2)));
		assertEquals(1, new Placeholder.Position(1, 2).compareTo(new Placeholder.Position(1, 1)));
	}

	@Test (expected = IllegalArgumentException.class)
	public void throwStartEnd() {
		new Placeholder.Position(1, 0);
	}
}
