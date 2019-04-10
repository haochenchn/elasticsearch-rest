package com.aaron.es.model;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: GongDeguang
 * Date: 2017-07-25 11:00
 */

public class EsQueryResult {
    int result = -1;//结果码, 0表示正常
    String errMsg;//异常信息

    long timeTook;//查询耗时ms
    long allHitCnt;//命中的记录总条数

    //---List<Pair<id, recordMap>>
    //List<Pair<String, Map<String, Object>>> curPageRecords;//当前分页的records
    List<Pair<String, String>> curPageRecords;//当前分页的records


    @Override
    public String toString() {
        return "QueryResult{" +
                "result=" + result +
                ", errMsg='" + errMsg + '\'' +
                ", timeTook=" + timeTook +
                ", allHitCnt=" + allHitCnt +
                ", curPageRecords=" + curPageRecords +
                '}';
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public long getTimeTook() {
        return timeTook;
    }

    public void setTimeTook(long timeTook) {
        this.timeTook = timeTook;
    }

    public void addTimeTook(long timeTook) {
        this.timeTook += timeTook;
    }

    public long getAllHitCnt() {
        return allHitCnt;
    }

    public void setAllHitCnt(long allHitCnt) {
        this.allHitCnt = allHitCnt;
    }

    public List<Pair<String, String>> getCurPageRecords() {
        return curPageRecords;
    }

    public List<String> getCurPageIds() {
        if (curPageRecords == null || curPageRecords.size() == 0) {
            return null;
        }
        List<String> ids = new ArrayList<>(curPageRecords.size());
        for (Pair<String, String> pair : curPageRecords) {
            ids.add(pair.getKey());
        }
        return ids;
    }

    public void setCurPageRecords(List<Pair<String, String>> curPageRecords) {
        this.curPageRecords = curPageRecords;
    }

    public static class ErrMsg {
        public static final String SUCCESS = "successful";
        public static final String BLANK_RESULT = "blank result";
        public static final String PARAM_ERROR = "param error";
    }


    public static EsQueryResult createBlankResult(long timeTook) {
        EsQueryResult res = new EsQueryResult();
        res.setResult(0);
        res.setErrMsg(ErrMsg.BLANK_RESULT);
        res.setTimeTook(timeTook);
        res.setAllHitCnt(0);
        res.setCurPageRecords(new ArrayList<>());
        return res;
    }

    public static EsQueryResult createErrorResult(String errMsg) {
        EsQueryResult res = new EsQueryResult();
        res.setResult(-1);
        res.setErrMsg(errMsg);
        res.setCurPageRecords(new ArrayList<>());
        return res;
    }

    public static EsQueryResult createSuccessResult(
            long timeTook, long allHitCnt, List<Pair<String, String>> curPageRecords) {
        EsQueryResult res = new EsQueryResult();
        res.setTimeTook(timeTook);
        res.setAllHitCnt(allHitCnt);
        res.setCurPageRecords(curPageRecords);
        res.setResult(0);
        res.setErrMsg(ErrMsg.SUCCESS);
        return res;
    }

}
