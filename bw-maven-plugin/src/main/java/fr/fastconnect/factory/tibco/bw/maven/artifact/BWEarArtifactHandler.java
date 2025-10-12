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
package fr.fastconnect.factory.tibco.bw.maven.artifact;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.artifact.handler.DefaultArtifactHandler;
/*
 * @author Amir Marzouk
 *
 */
@Named("bw-ear")
@Singleton
public class BWEarArtifactHandler extends DefaultArtifactHandler {

    public BWEarArtifactHandler() {
        super("bw-ear");
        setExtension("ear");
    }
}