package com.es.demo.controller;

import com.es.demo.service.EsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

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


}
