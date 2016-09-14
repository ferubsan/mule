/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.deployment.internal.descriptor;

import static org.mule.runtime.core.MuleServer.DEFAULT_CONFIGURATION;

import org.mule.runtime.core.MuleServer;

import java.io.File;

/**
 * Encapsulates defaults when no explicit descriptor provided with an app.
 */
public class EmptyApplicationDescriptor extends ApplicationDescriptor {

  public EmptyApplicationDescriptor(File appLocation) {
    super(appLocation.getName());
    setConfigResources(new String[] {MuleServer.DEFAULT_CONFIGURATION});
    File configPathFile = new File(appLocation, DEFAULT_CONFIGURATION);
    setArtifactLocation(appLocation);
    setRootFolder(appLocation.getParentFile());
    String configPath = String.format(configPathFile.getAbsolutePath());
    setAbsoluteResourcePaths(new String[] {configPath});
    setConfigResourcesFile(new File[] {configPathFile});
  }
}
