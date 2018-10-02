package dk.aaue.sna.ext.csv;

/**
 * @author Soren <soren@tanesha.net>
 */
public class WeightExtractor {

    private int position;

    public WeightExtractor(int position) {
        this.position = position;
    }

    public double createWeight(String[] csv) {
        if (csv.length > position)
            return Double.parseDouble(csv[position]);
        else
            return 1.0;
    }
}
