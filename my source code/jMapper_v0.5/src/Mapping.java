
import java.util.ArrayList;

/**
 * More detailed class for the metaphor
 * The ArrayList maplist contains objects of the type Pair
 * @author Rui P. Costa
 */
public class Mapping
{
	ArrayList<Pair> maplist = new ArrayList<Pair>();
	double value;

        public void setMaplist(ArrayList<Pair> maplist) {
            this.maplist = maplist;
        }

        public void setValue(double value) {
            this.value = value;
        }
        
        
        
        public String toHTML()
	{
		String s="<BR>";

		s+=((Pair)maplist.get(0)).source.concept+"&lt;-- "+value+" --&gt;"+((Pair)maplist.get(0)).target.concept+"<BR>[";

		for(int i=1; i<maplist.size(); i++)
			s+=((Pair)maplist.get(i)).source.concept+",";

		return (s.substring(0,s.length()-1)+"]")+buildDifferencesHTML();
	}

        @Override
	public String toString()
	{
		String s="\n";

		s+=((Pair)maplist.get(0)).source.concept+"<-- "+value+" -->"+((Pair)maplist.get(0)).target.concept+"\n[";

		for(int i=1; i<maplist.size(); i++)
			s+=((Pair)maplist.get(i)).source.concept+",";

		return (s.substring(0,s.length()-1)+"]")+buildDifferences();
	}
	
	/**
	 * Simply print the differences between sourcelist and targetlist to maplist
	 * @return
	 */
	private String buildDifferences()
	{
		String s="\n";	
		
		ArrayList dest = ((Pair)maplist.get(0)).source.getDestinations();
		ArrayList src = ((Pair)maplist.get(0)).target.getDestinations();
		ArrayList mappings = new ArrayList();
		
		for(int i=1; i<maplist.size(); i++)
			mappings.add(((Pair)maplist.get(i)).source.concept);
		
		s+="[";

		for(int i=0; i<dest.size(); i++)
			if(!mappings.contains(dest.get(i)))
				s+=dest.get(i)+",";
		
		if(s.charAt(s.length()-1)==',')
			s=s.substring(0,s.length()-1)+"] ";
		else
			s+="] ";
		
		s+="[";
		
		for(int i=0; i<src.size(); i++)
			if(!mappings.contains(src.get(i)))
				s+=src.get(i)+",";
		
		if(s.charAt(s.length()-1)==',')
			return s.substring(0,s.length()-1)+"]";
		else
			return s+"]";
	}
        
        /**
	 * Simply print the differences between sourcelist and targetlist to maplist
	 * @return
	 */
	private String buildDifferencesHTML()
	{
		String s="<BR>";	
		
		ArrayList dest = ((Pair)maplist.get(0)).source.getDestinations();
		ArrayList src = ((Pair)maplist.get(0)).target.getDestinations();
		ArrayList mappings = new ArrayList();
		
		for(int i=1; i<maplist.size(); i++)
			mappings.add(((Pair)maplist.get(i)).source.concept);
		
		s+="[";

		for(int i=0; i<dest.size(); i++)
			if(!mappings.contains(dest.get(i)))
				s+=dest.get(i)+",";
		
		if(s.charAt(s.length()-1)==',')
			s=s.substring(0,s.length()-1)+"] ";
		else
			s+="] ";
		
		s+="[";
		
		for(int i=0; i<src.size(); i++)
			if(!mappings.contains(src.get(i)))
				s+=src.get(i)+",";
		
		if(s.charAt(s.length()-1)==',')
			return s.substring(0,s.length()-1)+"]";
		else
			return s+"]";
	}

	public String xIsTheYofZ()
	{
		return ((Pair)maplist.get(0)).source.concept+" is the "+((Pair)maplist.get(0)).target.concept+" of "+((Pair)maplist.get(0)).source.domain+".";
	}

}