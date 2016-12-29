/*
 * Copyright (c) 2016 Andrey Antipov. All Rights Reserved.
 */
package fs.impl;

import fs.FS;

/**
 * {@link FS} config intended to separate interface from actual implementation
 * * implementor should modify it in order to switch between implementations
 *
 * @author Andrey Antipov (gorttar@gmail.com) (2016-12-29)
 */
public final class FSConfig {
    private FSConfig() {
    }

    public static FS init(long size) {
        return new SimpleFSImpl(size);
    }
}