/*
 *    GISToolkit - Geographical Information System Toolkit
 *    (C) 2003, Ithaqua Enterprises Inc.
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package gistoolkit.datasources.terraserver;

import java.util.Date;

/**
 * Class to keep track of performance statistics for the TerraserverDataSource.
 */
public class TerraserverPerformanceLog {
    
    /** How much time is spent reading data from disk. */
    private int myReadsFromDisk = 0;
    private long myReadFromDiskTime = 0;
    public void readFromDisk(long inTime){myReadsFromDisk++; myReadFromDiskTime = myReadFromDiskTime + inTime;}
    
    /** How much time is spent reading from terraserver. */
    private int myReadsFromServer = 0;
    private long myReadFromServerTime = 0;
    public void readFromServer(long inTime){myReadsFromServer++; myReadFromServerTime = myReadFromServerTime + inTime;}

    /** When did this generation start. */
    private long myStart = new Date().getTime();
    
    /** Whend did this generation end. */
    private long myEnd = 0;
    public void end(){myEnd = new Date().getTime();}
    
    /** StringBuffer for adding log comments. */
    private StringBuffer myStringBufferLog = null;
    
    /** Creates a new instance of TerraserverPerformanceLog */
    public TerraserverPerformanceLog(StringBuffer inStringBuffer) {
        myStringBufferLog = inStringBuffer;
    }
    
    /** Logs a message. */
    public void log(String inString){
        if (myStringBufferLog != null) {
            myStringBufferLog.append(new Date());
            myStringBufferLog.append(" - ");
            myStringBufferLog.append(inString);
            myStringBufferLog.append("\n");
        }
    }
    
    /** Get the return. */
    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append(new Date(myStart) + " - Beginning\n");
        sb.append(myStringBufferLog);
        sb.append("TotalDiskReads = "+myReadsFromDisk+"\n");
        sb.append("TimeReadingFromDisk = "+(myReadFromDiskTime/1000.0)+" seconds\n");
        sb.append("TotalServerReads = "+myReadsFromServer+"\n");
        sb.append("TimeReadingFromServer = "+(myReadFromServerTime/1000.0)+" seconds\n");
        sb.append("TotalTime = "+((myEnd-myStart)/1000.0)+" seconds\n");
        sb.append(new Date(myEnd) + " - End\n");
        return sb.toString();
    }
}
