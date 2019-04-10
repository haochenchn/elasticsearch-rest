package com.aaron.es.test;

import com.aaron.es.annotation.ESMapping;
import com.aaron.es.annotation.ESMetaData;
import com.aaron.es.enums.Analyzer;
import com.aaron.es.enums.DataType;

@ESMetaData(indexName = "test2",indexType = "d")
public class Leader {

    @ESMapping(datatype = DataType.keyword_type,copy_to = "COPYTO")
    private String CITY;

    @ESMapping(datatype = DataType.text_type,search_analyzer = Analyzer.ik_max_word,copy_to = "COPYTO")
    private String DISTRICT;

    @ESMapping(datatype = DataType.text_type,copy_to = "COPYTO")
    private String ORG_NAME;

    @ESMapping(datatype = DataType.text_type,copy_to = "COPYTO")
    private String ID_NO;

    @ESMapping(datatype = DataType.text_type)
    private String COPYTO;

    public Leader() {
    }

    public Leader(String CITY, String DISTRICT, String ORG_NAME, String ID_NO, String COPYTO) {
        this.CITY = CITY;
        this.DISTRICT = DISTRICT;
        this.ORG_NAME = ORG_NAME;
        this.ID_NO = ID_NO;
        this.COPYTO = COPYTO;
    }

    public String getCITY() {
        return CITY;
    }

    public void setCITY(String CITY) {
        this.CITY = CITY;
    }

    public String getDISTRICT() {
        return DISTRICT;
    }

    public void setDISTRICT(String DISTRICT) {
        this.DISTRICT = DISTRICT;
    }

    public String getORG_NAME() {
        return ORG_NAME;
    }

    public void setORG_NAME(String ORG_NAME) {
        this.ORG_NAME = ORG_NAME;
    }

    public String getID_NO() {
        return ID_NO;
    }

    public void setID_NO(String ID_NO) {
        this.ID_NO = ID_NO;
    }

    public String getCOPYTO() {
        return COPYTO;
    }

    public void setCOPYTO(String COPYTO) {
        this.COPYTO = COPYTO;
    }
}
