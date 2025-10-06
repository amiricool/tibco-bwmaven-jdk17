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
package fr.fastconnect.factory.tibco.bw.maven.compile;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;


import org.apache.commons.vfs2.util.FileObjectUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import fr.fastconnect.factory.tibco.bw.maven.AbstractBWArtifactMojo;

/**
 * <p>
 * This goal includes the JAR dependencies of the runtime scope inside the
 * "lib.zip" of the TIBCO BusinessWorks EAR.<br/>
 * It allows to load seamlessly the transitive dependencies of JARs of the
 * TIBCO BusinessWorks project and avoid the ClassNotFoundException issues when
 * the application is deployed on a TIBCO domain.
 * </p>
 * <p>
 * This step can be ignored by setting <i>includeTransitiveJARsInEAR</i> to
 * <i>false</i>.
 * </p>
 * 
 * @see UpdateAliasesLibsMojo
 * @author Mathieu Debove
 * 
 */
@Mojo( name="include-dependencies-in-bw-ear",
defaultPhase=LifecyclePhase.COMPILE )
public class IncludeDependenciesInEARMojo extends AbstractBWArtifactMojo {

	/**
	 * Whether to add JARs files inside EAR.
	 */
	@Parameter (property="includeTransitiveJARsInEAR", defaultValue="true")
	public Boolean includeTransitiveJARsInEAR;

	/**
	 * Whether to rename JARs files inside EAR without their version.
	 */
	@Parameter (property="removeVersionFromFileNames", defaultValue="false")
	public Boolean removeVersionFromFileNames;
	
	@Override
	protected String getArtifactFileExtension() {
		return BWEAR_EXTENSION;
	}

    private StandardFileSystemManager fileSystemManager;
    private FileObject earRoot;

    private void closeQuietly(FileObject... resources) {
        if (resources == null) {
            return;
        }
        for (FileObject resource : resources) {
            if (resource != null) {
                try {
                    resource.close();
                } catch (FileSystemException e) {
                    getLog().debug("Unable to close VFS resource", e);
                }
            }
        }
    }

	/**
	 * <p>
	 * This methods copies the transitive JAR dependencies of the project inside
	 * the "WEB-INF/lib" folder of the "lib.zip" subarchive of the TIBCO
	 * BusinessWorks EAR archive.
	 * </p>
	 * 
	 * @param ear, the TIBCO BusinessWorks EAR archive file
	 * @throws IOException 
	 * @throws JDOMException 
	 */
    private void copyRuntimeJARsInEAR(File ear) throws IOException, JDOMException {
        FileObject libDirectory = null;
        try {
            libDirectory = resolveLibDirectory();
            if (libDirectory == null) {
                getLog().error("Unable to resolve libDirectory 'zip:lib.zip!/WEB-INF/lib' in ear directory '" + ear.getAbsolutePath() + "'.");
                return;
            }

            if (!libDirectory.exists()) {
                libDirectory.createFolder();
            }

            for (Dependency dependency : this.getJarDependencies()) {
                String jarName = getJarName(dependency, false);
                File jarFile = new File(buildLibDirectory, jarName);
                if (!jarFile.exists()) {
                    getLog().warn("Unable to locate dependency JAR '" + jarName + "' in build directory '" + buildLibDirectory + "'.");
                    continue;
                }

                FileObject source = null;
                FileObject target = null;
                try {
                    source = fileSystemManager.resolveFile(jarFile.toURI().toString());
                    target = libDirectory.resolveFile(jarName);
                    target.copyFrom(source, Selectors.SELECT_SELF);
                } finally {
                    closeQuietly(target, source);
                }
            }

            if (removeVersionFromFileNames) {
                String outputDirectory = ear.getAbsolutePath() + File.separator + "lib.zip" + File.separator + "WEB-INF" + File.separator + "lib";
                removeVersionFromFileNames(outputDirectory, ear);
            }
        } catch (FileSystemException e) {
            throw new IOException("Unable to copy JAR dependencies into EAR", e);
        } finally {
            closeQuietly(libDirectory);
        }
    }

    private FileObject resolveLibDirectory() throws FileSystemException {
        if (earRoot == null) {
            return null;
        }
        return fileSystemManager.resolveFile(earRoot, "zip:lib.zip!/WEB-INF/lib");
    }


    private void removeVersionFromFileNames(String outputDirectory, File ear) throws IOException, JDOMException {
        for (Dependency dependency : this.getJarDependencies()) {
            Pattern p = Pattern.compile("(.*)-" + dependency.getVersion() + JAR_EXTENSION);

            String includeOrigin = getJarName(dependency, false);
            String includeDestination;

            Matcher m = p.matcher(includeOrigin);
            if (m.matches()) {
                includeDestination = m.group(1)+JAR_EXTENSION;

                renameJarInLib(includeOrigin, includeDestination);

                updateAlias(includeOrigin, includeDestination, ear);
            }
        }
    }

