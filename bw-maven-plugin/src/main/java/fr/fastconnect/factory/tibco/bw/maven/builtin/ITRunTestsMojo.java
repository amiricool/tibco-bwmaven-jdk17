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

    @Parameter(property = "groupId", defaultValue = "org.apache.maven.plugins")
    protected String groupId;
	
	@Override
	protected String getGroupId() {
		return groupId;
	}

    @Parameter(property = "artifactId", defaultValue = "maven-invoker-plugin")
    protected String artifactId;
	
	@Override
	protected String getArtifactId() {
		return artifactId;
	}

    @Parameter(property = "version", defaultValue = "${maven.invoker.plugin.version}", required = true)
    protected String version;
	
	@Override
	protected String getVersion() {
		return version;
	}

    @Parameter(property = "goal", defaultValue = "run")
    protected String goal;
	
	@Override
	protected String getGoal() {
		return goal;
	}

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

	@Override
	protected MavenProject getProject() {
		return project;
	}

    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    protected MavenSession session;

	@Override
	protected MavenSession getSession() {
		return session;
	}

    @Inject
	protected BuildPluginManager pluginManager;
	
	@Override
	protected BuildPluginManager getPluginManager() {
		return pluginManager;
	}

    @Parameter(property = "bw.it.skip", defaultValue = "${bw.it.skip}")
    protected boolean skipInvocation;

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

    @Parameter
    protected List<Resource> resources;
    
	@Override
	protected List<Resource> getResources() {
		return resources;
	}

}
