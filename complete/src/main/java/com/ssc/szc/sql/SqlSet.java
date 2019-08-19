package com.ssc.szc.sql;

import org.intellij.lang.annotations.Language;

/**
 * @author Lebonheur
 */
public class SqlSet {

    @Language("Oracle")
    public static final String UPDATE_SESSION_KEY = " MERGE INTO TCS.USER_SESSION_KEY USK " +
                                                    " USING (SELECT ? AS OPEN_ID, ? AS KEY FROM DUAL) INP " +
                                                    " ON (USK.OPEN_ID = INP.OPEN_ID) " +
                                                    " WHEN MATCHED THEN " +
                                                    "      UPDATE SET USK.SESSION_KEY = INP.KEY " +
                                                    " WHEN NOT MATCHED THEN " +
                                                    "      INSERT (USK.OPEN_ID, USK.SESSION_KEY) VALUES (INP.OPEN_ID, INP.KEY) ";

    @Language("Oracle")
    public static final String QUERY_SESSION_KEY_BY_ID = " SELECT SESSION_KEY FROM TCS.USER_SESSION_KEY WHERE OPEN_ID = ? ";

    @Language("Oracle")
    public static final String INSERT_BLACKLIST = " INSERT INTO TCS.USER_BLACKLIST (OPEN_ID, CUR_TIME, TYPES) " +
                                                  " VALUES (?, TO_CHAR(SYSTIMESTAMP, 'YYYY-MM-DD HH24:MI:SS'), ?) ";

}
