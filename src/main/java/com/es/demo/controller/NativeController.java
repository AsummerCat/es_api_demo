package com.es.demo.controller;

import com.es.demo.service.EsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * 原生API的使用
 */
@RestController
@RequestMapping("native")
public class NativeController {
	@Autowired
	private EsService esService;



	/**
	 * 创建索引
	 * @throws IOException
	 */
	@RequestMapping("createIndex")
	public void createIndex() throws IOException {
		esService.createIndex();
	}

	/**
	 * 删除索引
	 * @throws IOException
	 */
	@RequestMapping("deleteIndex")
	public void deleteIndex() throws IOException {
		esService.deleteIndex();
	}

	/**
	 * 判断索引是否存在
	 */

	@RequestMapping("isIndexExists")
	public void isIndexExists() throws IOException {
		esService.isIndexExists();
	}




	/**
	 * 新增数据 方式一 index语法
	 * @throws IOException
	 */
	@RequestMapping("addDataForIndex")
	public void addDataForIndex() throws IOException {
		esService.addDataForIndex();
	}
	/**
	 * 新增数据 方式二 upsert语法 不存在就新增,存在就更新
	 * @throws IOException
	 */
	@RequestMapping("addData")
	public void addData() throws IOException {
		esService.addData();
	}

	/**
	 * 更新数据
	 * @throws IOException
	 */
	@RequestMapping("updateData")
	public void save() throws IOException {
		esService.updateData();
	}

	/**
	 * 一次性GET多条数据 不存在则为null
	 * @throws IOException
	 */
	@RequestMapping("multiGetExists")
	public void multiGetExists() throws IOException {
		esService.multiGetExists();
	}

	/**
	 * 批处理插入
	 * @throws IOException
	 */
	@RequestMapping("bulkBatchAdd")
	public void bulkBatchAdd() throws IOException {
		esService.bulkBatchAdd();
	}

	/**
	 * 批处理更新
	 * @throws IOException
	 */
	@RequestMapping("bulkBatchUpdate")
	public void bulkBatchUpdate() throws IOException {
		esService.bulkBatchUpdate();
	}

	/**
	 * 批处理删除
	 * @throws IOException
	 */
	@RequestMapping("bulkBatchDelete")
	public void bulkBatchDelete() throws IOException {
		esService.bulkBatchDelete();
	}

	/**
	 * 基于scroll进行滚动查询
	 * @throws IOException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	@RequestMapping("scrollQuery")
	public void scrollQuery() throws IOException, InvocationTargetException, IllegalAccessException {
		esService.scrollQuery();
	}


	/**
	 * 高亮查询
	 * @throws IOException
	 */
	@RequestMapping("highlightQuery")
	public void highlightQuery() throws IOException{
		esService.highlightQuery();
	}

}
