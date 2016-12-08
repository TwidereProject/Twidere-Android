package org.mariotaku.twidere.util.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by mariotaku on 2016/12/8.
 */

public final class CountOnlyOutputStream extends OutputStream {
    private int count;

    @Override
    public void write(int i) throws IOException {
        count++;
    }

    public int getCount() {
        return count;
    }
}
