/*
 * RARoScope - Java Library for Scanning RAR Archives
 * 
 * Copyright 2008 Adarsh Ramamurthy 
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
 * 
 * Homepage: http://www.adarshr.com/papers/raroscope
 */

package com.adarshr.raroscope;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * Represents a RAR archive.
 * <p/>
 * This class is used to enumerate the entries in a RAR file.
 *
 * @author Adarsh Ramamurthy
 * @version 1.0, 10th March 2008
 */
public class RARFile {
    /**
     * The underlying stream.
     */
    private InputStream stream;

    /**
     * Marker block of RAR archives. It is "Rar!" in reverse bytes.
     */
    private static final long MARKER = 0x21726152L;

    /**
     * To hold the available bytes in the stream.
     */
    private long available;

    /**
     * Constructs an instance of <tt>RARFile</tt> for performing operations
     * on the archive.
     *
     * @param name the RAR file name.
     * @throws IOException in case of errors reading from the archive.
     */
    public RARFile(String name) throws IOException {
        this(new File(name));
    }

    /**
     * Constructs an instance of <tt>RARFile</tt> for performing operations
     * on the archive.
     *
     * @param file the RAR file.
     * @throws IOException in case of errors reading from the archive.
     */
    public RARFile(File file) throws IOException {
        this.stream = new FileInputStream(file);

        this.available = this.stream.available();

        byte[] headers = new byte[7 + 13];

        // Read the Marker Block and Archive Header Blocks.
        this.stream.read(headers);

        if (MARKER != getLong(headers, 0, 3)) {
            throw new IOException("Invalid RAR archive");
        }
    }

    /**
     * Enumerates all the entries in the RAR archive.
     *
     * @return an instance of <tt>Enumeration</tt> for displaying the entries.
     * @see RAREntry
     */
    public Enumeration<RAREntry> entries() {
        return new Enumeration<RAREntry>() {

            public RAREntry nextElement() {
                byte[] buf = new byte[32];

                RAREntry entry = null;

                try {
                    RARFile.this.available -= RARFile.this.stream.read(buf);

                    int type = buf[2] & 0xFF;

                    // Means File Header Block
                    if (type == 0x74) {
                        entry = new RAREntry();

                        long flags = getLong(buf, 3, 4);

                        entry.setDirectory((flags & 0xE0) == 0xE0);

                        long pSize = getLong(buf, 7, 10);
                        long size = getLong(buf, 11, 14);

                        // Very large file ( > 2GB )
                        if ((flags & 0x100) == 0x100) {
                            byte[] hiBytes = new byte[8];

                            RARFile.this.available -=
                                RARFile.this.stream.read(hiBytes);

                            pSize = getLong(hiBytes, 0, 4) << 32 | pSize;
                            size = getLong(hiBytes, 5, 8) << 32 | size;
                        }

                        long hSize = getLong(buf, 5, 6);

                        entry.setCompressedSize(pSize);
                        entry.setSize(getLong(buf, 11, 14));
                        entry.setHostOS(toOS(buf[15] & 0xFF));
                        entry.setCrc(getLong(buf, 16, 19));
                        entry.setTime(toDate(getLong(buf, 20, 23)));
                        entry.setVersion(toVersion(buf[24] & 0xFF));
                        entry.setMethod(toMethod(buf[25] & 0xFF));

                        long nSize = getLong(buf, 26, 27);

                        byte[] name = new byte[(int) nSize];

                        RARFile.this.available -=
                            RARFile.this.stream.read(name);

                        entry.setName(new String(name));

                        RARFile.this.available -=
                            RARFile.this.stream.skip(
                                hSize - (32 + nSize) + pSize);
                    }
                } catch (IOException e) {
                    throw new NoSuchElementException(e.getMessage());
                }

                if (entry == null) {
                    throw new NoSuchElementException();
                }

                return entry;
            }

            public boolean hasMoreElements() {
                return RARFile.this.available > 32;
            }

        };
    }

    /**
     * Closes the archive.
     *
     * @throws IOException in case of errors while closing.
     */
    public void close() throws IOException {
        this.stream.close();
    }

    /**
     * Converts the input inverted array of bytes to a long representation.
     *
     * @param bytes the byte array to be converted.
     * @return the long value.
     */
    protected long getLong(byte[] bytes) {
        long ret = 0;
        long mask = 0;

        for (int i = 0; i < bytes.length; i++) {
            ret |= (bytes[i] & 0xFF) << (8 * i); // mask and shift left
            mask = (mask << 8) | 0xFF; // generate the final mask
        }

        return ret & mask;
    }

    /**
     * Converts the input inverted array of bytes to a long representation.
     * Conversion is done inclusive of both the limits specified.
     *
     * @param bytes the byte array to be converted.
     * @param start the index to start with.
     * @param end   the end index.
     * @return the long value.
     */
    protected long getLong(byte[] bytes, int start, int end) {
        long ret = 0;
        long mask = 0;

        if (start < 0 || end >= bytes.length) {
            return ret;
        }

        for (int i = start, j = 0; i <= end; i++, j++) {
            ret |= (bytes[i] & 0xFF) << (8 * j); // mask and shift left
            mask = (mask << 8) | 0xFF; // generate the final mask
        }

        return ret & mask;
    }

    /**
     * Converts the DOS time to Java date.
     *
     * @param dosTime MS DOS format time.
     * @return an instance of <tt>Date</tt>.
     */
    protected Date toDate(long dosTime) {
        Calendar calendar = Calendar.getInstance();

        calendar.set(
            (int) (((dosTime >> 25) & 0x7f) + 1980),  // year
            (int) (((dosTime >> 21) & 0x0f) - 1),  // month
            (int) ((dosTime >> 16) & 0x1f),  // date
            (int) ((dosTime >> 11) & 0x1f),  // hours
            (int) ((dosTime >> 5) & 0x3f),  // minutes
            (int) ((dosTime << 1) & 0x3e));  // seconds

        return calendar.getTime();
    }

    /**
     * Translate the OS byte to a human readable string.
     *
     * @param o the number to be translated (1 byte).
     * @return the OS string.
     */
    protected String toOS(int o) {
        String os = null;

        switch (o) {
            case 0:
                os = "MS DOS";
                break;

            case 1:
                os = "OS/2";
                break;

            case 2:
                os = "Win32";
                break;

            case 3:
                os = "Unix";
                break;

            case 4:
                os = "Mac OS";
                break;

            case 5:
                os = "BeOS";
                break;

            default:
                os = "Unknown";
                break;
        }

        return os;
    }

    /**
     * Decodes the version information. Version number will be of the format
     * 10 * Major Version + Minor Version.
     *
     * @param v the version number.
     * @return the decoded version in the format "major.minor".
     */
    protected String toVersion(int v) {
        return String.valueOf(v / 10F);
    }

    /**
     * Translates the compression method into a string.
     *
     * @param m the compression method number.
     * @return the compression method string.
     */
    protected String toMethod(int m) {
        String method = null;

        switch (m) {
            case 0x30:
                method = "Storing";
                break;

            case 0x31:
                method = "Fastest Compression";
                break;

            case 0x32:
                method = "Fast Compression";
                break;

            case 0x33:
                method = "Normal Compression";
                break;

            case 0x34:
                method = "Good Compression";
                break;

            case 0x35:
                method = "Best Compression";
                break;

            default:
                method = "Unknown";
                break;
        }

        return method;
    }
}
