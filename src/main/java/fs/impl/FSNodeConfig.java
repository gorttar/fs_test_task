/*
 * Copyright (c) 2016 Andrey Antipov. All Rights Reserved.
 */
package fs.impl;

import static java.util.Objects.requireNonNull;

import javax.annotation.Nonnull;

/**
 * @author Andrey Antipov (gorttar@gmail.com) (2016-12-26)
 */
final class FSNodeConfig {
    private FSNodeConfig() {
    }

    static FSNode createFileUnder(@Nonnull String name, @Nonnull FSNode parent) {
        return new FSNodeImpl.File(requireNonNull(name), requireNonNull(parent));
    }

    static FSNode createDirUnder(@Nonnull String name, @Nonnull FSNode parent) {
        return new FSNodeImpl.Directory(requireNonNull(name), requireNonNull(parent));
    }

    static FSNode createRoot() {
        return new FSNodeImpl.Root();
    }
}