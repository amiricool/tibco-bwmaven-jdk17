package fr.fastconnect.factory.tibco.bw.maven.lifecycle;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

@Named("projlib")
@Singleton
public class ProjlibLifecycleMapping extends AbstractBWLifecycleMapping {

    private static Map<String, List<String>> lifecycle() {
        Map<String, List<String>> phases = new LinkedHashMap<>();
        phases.put("generate-sources", List.of("fr.fastconnect.factory.tibco.bw.maven:bw-maven-plugin:copy-bw-sources"));
        phases.put("generate-resources", List.of("fr.fastconnect.factory.tibco.bw.maven:bw-maven-plugin:copy-bw-dependencies"));
        phases.put("process-resources", List.of("fr.fastconnect.factory.tibco.bw.maven:bw-maven-plugin:resolve-bw-dependencies"));
        phases.put("compile", List.of(
                "fr.fastconnect.factory.tibco.bw.maven:bw-maven-plugin:update-alias-lib",
                "fr.fastconnect.factory.tibco.bw.maven:bw-maven-plugin:compile-projlib"));
        phases.put("generate-test-sources", List.of("fr.fastconnect.factory.tibco.bw.maven:bw-maven-plugin:copy-bw-test-sources"));
        phases.put("generate-test-resources", List.of("fr.fastconnect.factory.tibco.bw.maven:bw-maven-plugin:copy-bw-test-dependencies"));
        phases.put("process-test-resources", List.of("fr.fastconnect.factory.tibco.bw.maven:bw-maven-plugin:resolve-bw-test-dependencies"));
        phases.put("install", List.of("org.apache.maven.plugins:maven-install-plugin:install"));
        phases.put("deploy", List.of("org.apache.maven.plugins:maven-deploy-plugin:deploy"));
        return phases;
    }

    public ProjlibLifecycleMapping() {
        super(lifecycle());
    }
}