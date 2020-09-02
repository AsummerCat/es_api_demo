package com.es.demo.service;

import com.es.demo.index.Person;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.elasticsearch.common.unit.TimeValue.timeValueMillis;
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
		 //构造插入的数据
        bulkRequest.add(new IndexRequest("user") //获取index
		        //获取id
		        .id("1")
		        //构造field
		        .source(XContentFactory.jsonBuilder()
				        .startObject()
				        .field("name", "小明")
				        .field("age", 18)
				        .endObject()
		        ));
		//构造删除的数据
		bulkRequest.add(new DeleteRequest("user", "1"));
		bulkRequest.add(new DeleteRequest("user", "2"));
		BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
		//返回结果处理
		BulkItemResponse[] items = bulk.getItems();
		for (BulkItemResponse item : items) {
			String statueName = item.getResponse().getResult().name();
			String id = item.getResponse().getId();
			if("DELETED".equals(statueName)){
				System.out.println("id-->"+id+"<--"+"删除成功");
			}
			if("NOT_FOUND".equals(statueName)){
				System.out.println("id-->"+id+"<--"+"删除失败 未找到相关数据");
			}
		}
	}


	/**
	 * 基于scroll进行滚动查询
	 */
	public void scrollQuery() throws IOException, InvocationTargetException, IllegalAccessException {

		//1.建立查询索引
		SearchRequest searchRequest =new SearchRequest("user");

		//2.设置scroll的参数
		Scroll scroll = new Scroll(timeValueMillis(100));
		searchRequest.scroll(scroll);

		//3.封装查询条件
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//		searchSourceBuilder.query(QueryBuilders.matchQuery("name", "小明"));
		searchSourceBuilder.query(QueryBuilders.matchAllQuery());
		//限制单次批量查询的条数
		searchSourceBuilder.size(1); //设定每次返回多少条数据
		//设置返回字段和排除字段
		searchSourceBuilder.fetchSource(new String[]{"name","age","id"},null);
		//排序
		searchSourceBuilder.sort("age", SortOrder.DESC);
		searchRequest.source(searchSourceBuilder);

		//4.进行搜索
		SearchResponse searchResponse = restHighLevelClient.search(searchRequest,RequestOptions.DEFAULT);
		//获取到scrollId
		String scrollId = searchResponse.getScrollId();
		SearchHit[] hits = searchResponse.getHits().getHits();
		List<SearchHit> hitsList = Arrays.stream(hits).collect(Collectors.toList());
		List<SearchHit> resultSearchHit = new ArrayList<>(hitsList);

		//5.递归查询并且获取赋值的内容
		while (!CollectionUtils.isEmpty(hitsList)) {
			//封装scrollId 接着查询
			SearchScrollRequest searchScrollRequest = new SearchScrollRequest(scrollId);
			searchScrollRequest.scroll(scroll);

			SearchResponse nextResponse = restHighLevelClient.scroll(searchScrollRequest,RequestOptions.DEFAULT);
			scrollId = nextResponse.getScrollId();
			hits = nextResponse.getHits().getHits();
			hitsList = Arrays.stream(hits).collect(Collectors.toList());
			resultSearchHit.addAll(hitsList);
		}

		//6.及时清除es快照，释放资源
		ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
		clearScrollRequest.addScrollId(scrollId);
		boolean succeeded=restHighLevelClient.clearScroll(clearScrollRequest,RequestOptions.DEFAULT).isSucceeded();
		System.out.println("succeeded:" + succeeded);

		//7.最后获取到的数据进行遍历转义成对象
		ArrayList<Person> adminLogs = new ArrayList<>();
		for (SearchHit hit : resultSearchHit) {
			Map<String, Object> sourceAsMap = hit.getSourceAsMap();
			Person person = new Person();
			BeanUtilsBean.getInstance().populate(person,sourceAsMap);
			adminLogs.add(person);
		}

		//循环展示数据
		adminLogs.forEach(System.out::println);

	}


	/**
	 ****************************************************************************************************************
	 ************************************更多的搜索语法**************************************************************
	 ************************************更多的搜索语法**************************************************************
	 ************************************更多的搜索语法**************************************************************
	 ****************************************************************************************************************
	 */
	public void SearchMove(){
		//1.建立查询索引
		SearchRequest searchRequest =new SearchRequest("user");

		//2.搜索语法
		//封装查询条件
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		//查询所有
		searchSourceBuilder.query(QueryBuilders.matchAllQuery());

		//match查询
		searchSourceBuilder.query(QueryBuilders.matchQuery("name", "小明"));

		//term查询
		searchSourceBuilder.query(QueryBuilders.termQuery("name", "小明"));


        //bool语法查询
		// 绑定查询条件
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		// status字段为301或302
		boolQueryBuilder.must(QueryBuilders.termsQuery("status.keyword", new String[]{"301","302"}));
		// args字段包含786754748671257
		boolQueryBuilder.must(QueryBuilders.matchPhraseQuery("args","786754748671257"));
		// 时间大于等于2020-05-21 00:00:00，小于2020-05-22 00:00:00
		boolQueryBuilder.must(QueryBuilders.rangeQuery("@timestamp").gte(("2020-05-21 00:00:00")).lt(("2020-05-22 00:00:00")));
		// 绑定bool query
		searchSourceBuilder.query(boolQueryBuilder);

        // 准确计数
		searchSourceBuilder.trackTotalHits(true);
		// 超时时间60s
		searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
		//设置返回字段和排除字段
		searchSourceBuilder.fetchSource(new String[]{"name","age","id"},null);

		//排序
		searchSourceBuilder.sort("age", SortOrder.DESC);

		//设定每次返回多少条数据
		searchSourceBuilder.size(1);


		/**
		 * *********************************************
		 * ************封装request+查询*****************
		 * *********************************************
		 */
		//4.将查询内容封装request
		searchRequest.source(searchSourceBuilder);


		//5.进行搜索
		try {
			SearchResponse searchResponse = restHighLevelClient.search(searchRequest,RequestOptions.DEFAULT);
			SearchHit[] hits = searchResponse.getHits().getHits();
			//获取返回值
			List<SearchHit> hitsList = Arrays.stream(hits).collect(Collectors.toList());
			//7.最后获取到的数据进行遍历转义成对象
			ArrayList<Person> adminLogs = new ArrayList<>();
			//根据返回值转换为实体类
			for (SearchHit hit : hitsList) {
				Map<String, Object> sourceAsMap = hit.getSourceAsMap();
				Person person = new Person();
				BeanUtilsBean.getInstance().populate(person,sourceAsMap);
				adminLogs.add(person);
			}
		} catch (IOException | IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
		/**
		 * *********************************************
		 * ************封装request+查询*****************
		 * *********************************************
		 */
	}

}

