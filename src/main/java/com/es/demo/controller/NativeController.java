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

	@RequestMapping("findByName")
	public void findByName() {
	}

	/**
	 * 新增
	 * @throws IOException
	 */
	@RequestMapping("addData")
	public void addData() throws IOException {
		esService.addData();
		System.out.println("插入成功");
	}

	/**
	 * 更新
	 * @throws IOException
	 */
	@RequestMapping("updateData")
	public void save() throws IOException {
		esService.updateData();
		System.out.println("插入或更新成功");
	}

	/**
	 * 一次性GET多条数据 不存在则为null
	 * @throws IOException
	 */
	@RequestMapping("multiGetExists")
	public void multiGetExists() throws IOException {
		esService.multiGetExists();
		System.out.println("一次性GET多条数据 不存在则为null");
	}

	/**
	 * 批处理插入
	 * @throws IOException
	 */
	@RequestMapping("bulkBatchAdd")
	public void bulkBatchAdd() throws IOException {
		esService.bulkBatchAdd();
		System.out.println("bulk批处理插入");
	}

	/**
	 * 批处理更新
	 * @throws IOException
	 */
	@RequestMapping("bulkBatchUpdate")
	public void bulkBatchUpdate() throws IOException {
		esService.bulkBatchUpdate();
		System.out.println("bulk批处理更新");
	}

	/**
	 * 批处理删除
	 * @throws IOException
	 */
	@RequestMapping("bulkBatchDelete")
	public void bulkBatchDelete() throws IOException {
		esService.bulkBatchDelete();
		System.out.println("bulk批处理删除");
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
		System.out.println("基于scroll进行滚动查询");
	}

}
