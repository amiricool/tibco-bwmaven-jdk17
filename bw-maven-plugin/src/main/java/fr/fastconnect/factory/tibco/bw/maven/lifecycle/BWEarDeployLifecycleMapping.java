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