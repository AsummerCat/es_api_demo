package com.es.demo.service;

import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * 使用restHighLevelClient原生语法
 */
@Service
public class EsService {
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 新增数据
     */
    public void addData() throws IOException {
        //构造原有数据
        IndexRequest indexRequest = new IndexRequest("user") //获取index
                //获取id
                .id("3")
                //构造field
                .source(jsonBuilder()
                        .startObject()
                        .field("name", "小白")
                        .field("age", 30)
                        .endObject()
                );
        //如果是纯插入 这边doc中就不要添加内容
        UpdateRequest updateRequest = new UpdateRequest("user","3")
                .doc(XContentFactory.jsonBuilder()
                .startObject()
        .endObject()).upsert(indexRequest);
        //插入
        restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
    }
    /**
     * 更新数据
     * upsert语法 如果之前信息不存在就insert 存在就update
     * @throws IOException
     */
    public void updateData() throws IOException {

        //构造原有数据
        IndexRequest indexRequest = new IndexRequest("user") //获取index
                //获取id
                .id("1")
                //构造field
                .source(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("name", "小明")
                        .field("age", 18)
                        .endObject()
                );
        //修改后的field的数据
        UpdateRequest updateRequest = new UpdateRequest("user",  "1")
                .doc(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("age",21)
                        .endObject())
                .upsert(indexRequest);
        //执行更新语法
        DocWriteResponse.Result result = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT).getResult();

    }

}
