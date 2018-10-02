
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class GraphNode {
	Hashtable relations = new Hashtable(); // Stores all relations
	String concept;
	String domain;
	ArrayList candidates = new ArrayList();
	double attributes = 0; // @ 1 because of the size of the ArrayList that save the mappings

	GraphNode(String concept, String domain) {
		this.concept = concept;
		this.domain = domain;
	}

	void addEdge(String newrel, String newdest) {
		String tmp;
		attributes++;
		// System.out.println(concept+" > "+newrel+" > "+newdest+" > "+relations.contains(newrel));
		if (relations.get(newrel) != null)// When exists multiple attributes of a same type they stay like this: isa > vehicle;aircraft;space_ship
		{
			tmp = "" + relations.get(newrel);
			relations.remove(newrel);
			relations.put(newrel, tmp + ";" + newdest);
		} else
			relations.put(newrel, newdest);
	}

	@Override
	public String toString() {
		Enumeration e = relations.keys();
		String tmp, tmpF = "";

		while (e.hasMoreElements())
			tmpF += concept + "--" + (tmp = "" + e.nextElement()) + "-->" + relations.get(tmp) + "\n";

		return tmpF;
	}

	public Hashtable getRelations() {
		return relations;

	}

	public ArrayList getDestinations() // Return a ArrayList with all destinations
	{
		Enumeration e = relations.elements();
		ArrayList v = new ArrayList();
		StringTokenizer st;

		while (e.hasMoreElements()) {
			st = new StringTokenizer(e.nextElement() + "", ";");
			while (st.hasMoreElements())
				v.add(st.nextToken());
		}

		return v;

	}

	/*
	 * NOTE > This method doesnt work correctly
	 */
	public Hashtable getRelationsAndDestinations() // Return an hashtable with just a destination to one relation
	{
		Enumeration e = relations.elements();
		Enumeration e2 = relations.keys();
		Hashtable h = new Hashtable();
		StringTokenizer st;
		String rel;

		while (e.hasMoreElements()) {
			rel = "" + e2.nextElement();
			st = new StringTokenizer(e.nextElement() + "", ";");
			while (st.hasMoreElements())
				h.put(rel, st.nextToken());
		}

		return h;

	}

	public boolean hasRelation(String rel, String dest) {
		boolean f;
		StringTokenizer st;
		if (!(f = (relations.get(rel) + "").equals(dest))) {
			st = new StringTokenizer(relations.get(rel) + "", ";");
			if (st.countTokens() > 1)
				while (st.hasMoreElements())
					if (st.nextToken().equals(dest))
						return true;
		}
		return f;
	}

	public String hasDest(String dest) {
		StringTokenizer st;
		String rel = null;
		Enumeration e = relations.keys();

		while (e.hasMoreElements()) {
			if (!(relations.get((rel = "" + e.nextElement()))).equals(dest)) {
				st = new StringTokenizer(relations.get(rel) + "", ";");
				if (st.countTokens() > 1)
					while (st.hasMoreElements())
						if (st.nextToken().equals(dest))
							return rel;
			} else
				return rel;
		}
		return null;
	}

	public void substituteDest(String rel, String oldDest, String newDest) {
		StringTokenizer st;
		String tmp = "", tmpF = "";
		try {
			if (!(relations.get(rel) + "").equals(oldDest)) {
				st = new StringTokenizer(relations.get(rel) + "", ";");
				if (st.countTokens() > 1) {
					while (st.hasMoreElements()) {
						tmp = st.nextToken();
						if (tmp.equals(oldDest))
							tmpF += newDest + ";";
						else
							tmpF += tmp + ";";
					}
					relations.remove(rel);
					relations.put(rel, tmpF.substring(0, tmpF.length() - 1));
				}

			} else {
				relations.remove(rel);
				relations.put(rel, newDest);
			}
		} catch (Exception e) {
			System.err.println("Didn't found the following relation: " + rel);
			e.printStackTrace();
		}

	}

	public void findCandidates(Graph vehicle, Graph generic) {
		Enumeration relationsEnum = relations.keys();

		while (relationsEnum.hasMoreElements()) {
			String relation = "" + relationsEnum.nextElement();
			String target = "" + relations.get(relation);
			Enumeration enu = vehicle.getEnumeration();
			while (enu.hasMoreElements()) {
				GraphNode possible = (GraphNode) enu.nextElement();
				StringTokenizer st = new StringTokenizer(target, ";");
				while (st.hasMoreElements()) {
					String target2 = st.nextToken();
					if (possible.hasRelation(relation, target2)) {
						candidates.add(possible);
						break;
					}
					// TODO: colocar aqui outro timeout
				}
			}
		}

	}
}
