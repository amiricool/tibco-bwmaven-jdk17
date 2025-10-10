package fr.fastconnect.factory.tibco.bw.maven.artifact;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.artifact.handler.DefaultArtifactHandler;

@Named("bw-ear-deploy")
@Singleton
public class BWEarDeployArtifactHandler extends DefaultArtifactHandler {

    public BWEarDeployArtifactHandler() {
        super("bw-ear-deploy");
        setExtension("ear");
    }
}