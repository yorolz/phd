package jcfgonc.ws.json;

public class Link {
	public int source; // mapped to a string
	public int target; // mapped to a string
	public int key; // strange, no mapping to a string
	public String label;

	public Link(int sourceID, int targetID, int key, String label) {
		this.source = sourceID;
		this.target = targetID;
		this.key = key;
		this.label = label;
	}

	public Link(int sourceID, int targetID, int key) {
		this.source = sourceID;
		this.target = targetID;
		this.key = key;
		this.label = "";
	}
}
