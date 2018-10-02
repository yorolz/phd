package dk.aaue.sna.ext.csv;

/**
 * Implements default node extractors.
 *
 * @author Soren <soren@tanesha.net>
 */
public class NodeExtractors {

    public static class StringNodeExtractor implements NodeExtractor<String> {

        public final int idPosition;
        public final String defaultValue;

        public StringNodeExtractor(int idPosition, String defaultValue) {
            this.idPosition = idPosition;
            this.defaultValue = defaultValue;
        }

        public StringNodeExtractor(int idPosition) {
            this(idPosition, null);
        }

        @Override
        public String createNode(String[] values) {
            if (idPosition < values.length)
                return values[idPosition];
            else
                return defaultValue;
        }
    }

}
