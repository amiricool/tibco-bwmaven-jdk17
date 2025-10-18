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
 * This goal deploys the built EAR to a TIBCO domain, to prepare the execution
 * of Integration Tests.
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
 * <b>org.apache.maven.plugins:maven-invoker-plugin:run</b>
 * </p>
 * 
 * @goal it-deploy-bw
 * @inheritByDefault true
 * @requiresProject true
 * @aggregator true
 * @requiresDependencyResolution test
 *
 * @author Mathieu Debove
 * @author Amir Marzouk
 * 
 */
@Mojo(name = "it-deploy-bw", aggregator = true, requiresProject = true,
        requiresDependencyResolution = ResolutionScope.TEST)
public class ITRunTestsMojo extends AbstractWrapperForBuiltinMojo<Resource> {
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
    @Parameter(property = "artifactId", defaultValue = "maven-invoker-plugin")
    protected String artifactId;
	
	@Override
	protected String getArtifactId() {
		return artifactId;
	}
	/**
	 *  @parameter property="version"
	 */
    @Parameter(property = "version", defaultValue = "${maven.invoker.plugin.version}")
    protected String version;
	
	@Override
	protected String getVersion() {
		return version;
	}

	/**
	 *  @parameter property="goal"
	 */
    @Parameter(property = "goal", defaultValue = "run")
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
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
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

	/**
	 * @parameter
	 */
    @Parameter(property = "bw.it.skip", defaultValue = "${bw.it.skip}")
    protected boolean skipInvocation;

	// Configuration
    /**
	 * The actual Mojo configuration found in the Plexus 'components.xml' file.
	 * 
	 * <pre>
	 * &lt;component>
	 * 	&lt;role>org.apache.maven.plugin.Mojo&lt;/role>
	 * 	&lt;role-hint>default-it-deploy-bw&lt;/role-hint>
	 * 	&lt;implementation>fr.fastconnect.factory.tibco.bw.maven.builtin.ITRunTestsMojo
	 * 	&lt;/implementation>
	 * 	&lt;isolated-realm>false&lt;/isolated-realm>
	 * 	&lt;configuration>
	 * 		&lt;groupId>org.apache.maven.plugins&lt;/groupId>
	 * 		&lt;artifactId>maven-invoker-plugin&lt;/artifactId>
	 * 		&lt;version>1.9&lt;/version>
	 * 		&lt;goal>run&lt;/goal>
	 * 		&lt;configuration>
	 * 			&lt;property>
	 * 				&lt;name>cloneProjectsTo&lt;/name>
	 * 				&lt;value>${bw.it.projects.run.clone}&lt;/value>
	 * 			&lt;/property>
	 * 			&lt;property>
	 * 				&lt;name>goals&lt;/name>
	 * 				&lt;value>${bw.it.projects.run.goals}&lt;/value>
	 * 			&lt;/property>
	 * 			&lt;property>
	 * 				&lt;name>localRepositoryPath&lt;/name>
	 * 				&lt;value>${bw.it.local.repository.path}&lt;/value>
	 * 			&lt;/property>
	 * 			&lt;property>
	 * 				&lt;name>pomIncludes&lt;/name>
	 * 				&lt;value>${bw.it.projects.run.pomIncludes}&lt;/value>
	 * 			&lt;/property>
	 * 			&lt;property>
	 * 				&lt;name>profiles&lt;/name>
	 * 				&lt;value>${bw.it.projects.run.profile}&lt;/value>
	 * 			&lt;/property>
	 * 			&lt;property>
	 * 				&lt;name>projectsDirectory&lt;/name>
	 * 				&lt;value>${bw.it.projects.run.directory}&lt;/value>
	 * 			&lt;/property>
	 * 			&lt;property>
	 * 				&lt;name>properties&lt;/name>
	 * 				&lt;value>${bw.it.projects.run.properties}&lt;/value>
	 * 			&lt;/property>
	 * 			&lt;property>
	 * 				&lt;name>streamLogs&lt;/name>
	 * 				&lt;value>true&lt;/value>
	 * 			&lt;/property>
	 * 			&lt;property>
	 * 				&lt;name>skipInvocation&lt;/name>
	 * 				&lt;value>${bw.it.skip}&lt;/value>
	 * 			&lt;/property>
	 * 		&lt;/configuration>
	 * 	&lt;/configuration>
	 * 	&lt;requirements>
	 * 		&lt;requirement>
	 * 			&lt;role>org.apache.maven.plugin.BuildPluginManager&lt;/role>
	 * 			&lt;role-hint />
	 * 			&lt;field-name>pluginManager&lt;/field-name>
	 * 		&lt;/requirement>
	 * 	&lt;/requirements>
	 * &lt;/component>
	 * </pre>
	 * 
	 * @parameter
	 */
    @Parameter
    protected Properties configuration = defaultConfiguration();

    private static Properties defaultConfiguration() {
        Properties defaults = new Properties();
        defaults.setProperty("cloneProjectsTo", "${bw.it.projects.run.clone}");
        defaults.setProperty("goals", "${bw.it.projects.run.goals}");
        defaults.setProperty("localRepositoryPath", "${bw.it.local.repository.path}");
        defaults.setProperty("pomIncludes", "${bw.it.projects.run.pomIncludes}");
        defaults.setProperty("profiles", "${bw.it.projects.run.profile}");
        defaults.setProperty("projectsDirectory", "${bw.it.projects.run.directory}");
        defaults.setProperty("properties", "${bw.it.projects.run.properties}");
        defaults.setProperty("streamLogs", "true");
        defaults.setProperty("skipInvocation", "${bw.it.skip}");
        return defaults;
    }

    @Override
    protected Properties getConfiguration() {
        Properties defaults = defaultConfiguration();
        if (configuration == null) {
            return defaults;
        }

        Properties merged = new Properties();
        merged.putAll(defaults);
        merged.putAll(configuration);
        return merged;
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

}
