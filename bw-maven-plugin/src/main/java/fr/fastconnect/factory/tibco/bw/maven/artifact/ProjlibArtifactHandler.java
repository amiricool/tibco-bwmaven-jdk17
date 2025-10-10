package fr.fastconnect.factory.tibco.bw.maven.artifact;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.artifact.handler.DefaultArtifactHandler;

@Named("projlib")
@Singleton
public class ProjlibArtifactHandler extends DefaultArtifactHandler {

    public ProjlibArtifactHandler() {
        super("projlib");
        setExtension("projlib");
    }
}