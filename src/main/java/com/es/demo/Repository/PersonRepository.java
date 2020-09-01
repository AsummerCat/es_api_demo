package com.es.demo.Repository;

import com.es.demo.index.Person;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonRepository extends ElasticsearchRepository<Person, String> {

	List<Person> findByName(String keyWord);
}
