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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import fr.fastconnect.factory.tibco.bw.maven.AbstractBWMojo;
import fr.fastconnect.factory.tibco.bw.maven.compile.ArchiveBuilder;
import fr.fastconnect.factory.tibco.bw.maven.source.POMManager;

import javax.inject.Inject;

/**
 * <p>
 * This goal copies the TIBCO BusinessWorks project sources to a temporary
 * folder.<br />
 * These sources will be used to compile a TIBCO BusinessWorks EAR or Projlib
 * from a fresh copy and with potential machine-generated code (for instance
 * for Java Custom Functions).
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
 * @goal copy-bw-sources
 * @inheritByDefault true
 * @requiresProject true
 * @aggregator true
 * @requiresDependencyResolution test
 *
 * @author Mathieu Debove
 * @author Amir Marzouk
 * 
 */
@Mojo(name = "copy-bw-sources", aggregator = true, requiresProject = true,
        requiresDependencyResolution = ResolutionScope.TEST)
public class CopyBWSourcesMojo extends AbstractWrapperForBuiltinMojo<Resource> {

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

    @Parameter(property = "version", defaultValue = "${maven.resources.plugin.version}")
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

    private static Properties defaultConfiguration() {
        Properties defaults = new Properties();
        defaults.setProperty("outputDirectory", "${project.build.directory}/src");
        return defaults;
    }

    private static List<Resource> defaultResources() {
        Resource resource = new Resource();
        resource.setDirectory("${bw.project.location}");
        resource.setFiltering(true);
        resource.addExclude("**/*TestSuite/");

        resource.addExclude("**/*.class");
        resource.addExclude("**/*.jar");
        resource.addExclude("**/*.zip");

        List<Resource> defaults = new ArrayList<Resource>();
        defaults.add(resource);
        return defaults;
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

    @Parameter(defaultValue = "${plugin}", readonly = true)
    private PluginDescriptor pluginDescriptor;

    @Parameter(property = "bw.container.merged.enterprise.archive.name", defaultValue = "${project.artifactId}")
    private String enterpriseArchiveName;

    @Parameter(property = "bw.container.merged.process.archive.name", defaultValue = "${project.artifactId}")
    private String processArchiveName;

	@Override
	protected List<Resource> getResources() {
		List<Resource> result = new ArrayList<Resource>();
		if (resources != null) {
			result.addAll(resources);
		}
		
		if (isContainerEnabled(getProject())) {
			result.clear(); // ignore configuration from Plexus 'components.xml'

			getLog().debug(getProject().getProperties().toString());
			getLog().debug(getProject().getProperties().getProperty("project.build.directory.src"));
			File buildSrcDirectory = new File(getProject().getProperties().getProperty("project.build.directory.src"));
			buildSrcDirectory.mkdirs(); // create "target/src" directory

			// define a ".archive" file to merge all ".archive" found in other projects
			String bwProjectArchiveBuilder = getProject().getProperties().getProperty("bw.project.archive.builder");
			File bwProjectArchiveMerged = new File(buildSrcDirectory.getAbsolutePath() + File.separator + bwProjectArchiveBuilder);
			getLog().debug(".archive: " + bwProjectArchiveMerged.getAbsolutePath());

			// create an empty Archive Builder (".archive" file)
			ArchiveBuilder mergedArchiveBuilder = new ArchiveBuilder();
			
			List<MavenProject> projectsToAggregate = new ArrayList<MavenProject>();
			
			MavenProject aggregator = getProject().getParent();
			@SuppressWarnings("unchecked")
			List<String> modules = aggregator.getModules();

			for (String module : modules) {
				getLog().debug(module);
				String pom = aggregator.getBasedir() + File.separator + module + File.separator + "pom.xml";
				File pomFile = new File(pom);
				
				try {
					projectsToAggregate.add(new MavenProject(POMManager.getModelFromPOM(pomFile, getLog())));
				} catch (Exception e) {
					getLog().debug("Unable to add project from module: " + module);
				}
			}

			List<MavenProject> projects = new ArrayList<MavenProject>();
			projects.addAll(getSession().getProjects());
			
			for (Iterator<MavenProject> it = projects.iterator(); it.hasNext();) {
				MavenProject p = (MavenProject) it.next();
				if (!isProjectToAggregate(p, projectsToAggregate)) {
					it.remove();
				}
			}

			if (projects.size() > 0) {
				for (MavenProject p : projects) {
					if (p.getPackaging().equals(AbstractBWMojo.BWEAR_TYPE) && !isContainerEnabled(p)) {
						// initialize project information
						String basedir = p.getBasedir().getAbsolutePath();
						String bwProjectLocation = p.getProperties().getProperty("bw.project.location");
						bwProjectArchiveBuilder = p.getProperties().getProperty("bw.project.archive.builder"); // the ".archive" of the project
						getLog().debug(basedir);
						getLog().debug(bwProjectLocation);
						
						File bwProjectArchive = new File(basedir + File.separator + bwProjectLocation + File.separator + bwProjectArchiveBuilder);
						getLog().debug(bwProjectArchive.getAbsolutePath());
						//
						
						mergedArchiveBuilder.merge(bwProjectArchive);
	
						// add sources from the project to the container sources
						File srcDirectory = new File(basedir + File.separator + bwProjectLocation);
						result.add(addResource(srcDirectory));
					}
				}
	
				mergedArchiveBuilder.setSharedArchiveAuthor(pluginDescriptor.getArtifactId());
				mergedArchiveBuilder.setEnterpriseArchiveAuthor(pluginDescriptor.getArtifactId());
				mergedArchiveBuilder.setEnterpriseArchiveFileLocationProperty(this.getProject().getArtifactId() + AbstractBWMojo.BWEAR_EXTENSION);
				mergedArchiveBuilder.setEnterpriseArchiveName(enterpriseArchiveName);
				mergedArchiveBuilder.setFirstProcessArchiveName(processArchiveName);
				mergedArchiveBuilder.removeDuplicateProcesses();
				mergedArchiveBuilder.save(bwProjectArchiveMerged);
			}
		}

		return result;
	}

	private boolean isProjectToAggregate(MavenProject project,	List<MavenProject> projectsToAggregate) {
		if (project == null) {
			return false;
		}
		for (MavenProject p : projectsToAggregate) {
			if (project.equals(p)) {
				return true;
			}
		}
		return false;
	}

	private Resource addResource(File srcDirectory) {
		Resource r = new Resource();

		r.setDirectory(srcDirectory.getAbsolutePath());
		r.setFiltering(true);
		r.addExclude("**/*TestSuite/"); // exclude FCUnit TestSuites

		return r;
	}

	private boolean isContainerEnabled(MavenProject p) {
		if (p == null) {
			return false;
		}

		String isContainer = p.getProperties().getProperty("bw.container");
		return (isContainer != null && isContainer.equals("true"));
	}

}
