/*******************************************************************************
 * COPYRIGHT(c) 2015 STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *   1. Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *   3. Neither the name of STMicroelectronics nor the names of its contributors
 *      may be used to endorse or promote products derived from this software
 *      without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 ******************************************************************************/
package com.st.BlueSTSDK.Log;

import android.util.Log;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.Field;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * Dump the feature change on a coma separated value file, the fist line will contain the data
 * name and the others the feature data.
 * <p>
 * The name file will be equal to the feature name.
 * </p>
 * <p>
 * This class can be used for dump more than a feature at time, each feature will be dumped in a
 * different file
 * </p>
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class FeatureLogCSVFile implements Feature.FeatureLoggerListener {
    private final static String TAG = FeatureLogCSVFile.class.getCanonicalName();

    /** directory where save the dump file*/
    private String mDirectoryPath;
    /**
     * map used for associate a file to each feature
     */
    private Map<Feature,Writer> mFormatterCacheMap;

    /**
     * create a new logger
     * @param dumpDirectoryPath directory path used for save the file
     */
    public FeatureLogCSVFile(String dumpDirectoryPath){
        mFormatterCacheMap = new HashMap<>();
        mDirectoryPath = dumpDirectoryPath;
        File f = new File(mDirectoryPath);
        if(!f.exists())
            f.mkdirs();
    }//FeatureLogCSVFile

    /**
     * print the file header, with the node name the raw data and the feature field
     * @param out stream where write the feature data
     * @param f feature that this close will dump
     * @throws IOException if there is an error during the stream writing
     */
    private void printHeader(Writer out,Feature f)throws IOException {
        Field fields[] = f.getFieldsDesc();
        out.append("Device,Timestamp,RawData,");
        for(Field field:fields){
            out.append(field.getName());
            out.append(',');
        }//for
        out.append('\n');
    }//printHeader

    /**
     * dump an array of byte as a string of hexadecimal value
     * <p> No coma will be added at the end of the string</p>
     * @param out stream where write the feature data
     * @param data byte data to dump
     * @throws IOException if there is an error during the stream writing
     */
    private void storeBlobData(Writer out,byte data[]) throws IOException {
        for(byte b: data) {
            out.append( String.format("%02X", b));
        }//for
    }//storeBlobData

    /**
     * dump the feature data
     * @param out stream where write the feature data
     * @param data feature data
     * @throws IOException if there is an error during the stream writing
     */
    private void storeFeatureData(Writer out, Number data[]) throws IOException {
        for(Number n: data){
            out.append(n.toString());
            out.append(',');
        }//for
        out.append('\n');
    }

    /**
     * create a new file for the feature or return an already opened file,
     * <p>
     *   the file will be created in the directory passed to the constructor and with the feature
     *   name
     * </p>
     * @param f feature that you want dump
     * @return stream where write the feature data
     * @throws IOException if there is an error during the stream writing
     */
    private Writer openDumpFile(Feature f) throws IOException {
        Writer temp = mFormatterCacheMap.get(f);
        if(temp!=null){
            return temp;
        }
        //else
        temp = new OutputStreamWriter(new FileOutputStream(String.format("%s/%s.csv",
                mDirectoryPath, f.getName()),true));
        printHeader(temp,f);
        mFormatterCacheMap.put(f, temp);
        return temp;
    }

    @Override
    public void logFeatureUpdate(Feature feature, byte[] rawData, Feature.Sample data) {
        try {
            Writer out = openDumpFile(feature);
            synchronized (out) { // be secure that only one call write on the file
                out.append(feature.getParentNode().getTag());
                out.append(',');
                out.append(Long.toString(data.timestamp));
                out.append(',');
                if (rawData != null)
                    storeBlobData(out, rawData);
                out.append(',');
                storeFeatureData(out, data.data);
            }//synchronized
        } catch (IOException e) {
            Log.e(TAG,"Error dumping data Feature: "+feature.getName()+"\n"+e.toString());
        }//try-catch
    }

    /**
     * close all the open file
     */
    public void closeFiles(){
        try {
            for (Writer w : mFormatterCacheMap.values()) {
                w.flush();
                w.close();
            }//for
        }catch (IOException e){
            Log.e(TAG,"Error closing the file: "+e.toString());
        }
        mFormatterCacheMap.clear();
    }

    /**
     * get all the log file in the directory
     * @param directoryPath path where search the file
     * @return all file in the directory with an extension .csv
     */
    static public File[] getLogFiles(String directoryPath){
        File directory = new File(directoryPath);
        //find all the csv files
        final FileFilter csvFilter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".csv");
            }
        };
        return directory.listFiles(csvFilter);
    }

    /**
     * remove all the csv file in the directory
     * @param directoryPath directory where this class dumped the feature data
     */
    static public void clean(String directoryPath){
        for(File f: getLogFiles(directoryPath) ){
            if(!f.delete())
                Log.e(TAG,"Error deleting the file "+f.getAbsolutePath());
        }//for
    }

}
