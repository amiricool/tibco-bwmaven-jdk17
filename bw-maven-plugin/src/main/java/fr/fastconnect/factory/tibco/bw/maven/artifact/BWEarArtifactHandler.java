package fr.fastconnect.factory.tibco.bw.maven.artifact;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.artifact.handler.DefaultArtifactHandler;

@Named("bw-ear")
@Singleton
public class BWEarArtifactHandler extends DefaultArtifactHandler {

    public BWEarArtifactHandler() {
        super("bw-ear");
        setExtension("ear");
    }
}