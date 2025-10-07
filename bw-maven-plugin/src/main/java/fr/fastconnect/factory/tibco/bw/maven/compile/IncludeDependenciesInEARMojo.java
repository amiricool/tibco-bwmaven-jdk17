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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.jaxen.JaxenException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;

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
     * @throws JaxenException
     */
    private ArchiveContents currentEarArchive;
    private ArchiveContents currentLibArchive;

    private void copyRuntimeJARsInEAR(File ear) throws IOException, JDOMException {
        Path earPath = ear.toPath();
        byte[] earBytes = Files.readAllBytes(earPath);

        try {
            currentEarArchive = readZipArchive(earBytes);

            byte[] libZip = currentEarArchive.files.get("lib.zip");
            if (libZip == null) {
                throw new IOException("Unable to locate lib.zip inside EAR archive");
            }

            currentLibArchive = readZipArchive(libZip);
            currentLibArchive.directories.add("WEB-INF/");
            currentLibArchive.directories.add("WEB-INF/lib/");

            for (Dependency dependency : this.getJarDependencies()) {
                String jarName = getJarName(dependency, false);
                Path source = new File(buildLibDirectory, jarName).toPath();
                if (!Files.exists(source)) {
                    throw new IOException("Unable to locate dependency JAR: " + source);
                }
                byte[] jarContent = Files.readAllBytes(source);
                currentLibArchive.files.put("WEB-INF/lib/" + jarName, jarContent);
            }

            if (removeVersionFromFileNames) {
                removeVersionFromFileNames(ear);
            }

            currentEarArchive.files.put("lib.zip", writeZipArchive(currentLibArchive));



            Files.write(earPath, writeZipArchive(currentEarArchive));
        } catch (JaxenException e) {
            throw new JDOMException("Failed to update EAR aliases", e);
        } finally {
            currentEarArchive = null;
            currentLibArchive = null;
        }
    }

    private static final class ArchiveContents {
        private final Map<String, byte[]> files = new LinkedHashMap<>();
        private final Set<String> directories = new LinkedHashSet<>();
    }

    private ArchiveContents readZipArchive(byte[] archiveBytes) throws IOException {
        ArchiveContents contents = new ArchiveContents();
        try (ZipArchiveInputStream input = new ZipArchiveInputStream(new ByteArrayInputStream(archiveBytes))) {
            ZipArchiveEntry entry;
            while ((entry = input.getNextZipEntry()) != null) {
                System.out.println("Entry :"+entry.getName());
                String name = entry.getName();
                if (entry.isDirectory()) {
                    contents.directories.add(ensureDirectoryName(name));
                    continue;
                }
                contents.files.put(name, readEntryBytes(input));
            }
        }
        return contents;
    }

    private byte[] writeZipArchive(ArchiveContents contents) throws IOException {
        return writeZipArchive(contents.directories, contents.files);
    }

    private void removeVersionFromFileNames(File ear) throws IOException, JDOMException, JaxenException {
        if (currentLibArchive == null ) {
            getLog().info("Nothing to remove in the EAR archive");
            return;
        }

        for (Dependency dependency : this.getJarDependencies()) {
            Pattern p = Pattern.compile("(.*)-" + Pattern.quote(dependency.getVersion()) + JAR_EXTENSION);

            String includeOrigin = getJarName(dependency, false);
            Matcher matcher = p.matcher(includeOrigin);
            if (matcher.matches()) {
                String includeDestination = matcher.group(1) + JAR_EXTENSION;
                String originEntry = "WEB-INF/lib/" + includeOrigin;
                String destinationEntry = "WEB-INF/lib/" + includeDestination;
                byte[] originBytes = currentLibArchive.files.remove(originEntry);
                if (originBytes != null) {
                    currentLibArchive.files.put(destinationEntry, originBytes);
                    updateAlias(includeOrigin, includeDestination, ear);

                } else {
                    getLog().error("Unable to find jar " + includeOrigin + " in WEB-INF/lib/");
                }
            }
        }
    }

    private void updateAlias(String includeOrigin, String includeDestination, File ear) throws JDOMException, IOException, JaxenException {
        if (currentEarArchive == null) {
            return;
        }

        byte[] tibcoXml = currentEarArchive.files.get("TIBCO.xml");
        if (tibcoXml != null) {
            byte[] updatedTibco = updateTibcoXml(tibcoXml, includeOrigin, includeDestination);
            if (updatedTibco != null) {
                currentEarArchive.files.put("TIBCO.xml", updatedTibco);
            }
        } else {
            getLog().error("Unable to find TIBCO.xml in ear archive " + ear.getAbsolutePath());
        }

        updateAliasInPARs(includeOrigin, includeDestination, ear);
    }

    private void updateAliasInPARs(String includeOrigin, String includeDestination, File ear) throws IOException, JDOMException {

        Map<String, byte[]> updatedPars = new LinkedHashMap<>();
        for (Map.Entry<String, byte[]> entry : currentEarArchive.files.entrySet()) {
            if (entry.getKey().endsWith(".par")) {
                try {
                    byte[] updatedPar = updateParArchive(entry.getValue(), includeOrigin, includeDestination);
                    if (updatedPar != null) {
                        updatedPars.put(entry.getKey(), updatedPar);
                    }
                } catch (JaxenException e) {
                    throw new JDOMException("Failed to update alias in PAR " + entry.getKey(), e);
                }
            }
        }

        updatedPars.forEach(currentEarArchive.files::put);
    }

    private byte[] updateTibcoXml(byte[] tibcoBytes, String includeOrigin, String includeDestination) throws JDOMException, IOException, JaxenException {
        SAXBuilder sxb = new SAXBuilder();
        Document document = sxb.build(new ByteArrayInputStream(tibcoBytes));

        XPath xpa = XPath.newInstance("//dd:NameValuePairs/dd:NameValuePair[starts-with(dd:name, 'tibco.alias') and dd:value='" + includeOrigin + "']/dd:value");
        xpa.addNamespace("dd", "http://www.tibco.com/xmlns/dd");

        Element singleNode = (Element) xpa.selectSingleNode(document);
        if (singleNode == null) {
            return null;
        }

        singleNode.setText(includeDestination);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        XMLOutputter xmlOutput = new XMLOutputter();
        xmlOutput.setFormat(Format.getPrettyFormat().setIndent("    "));
        xmlOutput.output(document, output);
        return output.toByteArray();
    }

    private byte[] updateParArchive(byte[] parBytes, String includeOrigin, String includeDestination) throws IOException, JDOMException, JaxenException {
        ArchiveContents contents = readZipArchive(parBytes);

        byte[] tibcoXml = contents.files.get("TIBCO.xml");
        if (tibcoXml == null) {
            getLog().error("Unable to find TIBCO.xml in par archive");
            return null;
        }

        byte[] updatedTibco = updateParDependency(tibcoXml, includeOrigin, includeDestination);
        if (updatedTibco == null) {
            return null;
        }

        contents.files.put("TIBCO.xml", updatedTibco);
        return writeZipArchive(contents);
    }

    private byte[] updateParDependency(byte[] tibcoXml, String includeOrigin, String includeDestination) throws JDOMException, IOException, JaxenException {
        SAXBuilder sxb = new SAXBuilder();
        Document document = sxb.build(new ByteArrayInputStream(tibcoXml));

        XPath xpa = XPath.newInstance("//dd:NameValuePairs/dd:NameValuePair[dd:name='EXTERNAL_JAR_DEPENDENCY']/dd:value");
        xpa.addNamespace("dd", "http://www.tibco.com/xmlns/dd");

        Element singleNode = (Element) xpa.selectSingleNode(document);
        if (singleNode == null) {
            return null;
        }

        String value = singleNode.getText();
        if (!value.contains(includeOrigin)) {
            return null;
        }

        String updatedValue = value.replace(includeOrigin, includeDestination);
        if (updatedValue.equals(value)) {
            return null;
        }

        singleNode.setText(updatedValue);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        XMLOutputter xmlOutput = new XMLOutputter();
        xmlOutput.setFormat(Format.getPrettyFormat().setIndent("    "));
        xmlOutput.output(document, output);
        return output.toByteArray();
    }

    private byte[] readEntryBytes(InputStream input) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int read;
        while ((read = input.read(data)) != -1) {
            buffer.write(data, 0, read);
        }
        return buffer.toByteArray();
    }

    private String ensureDirectoryName(String name) {
        String normalized = name.replace('\\', '/');
        if (!normalized.endsWith("/")) {
            normalized += "/";
        }
        return normalized;
    }

    private int directoryDepth(String directory) {
        int depth = 0;
        for (int i = 0; i < directory.length(); i++) {
            if (directory.charAt(i) == '/') {
                depth++;
            }
        }
        return depth;
    }

    private byte[] writeZipArchive(Set<String> directories, Map<String, byte[]> files) throws IOException {
        Set<String> allDirectories = new LinkedHashSet<>();
        for (String directory : directories) {
            allDirectories.add(ensureDirectoryName(directory));
        }
        for (String fileName : files.keySet()) {
            int index = fileName.lastIndexOf('/');
            while (index > 0) {
                allDirectories.add(fileName.substring(0, index + 1));
                index = fileName.lastIndexOf('/', index - 1);
            }
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ZipArchiveOutputStream zipOutput = new ZipArchiveOutputStream(output)) {
            List<String> orderedDirectories = new ArrayList<>(allDirectories);
            orderedDirectories.sort((a, b) -> {
                int depthCompare = Integer.compare(directoryDepth(a), directoryDepth(b));
                if (depthCompare != 0) {
                    return depthCompare;
                }
                return a.compareTo(b);
            });

            for (String directory : orderedDirectories) {
                ZipArchiveEntry entry = new ZipArchiveEntry(directory);
                zipOutput.putArchiveEntry(entry);
                zipOutput.closeArchiveEntry();
            }

            for (Map.Entry<String, byte[]> fileEntry : files.entrySet()) {
                ZipArchiveEntry entry = new ZipArchiveEntry(fileEntry.getKey());
                zipOutput.putArchiveEntry(entry);
                zipOutput.write(fileEntry.getValue());
                zipOutput.closeArchiveEntry();
            }

            zipOutput.finish();
        }

        return output.toByteArray();
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
        currentEarArchive = null;
        currentLibArchive = null;
        getLog().debug("Using EAR : " + ear.getAbsolutePath());

        try {
            this.copyRuntimeJARsInEAR(ear);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (JDOMException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

}