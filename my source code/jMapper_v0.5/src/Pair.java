/**
 * Used to save mappings information
 * @author Rui P. Costa
 */
public class Pair{
	GraphNode source, target;
        int recursive_level;

        public void setRecursive_level(int recursive_level) {
            this.recursive_level = recursive_level;
        }

        public int getRecursive_level() {
            return recursive_level;
        }      
        

	Pair(GraphNode source, GraphNode target)
	{
		this.source=source;
		this.target=target;
	}

	public String toString()
	{
		return source.concept+"/"+target.concept;
		//return source.concept+" is the "+target.concept+" of "+tenor.domain;
	}

	public boolean equals(Pair p)
	{
		return p.source.concept.equals(source.concept)&&p.target.concept.equals(target.concept);

	}
}