package com.aaron.es.model;

import java.util.List;

/**
 * ElasticSearch查询所使用的基本类,
 * 为符合ES发展趋势，程序开发过程中，每个索引只能拥有一种数据类型。
 * @author Administrator
 *
 */
public class EsQueryVo {

	private String indices=""; //索引名称

	private String types="";   //索引中的类型

	private int pageSize=30;       //返回数据量

	private int pageNum = 1;       //当前页码

	private String sortField;	//排序字段
	private boolean isDesc = false; //是否倒序

	private int count=0;      //总条数，用于分页处理

	private int timeout=30;   //设置请求超时时间

	private String keyword; //全文本搜索

	private String flag="1";     //1表示term搜索 0表示全文检索

	private List<String> categoryIds; //数据类型id集合

	public List<String> getCategoryIds() {
		return categoryIds;
	}

	public void setCategoryIds(List<String> categoryIds) {
		this.categoryIds = categoryIds;
	}

	public String getIndices() {
		return indices;
	}

	public void setIndices(String indices) {
		this.indices = indices;
	}

	public String getTypes() {
		return types;
	}

	public void setTypes(String types) {
		this.types = types;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getPageNum() {
		return pageNum;
	}

	public void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public String getSortField() {
		return sortField;
	}

	public void setSortField(String sortField) {
		this.sortField = sortField;
	}

	public boolean isDesc() {
		return this.isDesc;
	}

	public void setDesc(boolean desc) {
		this.isDesc = desc;
	}

	@Override
	public String toString() {
		return "Elasticsearch QueryVo [index="+ indices +"; keyword=" + keyword + "]";
	}	
		
	
}
