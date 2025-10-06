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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class IncludeDependenciesInEARMojoVfsTest {

    private StandardFileSystemManager manager;

    @Before
    public void setUp() throws Exception {
        manager = new StandardFileSystemManager();
        manager.init();
    }

    @After
    public void tearDown() {
        if (manager != null) {
            manager.close();
        }
    }

    @Test
    public void nestedZipMustBeAccessedThroughDedicatedFileSystem() throws Exception {
        Path tempDir = Files.createTempDirectory("ear");
        File ear = createEarWithNestedLib(tempDir);

        FileObject earRoot = manager.resolveFile("zip:" + ear.toURI().toString());

        FileObject incorrectLibDirectory = earRoot.resolveFile("zip:lib.zip!/WEB-INF/lib");
        assertFalse("VFS should not resolve nested zips via a zip: prefix relative to the parent archive", incorrectLibDirectory.exists());

        FileObject libZip = earRoot.resolveFile("lib.zip");
        FileObject libFileSystem = manager.createFileSystem("zip", libZip);
        FileObject libDirectory = libFileSystem.resolveFile("WEB-INF/lib");
        assertTrue("lib.zip!/WEB-INF/lib should exist when addressed through a dedicated zip filesystem", libDirectory.exists());
    }

    private File createEarWithNestedLib(Path tempDir) throws IOException {
        Path libZip = tempDir.resolve("lib.zip");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(libZip))) {
            zos.putNextEntry(new ZipEntry("WEB-INF/lib/"));
            zos.closeEntry();
            zos.putNextEntry(new ZipEntry("WEB-INF/lib/dummy.txt"));
            zos.write("dummy".getBytes());
            zos.closeEntry();
        }

        Path ear = tempDir.resolve("test.ear");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(ear))) {
            zos.putNextEntry(new ZipEntry("lib.zip"));
            try (InputStream in = Files.newInputStream(libZip)) {
                IOUtils.copy(in, zos);
            }
            zos.closeEntry();
        }

        return ear.toFile();
    }
}
