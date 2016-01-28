package edu.kit.ipd.crowdcontrol.objectservice.template;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses placeholders from templates and applies placeholder values to them.
 */
public class Template {
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{([^:{}]+)(?::([^:{}]+))?(?::([^:{}]+))?\\}\\}");

    private Template() {
        // intentionally left blank
    }

    /**
     * Parses a template.
     *
     * @param text Raw template.
     * @return A map, mapping placeholder names to their positions.
     */
    public static Map<String, Placeholder> parse(String text) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        Map<String, Placeholder> placeholders = new HashMap<>();
        List<Placeholder.Position> positions;
        Placeholder placeholder;

        while (matcher.find()) {
            String name = matcher.group(1);
            String description = matcher.group(2);
            String type = matcher.group(3);

            if (description == null) {
                description = "";
            }

            if (type == null) {
                type = "text";
            }

            if (placeholders.containsKey(name)) {
                placeholder = placeholders.get(name);
                positions = placeholder.getPositions();

                // If this placeholder is not the first occurrence,
                // use description and type from first occurrence.
                description = placeholder.getDescription();
                type = placeholder.getType();
            } else {
                positions = new ArrayList<>();
            }

            positions.add(new Placeholder.Position(matcher.start(), matcher.end()));

            placeholders.put(name, new Placeholder(name, description, type, positions));
        }

        return placeholders;
    }

    /**
     * Applies values to a template, effectively rendering it.
     *
     * @param text         Raw template.
     * @param placeholders Placeholder name to replacement map.
     * @return Rendered template.
     */
    public static String apply(String text, Map<String, String> placeholders) {
        Map<String, Placeholder> placeholderMap = parse(text);

        if (!placeholderMap.keySet().equals(placeholders.keySet())) {
            throw new IllegalArgumentException("Placeholders do not match the template's definition!");
        }

        int offset = 0;
        List<ReplaceOp> replacements = new ArrayList<>();

        for (Placeholder placeholder : placeholderMap.values()) {
            for (Placeholder.Position position : placeholder.getPositions()) {
                replacements.add(new ReplaceOp(position.getStart(), position.getEnd(), placeholders.get(placeholder.getName())));
            }
        }

        Collections.sort(replacements);

        for (ReplaceOp op : replacements) {
            text = text.substring(0, op.getStart() + offset) + op.getReplacement() + text.substring(op.getEnd() + offset);
            offset += op.getReplacement().length() - (op.getEnd() - op.getStart());
        }

        return text;
    }

    /**
     * Replacement operations have to be executed in order, so we need an additional helper class.
     */
    private static class ReplaceOp implements Comparable<ReplaceOp> {
        private int start;
        private int end;
        private String replacement;

        public ReplaceOp(int start, int end, String replacement) {
            this.start = start;
            this.end = end;
            this.replacement = replacement;
        }

        public int getStart() {
            return this.start;
        }

        public int getEnd() {
            return this.end;
        }

        public String getReplacement() {
            return this.replacement;
        }

        @Override
        public int compareTo(ReplaceOp op) {
            if (start < op.start) {
                return -1;
            }

            if (start > op.start) {
                return 1;
            }

            if (end < op.end) {
                return -1;
            }

            if (end > op.end) {
                return 1;
            }

            return 0;
        }
    }
}
