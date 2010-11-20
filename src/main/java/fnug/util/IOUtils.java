package fnug.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/*
 Copyright 2010 Martin Algesten

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

/**
 * Helper methods for i/o operations.
 * 
 * @author Martin Algesten
 * 
 */
public class IOUtils {

    /**
     * Recursively remove the given file.
     * 
     * @param cur
     *            file to remove recursively.
     */
    public static void rm(File cur) {
        if (cur.isDirectory()) {
            for (File f : cur.listFiles()) {
                rm(f);
            }
        }
        cur.delete();
    }

    /**
     * Makes an md5 sum of the given string.
     * 
     * @param s
     *            string to make sum of.
     * @return the md5 as hexadecimals.
     */
    public static String md5(String s) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
            md5.update(s.getBytes());
            return new BigInteger(1, md5.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Copies all bytes of the input stream to the output stream. Will not close any streams.
     * 
     * @param is
     *            Read from
     * @param os
     *            Write to.
     * @throws IOException
     *             if any stream throws exception.
     */
    public static void spool(InputStream is, OutputStream os) throws IOException {

        byte[] buf = new byte[1024];
        int read;
        while ((read = is.read(buf, 0, buf.length)) != -1) {
            os.write(buf, 0, read);
        }

    }

    /**
     * Helper method to normalize <code>../foo/../bar/../</code> style paths.
     * 
     * @param path
     *            to normalize
     * @return the normalized path.
     */
    public static String normalize(String path) {
        try {
            URI uri = new URI("http", "fake", "/" + path, "", "");
            return uri.normalize().getPath();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

}
