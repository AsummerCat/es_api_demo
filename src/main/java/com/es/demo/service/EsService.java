package com.es.demo.service;

import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequest;
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
		UpdateRequest updateRequest = new UpdateRequest("user", "3")
				.doc(XContentFactory.jsonBuilder()
						.startObject()
						.endObject()).upsert(indexRequest);
		//插入
		restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
	}

	/**
	 * 更新数据
	 * upsert语法 如果之前信息不存在就insert 存在就update
	 *
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
		UpdateRequest updateRequest = new UpdateRequest("user", "1")
				.doc(XContentFactory.jsonBuilder()
						.startObject()
						.field("age", 21)
						.endObject())
				.upsert(indexRequest);
		//执行更新语法
		DocWriteResponse.Result result = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT).getResult();

	}


	/**
	 * 一次性GET多条数据 不存在则为null
	 *
	 * @throws IOException
	 */
	public void multiGetExists() throws IOException {
		MultiGetRequest items = new MultiGetRequest();
		//拼接数据
		items.add(new MultiGetRequest.Item("user", "1"));
		items.add(new MultiGetRequest.Item("user", "2"));
		items.add(new MultiGetRequest.Item("user", "3"));
		items.add(new MultiGetRequest.Item("user", "4"));
		//GET 多条数据
		MultiGetItemResponse[] responses = restHighLevelClient.mget(items, RequestOptions.DEFAULT).getResponses();
		//返回的结果如果是存在的 输出json
		for (MultiGetItemResponse respons : responses) {
			GetResponse res = respons.getResponse();
			//document是否存在
			if (res.isExists()) {
				String json = res.getSourceAsString();
				System.out.println(json);
			}
		}
	}


	/**
	 * 批处理插入
	 *
	 * @throws IOException
	 */
	public void bulkBatchAdd() throws IOException {
		BulkRequest bulkRequest = new BulkRequest();
		//批量构建数据
		bulkRequest.add(new IndexRequest("user")
				.id("1")
				.source(XContentFactory.jsonBuilder()
						.startObject()
						.field("name", "小明")
						.field("age", 18)
						.endObject()
				));
		bulkRequest.add(new IndexRequest("user")
				.id("2")
				.source(XContentFactory.jsonBuilder()
						.startObject()
						.field("name", "小红")
						.field("age", 19)
						.endObject()
				));
		bulkRequest.add(new IndexRequest("user")
				.id("3")
				.source(XContentFactory.jsonBuilder()
						.startObject()
						.field("name", "小蓝")
						.field("age", 19)
						.endObject()
				));
		//提交批处理
		restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
	}


	/**
	 * 批处理更新
	 *
	 * @throws IOException
	 */
	public void bulkBatchUpdate() throws IOException {
		BulkRequest bulkRequest = new BulkRequest();
		//构造原有数据
		IndexRequest indexRequest1 = new IndexRequest("user") //获取index
				//获取id
				.id("1")
				//构造field
				.source(XContentFactory.jsonBuilder()
						.startObject()
						.field("name", "小明")
						.field("age", 18)
						.endObject()
				);
		IndexRequest indexRequest2 = new IndexRequest("user") //获取index
				//获取id
				.id("2")
				//构造field
				.source(XContentFactory.jsonBuilder()
						.startObject()
						.field("name", "小东")
						.field("age", 18)
						.endObject()
				);



		//构造更新的数据
		bulkRequest.add(new UpdateRequest("user", "1")
				.doc(XContentFactory.jsonBuilder()
						.startObject()
						.field("age", 30)
						.endObject())
				//不存在就插入,存在就更新
				.upsert(indexRequest1));
		bulkRequest.add(new UpdateRequest("user", "2")
				.doc(XContentFactory.jsonBuilder()
						.startObject()
						.field("age", 40)
						.endObject())
				.upsert(indexRequest2));
		//直接更新
		bulkRequest.add(new UpdateRequest("user", "3")
				.doc(XContentFactory.jsonBuilder()
						.startObject()
						.field("age", 50)
						.endObject()));
         //批处理
		restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
	}

	/**
	 * 批处理删除
	 *
	 * @throws IOException
	 */
	public void bulkBatchDelete() throws IOException {
		BulkRequest bulkRequest = new BulkRequest();
		//构造删除的数据
		bulkRequest.add(new UpdateRequest("user", "1")
				.doc(XContentFactory.jsonBuilder()
						.startObject()
						.field("age", 21)
						.endObject()));
		bulkRequest.add(new DeleteRequest("user", "2"));
		bulkRequest.add(new DeleteRequest("user", "3"));
		restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
	}

}

