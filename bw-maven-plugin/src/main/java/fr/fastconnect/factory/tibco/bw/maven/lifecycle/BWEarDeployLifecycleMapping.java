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

@Named("bw-ear-deploy")
@Singleton
public class BWEarDeployLifecycleMapping extends AbstractBWLifecycleMapping {

    private static Map<String, List<String>> lifecycle() {
        Map<String, List<String>> phases = new LinkedHashMap<>();
        phases.put("prepare-package", List.of("fr.fastconnect.factory.tibco.bw.maven:bw-maven-plugin:generate-properties-from-xml"));
        phases.put("package", List.of(
                "fr.fastconnect.factory.tibco.bw.maven:bw-maven-plugin:merge-properties",
                "fr.fastconnect.factory.tibco.bw.maven:bw-maven-plugin:generate-xml-from-properties"));
        phases.put("deploy", List.of("fr.fastconnect.factory.tibco.bw.maven:bw-maven-plugin:deploy-bw"));
        return phases;
    }

    public BWEarDeployLifecycleMapping() {
        super(lifecycle());
    }
}