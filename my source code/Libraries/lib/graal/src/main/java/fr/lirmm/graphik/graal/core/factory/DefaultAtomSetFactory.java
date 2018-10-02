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
/**
 * 
 */
package fr.lirmm.graphik.graal.core.factory;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.AtomSet;
import fr.lirmm.graphik.graal.api.core.InMemoryAtomSet;
import fr.lirmm.graphik.graal.api.factory.InMemoryAtomSetFactory;
import fr.lirmm.graphik.graal.core.atomset.LinkedListAtomSet;
import fr.lirmm.graphik.util.stream.CloseableIterator;
import fr.lirmm.graphik.util.stream.CloseableIteratorWithoutException;
import fr.lirmm.graphik.util.stream.IteratorException;

/**
 * @author Clément Sipieter (INRIA) {@literal <clement@6pi.fr>}
 * 
 */
public final class DefaultAtomSetFactory implements InMemoryAtomSetFactory {

	private static DefaultAtomSetFactory instance = new DefaultAtomSetFactory();

	private DefaultAtomSetFactory() {
	}

	public static DefaultAtomSetFactory instance() {
		return instance;
	}

	@Override
	public InMemoryAtomSet create() {
		return new LinkedListAtomSet();
	}

	@Override
	public InMemoryAtomSet create(Atom... atoms) {
		InMemoryAtomSet atomset = this.create();
		for (Atom a : atoms) {
			atomset.add(a);
		}
		return atomset;
	}
	
	@Override
	public InMemoryAtomSet create(CloseableIterator<Atom> atoms) throws IteratorException {
		InMemoryAtomSet atomset = this.create();
		while(atoms.hasNext()) {
			atomset.add(atoms.next());
		}
		return atomset;
	}
	
	@Override
	public InMemoryAtomSet create(CloseableIteratorWithoutException<Atom> atoms) {
		InMemoryAtomSet atomset = this.create();
		while(atoms.hasNext()) {
			atomset.add(atoms.next());
		}
		return atomset;
	}

	@Override
	public InMemoryAtomSet create(AtomSet src) throws IteratorException {
		InMemoryAtomSet atomset = this.create();
		CloseableIterator<Atom> it = src.iterator();
		while(it.hasNext()) {
			Atom a = it.next();
			atomset.add(a);
		}
		return atomset;
	}

	@Override
	public InMemoryAtomSet create(InMemoryAtomSet src) {
		try {
			return create((AtomSet) src);
		} catch (IteratorException e) {
			throw new Error("Should never happen");
		}
	}

	@Override
	public InMemoryAtomSet create(Atom atom) {
		return new LinkedListAtomSet(atom);
	}
}
