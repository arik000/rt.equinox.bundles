/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.internal.security.storage.friends;

import java.net.URL;
import java.util.*;
import javax.crypto.spec.PBEKeySpec;
import org.eclipse.equinox.internal.security.auth.AuthPlugin;
import org.eclipse.equinox.internal.security.storage.*;
import org.eclipse.equinox.internal.security.storage.PasswordProviderSelector.ExtStorageModule;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.provider.IPreferencesContainer;
import org.osgi.framework.BundleContext;

/**
 * Collection of utilities that gives friends additional access into
 * internals of the secure storage.
 */
public class InternalExchangeUtils {

	static public final String HINT_PASSWORD_AUTOGEN = "AutomaticPasswordGeneration"; //$NON-NLS-1$

	static private final String JUNIT_APPS1 = "org.eclipse.pde.junit.runtime."; //$NON-NLS-1$
	static private final String JUNIT_APPS2 = "org.eclipse.test."; //$NON-NLS-1$

	static private List listeners = new ArrayList();

	/**
	 * Detects ciphers supplied by the current JVM that can be used with
	 * the secure storage. Returns Map of pairs: supported cipher algorithm to 
	 * a supported key factory algorithm.
	 */
	static public Map ciphersDetectAvailable() {
		return new JavaEncryption().detect();
	}

	/**
	 * Gathers list of available password providers. Note: this method does not try
	 * to instantiate providers, hence, providers listed as available by this method
	 * might fail on instantiation and not be available for the actual use.
	 * @return available password providers as described in extensions
	 */
	static public List passwordProvidersFind() {
		List availableModules = PasswordProviderSelector.getInstance().findAvailableModules(null);
		List result = new ArrayList(availableModules.size());
		for (Iterator i = availableModules.iterator(); i.hasNext();) {
			ExtStorageModule module = (ExtStorageModule) i.next();
			result.add(new PasswordProviderDescription(module.name, module.moduleID, module.priority, module.description, module.hints));
		}
		return result;
	}

	/**
	 * Clears cached information from password providers.
	 */
	static public void passwordProvidersReset() {
		PasswordProviderSelector.getInstance().clearCaches();
	}

	static public boolean isLoggedIn() {
		return PasswordProviderSelector.getInstance().isLoggedIn();
	}

	/**
	 * Returns location of default storage
	 * @return location of the default storage, might be null
	 */
	static public URL defaultStorageLocation() {
		ISecurePreferences defaultStorage = SecurePreferencesFactory.getDefault();
		if (defaultStorage == null)
			return null;
		return ((SecurePreferencesWrapper) defaultStorage).getContainer().getLocation();
	}

	/**
	 * Closes open default storage, if any, and deletes the actual file.
	 */
	static public void defaultStorageDelete() {
		ISecurePreferences defaultStorage = SecurePreferencesFactory.getDefault();
		if (defaultStorage == null)
			return;
		URL location = defaultStorageLocation();
		if (location == null)
			return;

		// clear the default preferences store from the mapper
		SecurePreferencesMapper.clearDefault();

		// delete the actual file
		if (StorageUtils.exists(location))
			StorageUtils.delete(location);

		// notify listeners
		synchronized (listeners) {
			for (Iterator i = listeners.iterator(); i.hasNext();) {
				IDeleteListener listener = (IDeleteListener) i.next();
				listener.onDeleted();
			}
		}
	}

	/**
	 * Registers a new listener to be notified when default preferences are deleted.
	 * @param listener class to be notified after default preferences are deleted
	 */
	static public void addDeleteListener(IDeleteListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	static public void setupRecovery(String[][] userParts, String moduleID, IPreferencesContainer container) {
		PasswordManagement.setupRecovery(userParts, moduleID, container);
	}

	static public String[] getPasswordRecoveryQuestions(ISecurePreferences node, String moduleID) {
		SecurePreferencesRoot rootNode = ((SecurePreferencesWrapper) node).getContainer().getRootData();
		return PasswordManagement.getPasswordRecoveryQuestions(rootNode, moduleID);
	}

	static public String recoverPassword(String[] answers, ISecurePreferences node, String moduleID) {
		SecurePreferencesRoot rootNode = ((SecurePreferencesWrapper) node).getContainer().getRootData();
		String password = PasswordManagement.recoverPassword(answers, rootNode, moduleID);
		if (password != null)
			rootNode.cachePassword(moduleID, new PasswordExt(new PBEKeySpec(password.toCharArray()), moduleID));
		return password;
	}

	/**
	 * This is a bit of a strange code that tries to determine if we are running in a JUnit
	 */
	static public boolean isJUnitApp() {
		BundleContext context = AuthPlugin.getDefault().getBundleContext();
		if (context == null)
			return false;
		String app = context.getProperty("eclipse.application"); //$NON-NLS-1$
		if (app == null)
			return false;
		if (app.startsWith(JUNIT_APPS1))
			return true;
		if (app.startsWith(JUNIT_APPS2))
			return true;
		return false;
	}

}
