package com.finalyearproject.dan.ocraccountingapp;

import com.finalyearproject.dan.ocraccountingapp.nosql.nosql.NoSQLResult;

import java.io.Serializable;

public class SQLObj implements Serializable {

    private NoSQLResult noSQLResult;

    public void setObj(NoSQLResult noSQLResult) {this.noSQLResult = noSQLResult;}

    public NoSQLResult getObj() {return noSQLResult;}
}
