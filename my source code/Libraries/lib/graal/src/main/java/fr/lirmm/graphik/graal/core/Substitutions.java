/*
 * Copyright (C) Inria Sophia Antipolis - Méditerranée / LIRMM
 * (Université de Montpellier & CNRS) (2014 - 2017)
 *
 * Contributors :
 *
 * Clément SIPIETER <clement.sipieter@inria.fr>
 * Mélanie KÖNIG
 * Swan ROCHER
 * Jean-François BAGET
 * Michel LECLÈRE
 * Marie-Laure MUGNIER <mugnier@lirmm.fr>
 *
 *
 * This file is part of Graal <https://graphik-team.github.io/graal/>.
 *
 * This software is governed by the CeCILL  license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 */
package fr.lirmm.graphik.graal.core;

import java.util.LinkedList;
import java.util.List;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.Substitution;
import fr.lirmm.graphik.graal.api.core.Term;
import fr.lirmm.graphik.graal.api.core.Variable;
import fr.lirmm.graphik.graal.core.factory.DefaultSubstitutionFactory;
import fr.lirmm.graphik.util.Partition;

/**
 * @author Clément Sipieter (INRIA) {@literal <clement@6pi.fr>}
 *
 */
public final class Substitutions {

	// /////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	// /////////////////////////////////////////////////////////////////////////

	private Substitutions() {
	}

	// /////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	// /////////////////////////////////////////////////////////////////////////

	public static Substitution emptySubstitution() {
		return EmptySubstitution.instance();
	}

	public static Substitution add(Substitution s1, Substitution s2) {
		Substitution newSub = new TreeMapSubstitution(s1);
		for (Variable term : s2.getTerms()) {
			if (!newSub.put(term, s2.createImageOf(term))) {
				return null;
			}
		}
		return newSub;
	}

	public static Partition<Term> toPartition(Substitution s) {
		Partition<Term> partition = new Partition<Term>();
		for (Variable v : s.getTerms()) {
			partition.add(v, s.createImageOf(v));
		}
		return partition;
	}

	/**
	 * Create a new Atom which is the image of the specified atom by replacing
	 * the specified term by the specified image.
	 * 
	 * @param atom
	 * @param var
	 *            the variable to replace
	 * @param image
	 *            the image of the specified term
	 * @return a new Atom which is the image of the specified atom.
	 */
	public static Atom createImageOf(Atom atom, Variable var, Term image) {
		List<Term> termsSubstitut = new LinkedList<Term>();
		for (Term t : atom.getTerms()) {
			if (var.equals(t)) {
				termsSubstitut.add(image);
			} else {
				termsSubstitut.add(t);
			}
		}

		return new DefaultAtom(atom.getPredicate(), termsSubstitut);
	}

	public static Substitution aggregate(Substitution s1, Substitution s2) {
		Substitution newSub = DefaultSubstitutionFactory.instance().createSubstitution(s1);
		for (Variable term : s2.getTerms()) {
			if (!newSub.aggregate(term, s2.createImageOf(term))) {
				return null;
			}
		}
		return newSub;
	}

}
