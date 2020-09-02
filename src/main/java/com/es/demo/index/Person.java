package com.es.demo.index;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "user")
@Data
@ToString
public class Person {

    /**
     * 必须有id注解
     */
    @Id
    private String id;
    private String name;
    private int age;
    private String idCard;
}