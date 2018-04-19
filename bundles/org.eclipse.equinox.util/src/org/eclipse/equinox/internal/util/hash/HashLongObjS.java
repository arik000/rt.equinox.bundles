/*******************************************************************************
 * Copyright (c) 1997, 2018 by ProSyst Software GmbH
 * http://www.prosyst.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ProSyst Software GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.internal.util.hash;

/**
 * Synchronized extension of org.eclipse.equinox.internal.util.hash.HashLongObjNS
 * 
 * @author Pavlin Dobrev
 * @version 1.0
 */

public final class HashLongObjS extends HashLongObjNS {

	public HashLongObjS() {
		super(101, LOAD_FACTOR);
	}

	public HashLongObjS(int capacity) {
		super(capacity, LOAD_FACTOR);
	}

	public HashLongObjS(int capacity, double lf) {
		super(capacity, lf);
	}

	@Override
	public synchronized void put(long key, Object value) {
		super.put(key, value);
	}

	@Override
	public synchronized Object get(long key) {
		return super.get(key);
	}

	@Override
	public synchronized Object remove(long key) {
		return super.remove(key);
	}

	@Override
	public synchronized int size() {
		return super.size();
	}

	@Override
	public synchronized void removeAll() {
		super.removeAll();
	}
}
