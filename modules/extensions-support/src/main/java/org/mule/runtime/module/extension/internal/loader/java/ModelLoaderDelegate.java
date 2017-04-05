package org.mule.runtime.module.extension.internal.loader.java;

import org.mule.runtime.api.meta.model.declaration.fluent.ExtensionDeclarer;
import org.mule.runtime.extension.api.loader.ExtensionLoadingContext;

public interface ModelLoaderDelegate {

  ExtensionDeclarer declare(ExtensionLoadingContext context);
}
