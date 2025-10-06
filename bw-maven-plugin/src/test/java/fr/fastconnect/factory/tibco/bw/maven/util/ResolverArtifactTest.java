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
package fr.fastconnect.factory.tibco.bw.maven.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.junit.Test;

/**
 * Simple regression test ensuring the repository resolver classes exposed by
 * the Maven Resolver API remain available through the configured dependencies.
 */
public class ResolverArtifactTest {

    @Test
    public void defaultArtifactExposesCoordinates() {
        Artifact artifact = new DefaultArtifact("org.apache.maven.plugins", "maven-source-plugin", "jar", "3.3.1");

        assertEquals("org.apache.maven.plugins", artifact.getGroupId());
        assertEquals("maven-source-plugin", artifact.getArtifactId());
        assertEquals("jar", artifact.getExtension());
        assertEquals("", artifact.getClassifier());
        assertEquals("3.3.1", artifact.getVersion());

    }
}