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

import java.util.Date;

/**
 * Represents an entry in a RAR archive.
 *
 * @author Adarsh Ramamurthy
 * @version 1.0, 10th March 2008
 */
public class RAREntry {
    private String name;

    private Date time;

    private long size;

    private long compressedSize;

    private long crc;

    private boolean directory;

    private String hostOS;

    private String method;

    private String version;

    public void setDirectory(boolean directory) {
        this.directory = directory;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setCompressedSize(long compressedSize) {
        this.compressedSize = compressedSize;
    }

    public void setCrc(long crc) {
        this.crc = crc;
    }

    public String getName() {
        return name;
    }

    public Date getTime() {
        return time;
    }

    public long getSize() {
        return size;
    }

    public long getCompressedSize() {
        return compressedSize;
    }

    public long getCrc() {
        return crc;
    }

    public boolean isDirectory() {
        return directory;
    }

    public String getHostOS() {
        return hostOS;
    }

    public void setHostOS(String hostOS) {
        this.hostOS = hostOS;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Constructs a <code>String</code> with all attributes
     * in name = value format.
     *
     * @return a <code>String</code> representation
     * of this object.
     */
    public String toString() {
        final String TAB = "    ";

        String retValue = "";

        retValue = "RAREntry ( "
            + "name = " + this.name + TAB
            + "time = " + this.time + TAB
            + "size = " + this.size + TAB
            + "compressedSize = " + this.compressedSize + TAB
            + "crc = " + this.crc + TAB
            + "directory = " + this.directory + TAB
            + "hostOS = " + this.hostOS + TAB
            + "method = " + this.method + TAB
            + "version = " + this.version + TAB
            + " )";

        return retValue;
    }
}
