import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

public class jMapperUsageExample{
	public static void main(String args[]){
		ArrayList <String> ve, te;
        ArrayList <Metaphor> metaphors;
        String ste, sve;
        jMapper jm;
        
        System.out.println(System.getProperty("user.dir"));

		//1º READ SOURCE ONTOLOGY (TENOR)
        String tenorPath = "../ontologies/classics/king_arthur.txt";        
        ste = readFile(tenorPath);
        te = arrangeString(ste);   
        String domainTen = new StringTokenizer(tenorPath, ".", false).nextToken();        

		//2º READ TARGET ONTOLOGY (VEHICLE)
        String vehiclePath = "../ontologies/classics/starwars.txt";
		sve = readFile(vehiclePath);
        ve = arrangeString(sve);
        String domainVei = new StringTokenizer(vehiclePath, ".", false).nextToken();
		
		//3º SET THE PARAMETERS (if not defined the default value kept)
        jm = new jMapper();
        jm.setAnalogyTax(0.5);
        jm.setLowest_analogyTax(0.3);
        jm.setDepth(3);
        jm.setStrongers(true);
        
		//4º SET THE TWO ONTOLOGIES
        jm.set_graphs(ve, te);
		
		//5º SET THE TWO ONTOLOGIES
        System.out.println(domainTen+" VS "+domainVei);
        metaphors = jm.runMapper();
        
        System.out.println("Final mappings ("+metaphors.size()+"):");        
		printMetaphors(metaphors);
	}
	
	static void printMetaphors(ArrayList<Metaphor> a){
		for(int i=0;i<a.size();i++)
			System.out.println(a.get(i));
	}
	
	static String readFile(String file) {
	    String res = "";
	    Scanner sc = null;
	    
	    try {
	    	sc = new Scanner(new File(file));
	    } catch (Exception e) {
	        return "[]";
	    }
	
	    while (sc.hasNextLine())
	    {
	        res = res + sc.nextLine();
	    }
	
	    return res;
	}
	
	static ArrayList<String> arrangeString(String input) {
	    ArrayList <String>strings = new ArrayList<String>();
	
	    String[] tokens = input.split("\\.");
	
	    for (int i=0; i<tokens.length; i++)
	        strings.add(tokens[i]);    
	
	    return strings;
	}
}