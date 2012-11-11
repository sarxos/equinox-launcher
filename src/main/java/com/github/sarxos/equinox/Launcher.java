package com.github.sarxos.equinox;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;


public class Launcher {

	private static String[] jars = null;
	private static String[] libs = null;

	private BundleContext context;

	private Launcher() {

		FrameworkFactory frameworkFactory = ServiceLoader.load(FrameworkFactory.class).iterator().next();

		Map<String, String> config = new HashMap<String, String>();
		config.put("osgi.console", "");
		config.put("osgi.clean", "true");
		config.put("osgi.noShutdown", "true");
		config.put("eclipse.ignoreApp", "true");
		config.put("osgi.bundles.defaultStartLevel", "4");
		config.put("osgi.configuration.area", "./configuration");

		// config.put("osgi.debug", "configuration/debug.options");
		// config.put("osgi.console", "localhost:2223");
		// config.put("osgi.console.ssh", "localhost:2222");
		// config.put("osgi.console.ssh.useDefaultSecureStorage", "true");
		// config.put("osgi.console.enable.builtin", "false");

		// automated bundles deployment
		config.put("felix.fileinstall.dir", "./dropins");
		config.put("felix.fileinstall.noInitialDelay", "true");
		config.put("felix.fileinstall.start.level", "4");

		//@formatter:off
		// System.setProperty("ssh.server.keystore", "configuration/hostkey.ser");
		// System.setProperty("org.eclipse.equinox.console.jaas.file", "configuration/store");
		// System.setProperty("java.security.auth.login.config", "configuration/org.eclipse.equinox.console.authentication.config");
		//@formatter:on

		Framework framework = frameworkFactory.newFramework(config);

		try {
			framework.start();
		} catch (BundleException e) {
			e.printStackTrace();
		}

		context = framework.getBundleContext();

		// logging
		Bundle b1 = install("slf4j-api");
		Bundle b2 = install("logback-core");
		Bundle b3 = install("logback-classic");
		try {
			b1.start();
			b2.start();
			b3.start();
		} catch (BundleException e) {
			e.printStackTrace();
		}

		// framework bundles
		start("org.eclipse.osgi.services");
		start("org.eclipse.osgi.util");
		start("org.eclipse.equinox.common");
		start("org.eclipse.equinox.registry");
		start("org.eclipse.equinox.preferences");
		start("org.eclipse.equinox.app");
		start("org.eclipse.core.jobs");
		start("org.eclipse.core.contenttype");
		start("org.eclipse.core.runtime");
		start("org.eclipse.equinox.security");
		start("org.eclipse.equinox.event");
		start("org.eclipse.equinox.log");

		// security
		start("bcprov-ext-jdk16");

		// default shell
		start("org.apache.felix.gogo.runtime");
		start("org.apache.felix.gogo.command");
		start("org.apache.felix.gogo.shell");
		start("org.eclipse.equinox.console");

		// automated bundles deployment
		start("org.apache.felix.fileinstall");

		// mvn locator
		start("pax-url-mvn");

		// wrap locator (wrap non-OSGi into bundle)
		start("pax-url-wrap");

		// framework admin
		// start("org.eclipse.equinox.frameworkadmin");

		// ssh console
		// start("org.apache.mina.core");
		// start("org.apache.sshd.core");
		// start("org.eclipse.equinox.console.ssh");
		// install("org.eclipse.equinox.console.jaas.fragment");

		// p2 dropins discovery (doesn't work)
		// start("org.sat4j.core");
		// start("org.sat4j.pb");
		// start("org.eclipse.equinox.p2.core");
		// start("org.eclipse.equinox.p2.discovery");
		// start("org.eclipse.equinox.p2.metadata");
		// start("org.eclipse.equinox.p2.repository");
		// start("org.eclipse.equinox.p2.metadata.repository");
		// start("org.eclipse.equinox.p2.engine");
		// start("org.eclipse.equinox.p2.director");
		// start("org.eclipse.equinox.p2.director.app");
	}

	public static void main(String[] args) {
		new Launcher();
	}

	private String[] getJARs() {
		if (jars == null) {
			List<String> jarsList = new ArrayList<String>();
			File pluginsDir = new File("plugins");
			for (String jar : pluginsDir.list()) {
				jarsList.add(jar);
			}
			jars = jarsList.toArray(new String[jarsList.size()]);
		}
		return jars;
	}

	private String[] getLibs() {
		if (libs == null) {
			List<String> jarsList = new ArrayList<String>();
			File pluginsDir = new File("libs");
			for (String jar : pluginsDir.list()) {
				jarsList.add(jar);
			}
			libs = jarsList.toArray(new String[jarsList.size()]);
		}
		return libs;
	}

	protected Bundle start(String name) {
		Bundle bundle = install(name);
		if (bundle != null) {
			try {
				bundle.start();
			} catch (BundleException e) {
				e.printStackTrace();
			}
		}
		return bundle;
	}

	protected Bundle install(String name) {
		String found = null;
		for (String jar : getJARs()) {
			if (jar.startsWith(name + "_") || jar.startsWith(name + "-")) {
				found = String.format("file:plugins/%s", jar);
				break;
			}
		}
		for (String jar : getLibs()) {
			if (jar.startsWith(name + "_") || jar.startsWith(name + "-")) {
				found = String.format("file:libs/%s", jar);
				break;
			}
		}
		if (found == null) {
			throw new RuntimeException(String.format("JAR for %s not found", name));
		}
		try {
			return context.installBundle(found);
		} catch (BundleException e) {
			e.printStackTrace();
		}
		return null;
	}
}
