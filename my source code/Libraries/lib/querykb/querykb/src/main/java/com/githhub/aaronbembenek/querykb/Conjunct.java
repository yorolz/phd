package com.githhub.aaronbembenek.querykb;

public class Conjunct {

	private final String subject;
	private final String predicate;
	private final String object;
	
	private Conjunct(String predicate, String subject, String object) {
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
	}
	
	public static Conjunct make(String predicate, String subject, String object) {
		return new Conjunct(predicate, subject, object);
	}
	
	public String getPredicate() {
		return predicate;
	}

	public String getSubject() {
		return subject;
	}
	
	public String getObject() {
		return object;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(predicate);
		sb.append("(");
		sb.append(subject);
		sb.append(", ");
		sb.append(object);
		sb.append(")");
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((object == null) ? 0 : object.hashCode());
		result = prime * result + ((predicate == null) ? 0 : predicate.hashCode());
		result = prime * result + ((subject == null) ? 0 : subject.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Conjunct other = (Conjunct) obj;
		if (object == null) {
			if (other.object != null)
				return false;
		} else if (!object.equals(other.object))
			return false;
		if (predicate == null) {
			if (other.predicate != null)
				return false;
		} else if (!predicate.equals(other.predicate))
			return false;
		if (subject == null) {
			if (other.subject != null)
				return false;
		} else if (!subject.equals(other.subject))
			return false;
		return true;
	}

}
