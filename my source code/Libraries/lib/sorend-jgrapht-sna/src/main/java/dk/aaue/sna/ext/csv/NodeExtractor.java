package dk.aaue.sna.ext.csv;

/**
 * Creates a node based on an input line in the CSV.
 *
 * @author Soren <soren@tanesha.net>
 */
public interface NodeExtractor<V> {
    /**
     * Creates a node
     * @param values The CSV input
     * @return The node.
     */
    public V createNode(String[] values);
}
