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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.st.BlueSTSDK.Feature;

import java.io.File;
import java.util.List;

/**
 * Store the feature data into a Db in ram, the data can be exported in a csv file when the
 * logging is finished
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class FeatureLogDB implements Feature.FeatureLoggerListener {

    /** context used for open the db */
    private Context c;
    /** class used for create the db */
    private FeatureLogDBOpenHelper mDbHelper;
    /** db where store the data */
    private SQLiteDatabase mDb;

    /**
     * create a db for store the features list
     * @param c context to use for open the db
     * @param features list of feature that we want dump
     */
    public FeatureLogDB(Context c,List<Feature> features){
        this.c = c;
        mDbHelper = new FeatureLogDBOpenHelper(c,features);
        mDb = mDbHelper.getWritableDatabase();
    }//FeatureLogDb

    @Override
    public void logFeatureUpdate(Feature feature, byte[] rawData, Feature.Sample data) {
        mDb.insert(FeatureLogDBOpenHelper.sanitizeFeatureName(feature.getName()),null,
                FeatureLogDBOpenHelper.getFeatureRow(feature, rawData, data));
    }

    /**
     * dump each db table in a csv file
     * @param dirPath directory where store the files
     * @return array with the file where the db is dumped
     */
    public File[] dumpToFile(String dirPath) {
        return mDbHelper.dumpToFile(c,dirPath);
    }

    /**
     * remove all the dumped file
     * @param directoryPath directory where we dump the db
     */
    static public void clean(String directoryPath){
        FeatureLogDBOpenHelper.clean(directoryPath);
    }


}