    private void updateAlias(String includeOrigin, String includeDestination, File ear) throws IOException, JDOMException {
        FileObject tibcoXml = null;
        try {
            tibcoXml = earRoot.resolveFile("TIBCO.xml");
            if (!tibcoXml.exists()) {
                getLog().error("Unable to resolve file 'TIBCO.xml' in ear directory '" + ear.getAbsolutePath() + "'.");
                return;
            }

            Path tempFile = Files.createTempFile("tibco", ".xml");
            FileObject tempXml = null;
            try {
                tempXml = fileSystemManager.resolveFile(tempFile.toUri().toString());

                FileObjectUtils.writeContent(tibcoXml, tempXml);

                SAXBuilder sxb = new SAXBuilder();
                Document document = sxb.build(tempFile.toFile());

                Namespace ddNamespace = Namespace.getNamespace("dd", "http://www.tibco.com/xmlns/dd");
                XPathExpression<Element> expression = XPathFactory.instance().compile(
                        "//dd:NameValuePairs/dd:NameValuePair[starts-with(dd:name, 'tibco.alias') and dd:value='" + includeOrigin + "']/dd:value",
                        Filters.element(), null, ddNamespace);

                Element singleNode = expression.evaluateFirst(document);
                if (singleNode != null) {
                    singleNode.setText(includeDestination);
                    XMLOutputter xmlOutput = new XMLOutputter();
                    xmlOutput.setFormat(Format.getPrettyFormat().setIndent("    "));
                    try (Writer writer = Files.newBufferedWriter(tempFile)) {
                        xmlOutput.output(document, writer);
                    }

                    tibcoXml.copyFrom(tempXml, Selectors.SELECT_SELF);
                }

                updateAliasInPARs(includeOrigin, includeDestination, ear);
            } finally {
                closeQuietly(tempXml);
                Files.deleteIfExists(tempFile);
            }
        } catch (FileSystemException e) {
            throw new IOException("Unable to update TIBCO.xml in EAR", e);
        } finally {
            closeQuietly(tibcoXml);
        }
    }

    private void updateAliasInPARs(String includeOrigin, String includeDestination, File ear) throws IOException, JDOMException {
        try {
            for (FileObject child : earRoot.getChildren()) {
                try {
                    if (!"par".equalsIgnoreCase(child.getName().getExtension())) {
                        continue;
                    }

                    FileObject parRoot = null;
                    FileObject tibcoXml = null;
                    try {
                        parRoot = fileSystemManager.resolveFile(child, "zip:");
                        tibcoXml = parRoot.resolveFile("TIBCO.xml");
                        if (!tibcoXml.exists()) {
                            getLog().error("Unable to resolve file 'TIBCO.xml' in par directory '" + child.getPublicURIString() + "'.");
                            continue;
                        }

                        Path tempFile = Files.createTempFile("tibco-par", ".xml");
                        FileObject tempXml = null;
                        try {
                            tempXml = fileSystemManager.resolveFile(tempFile.toUri().toString());

                            FileObjectUtils.writeContent(tibcoXml, tempXml);

                            SAXBuilder sxb = new SAXBuilder();
                            Document document = sxb.build(tempFile.toFile());

                            Namespace ddNamespace = Namespace.getNamespace("dd", "http://www.tibco.com/xmlns/dd");
                            XPathExpression<Element> expression = XPathFactory.instance().compile(
                                    "//dd:NameValuePairs/dd:NameValuePair[dd:name='EXTERNAL_JAR_DEPENDENCY']/dd:value",
                                    Filters.element(), null, ddNamespace);

                            Element singleNode = expression.evaluateFirst(document);
                            if (singleNode != null) {
                                String value = singleNode.getText().replace(includeOrigin, includeDestination);
                                singleNode.setText(value);
                                XMLOutputter xmlOutput = new XMLOutputter();
                                xmlOutput.setFormat(Format.getPrettyFormat().setIndent("    "));
                                try (Writer writer = Files.newBufferedWriter(tempFile)) {
                                    xmlOutput.output(document, writer);
                                }

                                tibcoXml.copyFrom(tempXml, Selectors.SELECT_SELF);
                            }
                        } finally {
                            closeQuietly(tempXml);
                            Files.deleteIfExists(tempFile);
                        }
                    } finally {
                        closeQuietly(tibcoXml, parRoot);
                    }
                } finally {
                    closeQuietly(child);
                }
            }
        } catch (FileSystemException e) {
            throw new IOException("Unable to update TIBCO.xml in PAR archives", e);
        }
    }

	public void execute() throws MojoExecutionException {
    	if (skipCompile || skipEARCompile) {
    		getLog().info(SKIPPING);
    		return;
    	}

		if (isCurrentGoal("bw:launch-designer") || !includeTransitiveJARsInEAR) {
			return; // ignore
		}

		super.execute();

		File ear = getProject().getArtifact().getFile(); // EAR generated by "compile-bw-ear" goal
		if (ear == null) {
			ear = getOutputFile();
		}
		getLog().debug("Using EAR : " + ear.getAbsolutePath());

        fileSystemManager = new StandardFileSystemManager();

        try {
            fileSystemManager.init();
            earRoot = fileSystemManager.resolveFile("zip:" + ear.toURI().toString());
            this.copyRuntimeJARsInEAR(ear);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (JDOMException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } finally {
            closeQuietly(earRoot);
            earRoot = null;
            if (fileSystemManager != null) {
                fileSystemManager.close();
                fileSystemManager = null;
            }
        }
    }


    private void renameJarInLib(String includeOrigin, String includeDestination) throws IOException {
        FileObject libDirectory = null;
        FileObject origin = null;
        FileObject destination = null;
        try {
            libDirectory = resolveLibDirectory();
            if (libDirectory == null || !libDirectory.exists()) {
                getLog().error("Unable to resolve libDirectory 'zip:lib.zip!/WEB-INF/lib' in ear directory '" + earRoot.getPublicURIString() + "'.");
                return;
            }

            origin = libDirectory.resolveFile(includeOrigin);
            if (!origin.exists()) {
                getLog().error("Unable to resolve libDirectory '" + origin.getPublicURIString() + "' in ear directory '" + libDirectory.getPublicURIString() + "'.");
                return;
            }

            destination = libDirectory.resolveFile(includeDestination);
            if (destination.exists()) {
                destination.delete();
            }

            origin.moveTo(destination);
        } catch (FileSystemException e) {
            throw new IOException("Unable to rename dependency JAR inside lib.zip", e);
        } finally {
            closeQuietly(destination, origin, libDirectory);
        }
    }


}
