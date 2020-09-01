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


	@RequestMapping("addData")
	public void addData() throws IOException {
		esService.addData();
		System.out.println("插入成功");
	}
	@RequestMapping("updateData")
	public void save() throws IOException {
		esService.updateData();
		System.out.println("插入或更新成功");
	}
}
