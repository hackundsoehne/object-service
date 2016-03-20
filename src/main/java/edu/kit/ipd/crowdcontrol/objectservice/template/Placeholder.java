package edu.kit.ipd.crowdcontrol.objectservice.template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Placeholder {
    private String name;
    private String description;
    private String type;
    private List<Position> positions;

    public Placeholder(String name, String description, String type, List<Position> positions) {
        if (name == null || description == null || type == null || positions == null) {
            throw new IllegalArgumentException("Null is not allowed!");
        }

        this.name = name;
        this.description = description;
        this.type = type;
        this.positions = positions;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String getType() {
        return this.type;
    }

    public List<Position> getPositions() {
        // Return copy to ensure immutability.
        return new ArrayList<>(this.positions);
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }

        if (object == null) {
            return false;
        }

        if (!(object instanceof Placeholder)) {
            return false;
        }

        Placeholder placeholder = (Placeholder) object;

        if (!placeholder.name.equals(name)) {
            return false;
        }

        if (!placeholder.description.equals(description)) {
            return false;
        }

        if (!placeholder.type.equals(type)) {
            return false;
        }

        List<Position> one = placeholder.getPositions();
        List<Position> two = this.getPositions();

        Collections.sort(one);
        Collections.sort(two);

        return one.equals(two);
    }

    @Override
    public int hashCode() {
        return this.name.hashCode() + this.description.hashCode() + this.type.hashCode();
    }

    @Override
    public String toString() {
        return String.format("Placeholder[name=%s,description=%s,type=%s,positions=%s]", name, description, type, positions);
    }

    public static class Position implements Comparable<Position> {
        private int start;
        private int end;

        public Position(int start, int end) {
            if (start > end) {
                throw new IllegalArgumentException("Start must not be greater than end!");
            }

            this.start = start;
            this.end = end;
        }

        public int getStart() {
            return this.start;
        }

        public int getEnd() {
            return this.end;
        }

        @Override
        public boolean equals(Object object) {
            if (object == this) {
                return true;
            }

            if (object == null) {
                return false;
            }
            return object instanceof Position && this.compareTo((Position) object) == 0;
        }

        @Override
        public int hashCode() {
            int hc = 17;

            hc = hc * 13 + start;
            hc = hc * 59 + end;

            return hc;
        }

        @Override
        public int compareTo(Position position) {
            if (start < position.start) {
                return -1;
            }

            if (start > position.start) {
                return 1;
            }

            if (end < position.end) {
                return -1;
            }

            if (end > position.end) {
                return 1;
            }

            return 0;
        }

        @Override
        public String toString() {
            return String.format("Position[start=%d,end=%d]", start, end);
        }
    }
}
