/**
 * This class is responsable for storing the metaphors
 * When you call run_mapper method it return an ArrayList of Metaphors
 * 
 * @author Rui P. Costa
 */
public class Metaphor {
    private String source; 
    private String target;
    private double strength;
    private int recursive_level;

    public Metaphor(String source, String target, double strength, int recursive_level) {
        this.source = source;
        this.target = target;
        this.strength = strength;
        this.recursive_level = recursive_level;
    }

    public int getRecursive_level() {
        return recursive_level;
    }

    public String getSource() {
        return source;
    }

    public double getStrength() {
        return strength;
    }

    public String getTarget() {
        return target;
    }

    public void setRecursive_level(int recursive_level) {
        this.recursive_level = recursive_level;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setStrength(double strength) {
        this.strength = strength;
    }

    public void setTarget(String target) {
        this.target = target;
    }
    
    
    
    @Override
    public String toString()
    {
            return source+"<- "+strength+" ->"+target+" ("+recursive_level+")";
    }
}
