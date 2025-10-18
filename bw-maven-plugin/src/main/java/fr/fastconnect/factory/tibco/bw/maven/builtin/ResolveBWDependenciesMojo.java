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
 * This goal resolves TIBCO BusinessWorks dependencies (especially Projlibs)
 * defined in the POM of the project.<br/>
 * The resolution is transitive and follows the Maven standard.
 * </p>
 * 
 * <p>
 * These dependencies are then copied by
 * <a href="./copy-bw-dependencies-mojo.html">copy-bw-dependencies</a>.
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
 * <b>org.apache.maven.plugins:maven-dependency-plugin:list</b>
 * </p>
 * 
 * @goal resolve-bw-dependencies
 * @inheritByDefault true
 * @requiresProject true
 * @aggregator true
 * @requiresDependencyResolution test
 *
 * @author Mathieu Debove
 * @author Amir Marzouk
 * 
 */
@Mojo(name = "resolve-bw-dependencies", aggregator = true, requiresProject = true,
        requiresDependencyResolution = ResolutionScope.TEST)
public class ResolveBWDependenciesMojo extends AbstractWrapperForBuiltinMojo<Resource> {

    @Parameter(property = "groupId", defaultValue = "org.apache.maven.plugins")
    protected String groupId;

	@Override
	protected String getGroupId() {
		// TODO Auto-generated method stub
		return groupId;
	}

    @Parameter(property = "artifactId", defaultValue = "maven-dependency-plugin")
    protected String artifactId;

	@Override
	protected String getArtifactId() {
		// TODO Auto-generated method stub
		return artifactId;
	}

    @Parameter(property = "version", defaultValue = "${maven.dependency.plugin.version}")
    protected String version;

	@Override
	protected String getVersion() {
		// TODO Auto-generated method stub
		return version;
	}

    @Parameter(property = "goal", defaultValue = "list")
    protected String goal;

	@Override
	protected String getGoal() {
		// TODO Auto-generated method stub
		return goal;
	}

    private static Properties defaultConfiguration() {
        Properties defaults = new Properties();
        defaults.setProperty("outputFile", "${project.build.directory}/resolved");
        defaults.setProperty("includeTypes", "projlib,jar");
        defaults.setProperty("includeScope", "runtime");
        return defaults;
    }

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

	@Override
	protected MavenProject getProject() {
		// TODO Auto-generated method stub
		return project;
	}

    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    protected MavenSession session;

	@Override
	protected MavenSession getSession() {
		// TODO Auto-generated method stub
		return session;
	}

    @Inject
    protected BuildPluginManager pluginManager;
	
	@Override
	protected BuildPluginManager getPluginManager() {
		// TODO Auto-generated method stub
		return pluginManager;
	}

    @Parameter
    protected Properties configuration = defaultConfiguration();

    @Override
    protected Properties getConfiguration() {
        Properties defaults = defaultConfiguration();

        if (configuration == null || configuration.isEmpty()) {
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
		// TODO Auto-generated method stub
		return resources;
	}

}
