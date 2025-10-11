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
package fr.fastconnect.factory.tibco.bw.maven.lifecycle;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

@Named("bw-ear")
@Singleton
public class BWEarLifecycleMapping extends AbstractBWLifecycleMapping {

    private static Map<String, List<String>> lifecycle() {
        Map<String, List<String>> phases = new LinkedHashMap<>();
        phases.put("initialize", List.of("fr.fastconnect.factory.tibco.bw.maven:bw-maven-plugin:initialize"));
        phases.put("generate-sources", List.of("fr.fastconnect.factory.tibco.bw.maven:bw-maven-plugin:copy-bw-sources"));
        phases.put("generate-resources", List.of("fr.fastconnect.factory.tibco.bw.maven:bw-maven-plugin:copy-bw-dependencies"));
        phases.put("process-resources", List.of("fr.fastconnect.factory.tibco.bw.maven:bw-maven-plugin:resolve-bw-dependencies"));
        phases.put("compile", List.of(
                "fr.fastconnect.factory.tibco.bw.maven:bw-maven-plugin:update-alias-lib",
                "fr.fastconnect.factory.tibco.bw.maven:bw-maven-plugin:compile-bw-ear",
                "fr.fastconnect.factory.tibco.bw.maven:bw-maven-plugin:include-dependencies-in-bw-ear"));
        phases.put("generate-test-sources", List.of("fr.fastconnect.factory.tibco.bw.maven:bw-maven-plugin:copy-bw-test-sources"));
        phases.put("generate-test-resources", List.of("fr.fastconnect.factory.tibco.bw.maven:bw-maven-plugin:copy-bw-test-dependencies"));
        phases.put("process-test-resources", List.of("fr.fastconnect.factory.tibco.bw.maven:bw-maven-plugin:resolve-bw-test-dependencies"));
        phases.put("prepare-package", List.of(
                "fr.fastconnect.factory.tibco.bw.maven:bw-maven-plugin:extract-xml-from-ear",
                "fr.fastconnect.factory.tibco.bw.maven:bw-maven-plugin:generate-properties-from-xml",
                "fr.fastconnect.factory.tibco.bw.maven:bw-maven-plugin:generate-deployment-pom",
                "fr.fastconnect.factory.tibco.bw.maven:bw-maven-plugin:generate-standalone-deployment-pom"));
        phases.put("package", List.of(
                "fr.fastconnect.factory.tibco.bw.maven:bw-maven-plugin:merge-properties",
                "fr.fastconnect.factory.tibco.bw.maven:bw-maven-plugin:generate-xml-from-properties"));
        phases.put("pre-integration-test", List.of("fr.fastconnect.factory.tibco.bw.maven:bw-maven-plugin:it-deploy-bw"));
        phases.put("install", List.of("org.apache.maven.plugins:maven-install-plugin:install"));
        phases.put("deploy", List.of("org.apache.maven.plugins:maven-deploy-plugin:deploy"));
        return phases;
    }

    public BWEarLifecycleMapping() {
        super(lifecycle());
    }
}