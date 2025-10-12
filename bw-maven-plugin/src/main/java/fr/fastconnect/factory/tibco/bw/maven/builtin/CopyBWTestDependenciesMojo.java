/*
 * (C) Copyright 2011-2025 FastConnect SAS
 * (http://www.fastconnect.fr/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.fastconnect.factory.tibco.bw.maven.builtin;

import java.util.List;
import java.util.Properties;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import javax.inject.Inject;

/**
 * <p>
 * This goal copies TIBCO BusinessWorks dependencies (especially Projlibs) of
 * the 'test' scope in a temporary folder.<br/>
 * These dependencies will be used to create a unique environment (used to
 * launch the Designer and to launch tests).
 * </p>
 * 
 * <p>
 * A builtin goal from Maven is called by this goal with a custom configuration
 * defined in the 'components.xml' file from Plexus. This allows to use the
 * builtin goal bound to a lifecycle phase without adding configuration in POMs.
 * <br />
 * Please refer to {@link AbstractWrapperForBuiltinMojo} for a full explanation
 * of the lifecycle binding of a builtin Maven plugin.
 * </p>
 * 
 * <p>
 * <u>Original goal</u> :
 * <b>org.apache.maven.plugins:maven-dependency-plugin:copy-dependencies</b>
 * </p>
 * 
 * @goal copy-bw-test-dependencies
 * @inheritByDefault true
 * @requiresProject true
 * @aggregator true
 * @requiresDependencyResolution test
 *
 * @author Mathieu Debove
 * 
 */
@Mojo(name = "copy-bw-test-dependencies", aggregator = true, requiresProject = true,
        requiresDependencyResolution = ResolutionScope.TEST)
public class CopyBWTestDependenciesMojo extends AbstractWrapperForBuiltinMojo<Resource> {
	// Mojo configuration
	/**
	 *  @parameter property="groupId"
	 */
    @Parameter(property = "groupId", defaultValue = "org.apache.maven.plugins")
    protected String groupId;
	
	@Override
	protected String getGroupId() {
		return groupId;
	}

	/**
	 *  @parameter property="artifactId"
	 */
    @Parameter(property = "artifactId", defaultValue = "maven-dependency-plugin")
    protected String artifactId;
	
	@Override
	protected String getArtifactId() {
		return artifactId;
	}
	/**
	 *  @parameter property="version"
	 */
    @Parameter(property = "version", defaultValue = "${maven.dependency.plugin.version}")
    protected String version;
	
	@Override
	protected String getVersion() {
		return version;
	}

	/**
	 *  @parameter property="goal"
	 */
    @Parameter(property = "goal", defaultValue = "copy-dependencies")
    protected String goal;
	
	@Override
	protected String getGoal() {
		return goal;
	}

	// Environment configuration
	/**
	 * The project currently being build.
	 *
	 * @parameter property="project"
	 * @required
	 * @readonly
	 */
	protected MavenProject project;

	@Override
	protected MavenProject getProject() {
		return project;
	}

	/**
	 * The current Maven session.
	 *
	 * @parameter property="session"
	 * @required
	 * @readonly
	 */
    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    protected MavenSession session;

	@Override
	protected MavenSession getSession() {
		return session;
	}

	/**
	 * The Build Plugin Manager (this one is Java5 annotation style).
	 */
    @Inject
	protected BuildPluginManager pluginManager;
	
	@Override
	protected BuildPluginManager getPluginManager() {
		return pluginManager;
	}

	// Configuration
    /**
     * The actual Mojo configuration found in the Plexus 'components.xml' file.
     * <pre>
 	 *	&lt;component>
 	 *		&lt;role>org.apache.maven.plugin.Mojo&lt;/role>
 	 *		&lt;role-hint>default-copy-bw-dependencies&lt;/role-hint>
	 *		&lt;implementation>fr.fastconnect.factory.tibco.bw.maven.builtin.CopyBWTestDependenciesMojo&lt;/implementation>
	 *		&lt;isolated-realm>false&lt;/isolated-realm>
	 *		&lt;configuration>
	 *			&lt;groupId>org.apache.maven.plugins&lt;/groupId>
 	 *			&lt;artifactId>maven-dependency-plugin&lt;/artifactId>
	 *			&lt;version>2.8&lt;/version>
	 *			&lt;goal>copy-dependencies&lt;/goal>
 	 *			&lt;configuration>
 	 *				&lt;property>
 	 *					&lt;name>outputDirectory&lt;/name>
 	 *					&lt;value>${project.build.test.directory}/lib&lt;/value>
 	 *				&lt;/property>
 	 *				&lt;property>
 	 *					&lt;name>includeTypes&lt;/name>
 	 *					&lt;value>projlib,jar&lt;/value>
 	 *				&lt;/property>
 	 *				&lt;property>
 	 *					&lt;name>includeScope&lt;/name>
 	 *					&lt;value>test&lt;/value>
 	 *				&lt;/property>
 	 *				&lt;property>
 	 *					&lt;name>overWriteIfNewer&lt;/name>
 	 *					&lt;value>true&lt;/value>
 	 *				&lt;/property>
 	 *				&lt;property>
 	 *					&lt;name>overWriteReleases&lt;/name>
 	 *					&lt;value>true&lt;/value>
 	 *				&lt;/property>
 	 *			&lt;/configuration>
	 *		&lt;/configuration>
 	 *		&lt;requirements>
     *			&lt;requirement>
	 *				&lt;role>org.apache.maven.plugin.BuildPluginManager&lt;/role>
     *				&lt;role-hint />
     *				&lt;field-name>pluginManager&lt;/field-name>
     *			&lt;/requirement>
     *		&lt;/requirements>
	 *	&lt;/component>
	 * </pre>
     * @parameter
     */
    @Parameter
    protected Properties configuration = defaultConfiguration();

	@Override
	protected Properties getConfiguration() {
        return mergeWithDefaults(configuration, defaultConfiguration());
	}

    /**
    * Optional resources parameter do define includes/excludes filesets
    * 
    * @parameter
    */
    @Parameter
    protected List<Resource> resources;
    
	@Override
	protected List<Resource> getResources() {
		return resources;
	}

    private static Properties defaultConfiguration() {
        Properties defaults = new Properties();
        defaults.setProperty("outputDirectory", "${project.build.test.directory}/lib");
        defaults.setProperty("includeTypes", "projlib,jar");
        defaults.setProperty("includeScope", "test");
        defaults.setProperty("overWriteIfNewer", Boolean.TRUE.toString());
        defaults.setProperty("overWriteReleases", Boolean.TRUE.toString());
        return defaults;
    }

    private static Properties mergeWithDefaults(Properties provided, Properties defaults) {
        if (provided == null || provided.isEmpty()) {
            return defaults;
        }

        Properties merged = new Properties();
        merged.putAll(defaults);
        merged.putAll(provided);
        return merged;
    }
}
