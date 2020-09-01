package com.es.demo.controller;

import com.es.demo.Repository.PersonRepository;
import com.es.demo.index.Person;
import org.elasticsearch.common.UUIDs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("hello")
public class HelloController {
	@Autowired
	private PersonRepository personRepository;

	@RequestMapping("findByName")
	public void findByName(){
		List<Person> aa = personRepository.findByName("小明");
		System.out.println(aa);
	}
	@RequestMapping("save")
	public void save(){
		Person person = new Person();
		person.setId(UUIDs.base64UUID());
		person.setName("小明");
		person.setAge(17);
		personRepository.save(person);
		System.out.println("插入成功");
	}
}
