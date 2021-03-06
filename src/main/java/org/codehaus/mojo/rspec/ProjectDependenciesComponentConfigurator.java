package org.codehaus.mojo.rspec;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.classworlds.ClassRealm;
import org.codehaus.plexus.component.configurator.AbstractComponentConfigurator;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ConfigurationListener;
import org.codehaus.plexus.component.configurator.converters.composite.ObjectWithFieldsConverter;
import org.codehaus.plexus.component.configurator.converters.special.ClassRealmConverter;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

/**
 * A custom ComponentConfigurator which adds the project's runtime classpath
 * elements to the
 * 
 * @author Brian Jackson
 * @author Andre Goncalves
 * @since Aug 1, 2008 3:04:17 PM
 * 
 * @plexus.component 
 *                   role="org.codehaus.plexus.component.configurator.ComponentConfigurator"
 *                   role-hint="project-dependencies"
 * @plexus.requirement role=
 *                     "org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup"
 *                     role-hint="default"
 */
public final class ProjectDependenciesComponentConfigurator extends
		AbstractComponentConfigurator {
	
	// adicionar * @configurator project-dependencies ao mojo pra utlizar

	private final Log log = LogFactory
			.getLog(ProjectDependenciesComponentConfigurator.class);

	public void configureComponent(Object component,
			PlexusConfiguration configuration,
			ExpressionEvaluator expressionEvaluator, ClassRealm containerRealm,
			ConfigurationListener listener)
			throws ComponentConfigurationException {

		converterLookup.registerConverter(new ClassRealmConverter(
				classRealmWithProjectDependencies(expressionEvaluator,
						containerRealm)));

		new ObjectWithFieldsConverter().processConfiguration(converterLookup,
				component, containerRealm.getClassLoader(), configuration,
				expressionEvaluator, listener);
	}

	private ClassRealm classRealmWithProjectDependencies(
			ExpressionEvaluator expressionEvaluator, ClassRealm containerRealm)
			throws ComponentConfigurationException {
		List<String> runtimeClasspathElements;
		try {
			runtimeClasspathElements = runtimeClasspathElements(expressionEvaluator);
		} catch (ExpressionEvaluationException e) {
			throw new ComponentConfigurationException(
					"There was a problem evaluating: ${project.runtimeClasspathElements}",
					e);
		}

		// Add the project dependencies to the ClassRealm
		final URL[] urls = buildURLs(runtimeClasspathElements);
		for (URL url : urls) {
			containerRealm.addConstituent(url);
		}
		return containerRealm;
	}

	private List<String> runtimeClasspathElements(
			ExpressionEvaluator expressionEvaluator)
			throws ExpressionEvaluationException {
		@SuppressWarnings("unchecked")
		final List<String> runtimeClasspathElements = (List<String>) expressionEvaluator
				.evaluate("${project.runtimeClasspathElements}");
		return runtimeClasspathElements;
	}

	private URL[] buildURLs(List<String> runtimeClasspathElements)
			throws ComponentConfigurationException {
		// Add the projects classes and dependencies
		List<URL> urls = new ArrayList<URL>(runtimeClasspathElements.size());
		for (String element : runtimeClasspathElements) {
			try {
				final URL url = new File(element).toURI().toURL();
				urls.add(url);
				log.debug("Added to project class loader: " + url);
			} catch (MalformedURLException e) {
				throw new ComponentConfigurationException(
						"Unable to access project dependency: " + element, e);
			}
		}

		return urls.toArray(new URL[urls.size()]);
	}

}