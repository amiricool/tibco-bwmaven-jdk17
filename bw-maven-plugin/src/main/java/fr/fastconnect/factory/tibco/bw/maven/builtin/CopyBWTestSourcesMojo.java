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

import java.util.ArrayList;
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
 * This goal copies the TIBCO BusinessWorks project sources to a temporary
 * folder for the 'test' scope.<br />
 * These sources will be used to run the tests (see 
 * <a href="./bw-test-mojo.html">bw:bw-test</a>).
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
 * <b>org.apache.maven.plugins:maven-resources-plugin:copy-resources</b>
 * </p>
 * 
 * @goal copy-bw-test-sources
 * @inheritByDefault true
 * @requiresProject true
 * @aggregator true
 * @requiresDependencyResolution test
 *
 * @author Mathieu Debove
 * @author Amir Marzouk
 * 
 */
@Mojo(name = "copy-bw-test-sources", aggregator = true, requiresProject = true,
        requiresDependencyResolution = ResolutionScope.TEST)
public class CopyBWTestSourcesMojo extends AbstractWrapperForBuiltinMojo<Resource> {

    @Parameter(property = "groupId", defaultValue = "org.apache.maven.plugins")
    protected String groupId;

	@Override
	protected String getGroupId() {
		return groupId;
	}

    @Parameter(property = "artifactId", defaultValue = "maven-resources-plugin")
    protected String artifactId;

	@Override
	protected String getArtifactId() {
		return artifactId;
	}

    @Parameter(property = "maven.resources.plugin.version", defaultValue = "3.3.1", required = true)
    protected String version;

	@Override
	protected String getVersion() {
		return version;
	}

    @Parameter(property = "goal", defaultValue = "copy-resources")
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

    @Parameter
    protected Properties configuration = defaultConfiguration();
	
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
    protected List<Resource> resources = defaultResources();
	
    @Override
	protected List<Resource> getResources() {
		return resources;
	}

    private static Properties defaultConfiguration() {
        Properties defaults = new Properties();
        defaults.setProperty("outputDirectory", "${project.build.test.directory.src}");
        return defaults;
    }

    private static List<Resource> defaultResources() {
        Resource resource = new Resource();
        resource.setDirectory("${bw.project.location}");
        resource.setFiltering(true);

        resource.addExclude("**/*.class");
        resource.addExclude("**/*.jar");
        resource.addExclude("**/*.zip");

        List<Resource> defaults = new ArrayList<Resource>();
        defaults.add(resource);
        return defaults;

    }
}
