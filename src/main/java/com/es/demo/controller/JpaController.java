package com.es.demo.controller;

import com.es.demo.Repository.PersonRepository;
import com.es.demo.index.Person;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.UUIDs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.websocket.ClientEndpointConfig;
import java.util.List;

/**
 * JPA方式的使用es
 */
@RestController
@RequestMapping("jpa")
public class JpaController {
	@Autowired
	private PersonRepository personRepository;

	@RequestMapping("findByName")
	public void findByName() {
		List<Person> aa = personRepository.findByName("小明");
		System.out.println(aa);
	}

	@RequestMapping("save")
	public void save() {
		Person person = new Person();
		person.setId(UUIDs.base64UUID());
		person.setName("小明");
		person.setAge(17);
		personRepository.save(person);
		System.out.println("插入成功");
	}
}
