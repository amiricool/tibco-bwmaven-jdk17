package fr.fastconnect.factory.tibco.bw.maven.lifecycle;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

@Named("pom")
@Singleton
public class PomLifecycleMapping extends AbstractBWLifecycleMapping {

    private static Map<String, List<String>> lifecycle() {
        Map<String, List<String>> phases = new LinkedHashMap<>();
        phases.put("prepare-package", List.of("fr.fastconnect.factory.tibco.bw.maven:bw-maven-plugin:generate-deployment-root-pom"));
        phases.put("install", List.of("org.apache.maven.plugins:maven-install-plugin:install"));
        phases.put("deploy", List.of("org.apache.maven.plugins:maven-deploy-plugin:deploy"));
        return phases;
    }

    public PomLifecycleMapping() {
        super(lifecycle());
    }
}