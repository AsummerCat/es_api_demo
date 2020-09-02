package com.es.demo.service;

import com.alibaba.fastjson.JSONObject;
import com.es.demo.index.Person;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.collections4.MapUtils;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.script.mustache.SearchTemplateRequest;
import org.elasticsearch.script.mustache.SearchTemplateResponse;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortMode;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.elasticsearch.client.indices.CreateIndexRequest;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
     * 创建自定义索引
     */
    public void createIndex() throws IOException {
        XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                .field("properties")
                .startObject()
                .field("name").startObject().field("index", "true").field("type", "keyword").endObject()
                .field("age").startObject().field("index", "true").field("type", "integer").endObject()
                .field("money").startObject().field("index", "true").field("type", "double").endObject()
                .field("address").startObject().field("index", "true").field("type", "text").field("analyzer", "ik_max_word").endObject()
                .field("birthday").startObject().field("index", "true").field("type", "date").field("format", "strict_date_optional_time||epoch_millis").endObject()
                .endObject()
                .endObject();
        //注意索引必须是小写
        CreateIndexRequest createIndexRequest = new CreateIndexRequest("index_name");
        createIndexRequest.mapping(builder);
        CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
        boolean acknowledged = createIndexResponse.isAcknowledged();
        if (acknowledged) {
            System.out.println("索引创建成功");
        } else {
            System.out.println("索引创建失败");
        }
    }

    /**
     * 删除索引
     */
    public void deleteIndex() {
        try {
            DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest("index_name");
            deleteIndexRequest.indicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);
            AcknowledgedResponse delete = restHighLevelClient.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
            boolean acknowledged = delete.isAcknowledged();
            if (acknowledged) {
                System.out.println("删除成功");
            } else {
                System.out.println("删除失败");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断索引是否存在
     */
    public void isIndexExists() {
        try {
            GetIndexRequest getIndexRequest = new GetIndexRequest("index_name");
            getIndexRequest.humanReadable(true);
            boolean exists = restHighLevelClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
            String va = exists ? "存在" : "不存在";
            System.out.println("索引" + va);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 方式一 新增数据 index语法
     */
    public void addDataForIndex() throws IOException {
        //索引名称
        IndexRequest indexRequest = new IndexRequest("user");
        //可自定义Id
//        indexRequest.id("111");
        Person person = new Person();
        person.setId("1");
        person.setAge(18);
        person.setName("小明");
        person.setIdCard("350123199512161511");
        String userJson = JSONObject.toJSONString(person);
        //数据传输格式json
        indexRequest.source(userJson, XContentType.JSON);

        //构建数据
        IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        if (indexResponse != null) {
            String id = indexResponse.getId();
            String index = indexResponse.getIndex();
            long version = indexResponse.getVersion();
            if (indexResponse.getResult() == DocWriteResponse.Result.CREATED) {
                System.out.println("新增文档成功!" + index + "-" + id + "-" + version);
            } else if (indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {
                System.out.println("修改文档成功!");
            }
            // 分片处理信息
            ReplicationResponse.ShardInfo shardInfo = indexResponse.getShardInfo();
            if (shardInfo.getTotal() != shardInfo.getSuccessful()) {
                System.out.println("分片处理信息.....");
            }
            // 如果有分片副本失败，可以获得失败原因信息
            if (shardInfo.getFailed() > 0) {
                for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
                    String reason = failure.reason();
                    System.out.println("副本失败原因：" + reason);
                }
            }
        }

    }

    /**
     * 方式二 新增数据  upsert语法 不存在就新增,存在就更新
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
            if ("DELETED".equals(statueName)) {
                System.out.println("id-->" + id + "<--" + "删除成功");
            }
            if ("NOT_FOUND".equals(statueName)) {
                System.out.println("id-->" + id + "<--" + "删除失败 未找到相关数据");
            }
        }
    }


    /**
     * 基于scroll进行滚动查询
     */
    public void scrollQuery() throws IOException, InvocationTargetException, IllegalAccessException {

        //1.建立查询索引
        SearchRequest searchRequest = new SearchRequest("user");

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
        searchSourceBuilder.fetchSource(new String[]{"name", "age", "id"}, null);
        //排序
        searchSourceBuilder.sort("age", SortOrder.DESC);
        searchRequest.source(searchSourceBuilder);

        //4.进行搜索
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
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

            SearchResponse nextResponse = restHighLevelClient.scroll(searchScrollRequest, RequestOptions.DEFAULT);
            scrollId = nextResponse.getScrollId();
            hits = nextResponse.getHits().getHits();
            hitsList = Arrays.stream(hits).collect(Collectors.toList());
            resultSearchHit.addAll(hitsList);
        }

        //6.及时清除es快照，释放资源
        ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
        clearScrollRequest.addScrollId(scrollId);
        boolean succeeded = restHighLevelClient.clearScroll(clearScrollRequest, RequestOptions.DEFAULT).isSucceeded();
        System.out.println("succeeded:" + succeeded);

        //7.最后获取到的数据进行遍历转义成对象
        ArrayList<Person> adminLogs = new ArrayList<>();
        for (SearchHit hit : resultSearchHit) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            Person person = new Person();
            BeanUtilsBean.getInstance().populate(person, sourceAsMap);
            adminLogs.add(person);
        }

        //循环展示数据
        adminLogs.forEach(System.out::println);

    }


    /**
     * 基于temp模板进行搜索
     */
    public void getSearchTemplate() throws IOException {
        /**
         * 基于temp模板进行搜索
         */
        SearchTemplateRequest request = new SearchTemplateRequest();
        //调用模板名称
        request.setScript("page_query_by_bard");
        request.setScriptType(ScriptType.STORED);
        //封装params参数
        Map<String, Object> params = new HashMap<>();
        params.put("key", "name");
        params.put("value", "小明");
        params.put("size", 5);
        request.setScriptParams(params);
        //添加需要搜索的index
        request.setRequest(new SearchRequest("user"));
        //搜索
        SearchTemplateResponse searchTemplateResponse = restHighLevelClient.searchTemplate(request, RequestOptions.DEFAULT);
        SearchHit[] hits = searchTemplateResponse.getResponse().getHits().getHits();
        for (SearchHit hit : hits) {
            System.out.println(hit.getSourceAsString());
        }
    }


    /**
     * 高亮搜索
     */
    public void highlightQuery() throws IOException {
        //1.建立查询索引
        SearchRequest searchRequest = new SearchRequest("user");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("name", "小"));
        //2.高亮搜索
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        //设置高亮的字段
        HighlightBuilder.Field highlightTitle = new HighlightBuilder.Field("name");
        //字段高亮显示类型，默认用标签包裹高亮字词
//		highlightTitle.highlighterType("unified");
        //自定义高亮显示
        highlightTitle.preTags("<span style='color:red'>");
        highlightTitle.postTags("</span>");
        highlightBuilder.field(highlightTitle);
        searchSourceBuilder.highlighter(highlightBuilder);

        //3.封装条件进入request
        searchRequest.source(searchSourceBuilder);

        //4.进行查询
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = searchResponse.getHits();
        //获取高亮结果
        for (SearchHit hit : hits.getHits()) {
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField highlight = highlightFields.get("name");
            Text[] fragments = highlight.fragments();
            String fragmentString = fragments[0].string();
            System.out.println("输出结果:" + fragmentString);
        }
    }


    /**
     * 聚合查询
     */
    public void aggSearch() {
        //创建查询request 并且配上index
        SearchRequest searchRequest = new SearchRequest("user");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        //创建聚合语法  聚合的name  和聚合的字段
        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms("by_age").field("age");
        //添加到查询源中
        sourceBuilder.aggregation(termsAggregationBuilder);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        //添加到查询request
        searchRequest.source(sourceBuilder);

        try {
            //查询
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            //获取当前聚合搜索结果
            Aggregations aggregations = searchResponse.getAggregations();
            //获取当前层级的聚合
            Map<String, Aggregation> stringAggregationMap = aggregations.asMap();
            //获取指定聚合名称的结果集
            ParsedLongTerms parsedLongTerms = (ParsedLongTerms) stringAggregationMap.get("by_age");
            //遍历结果集渲染的列表
            List<? extends Terms.Bucket> buckets = parsedLongTerms.getBuckets();

            Map<Integer, Long> map = new HashMap<>();
            //遍历聚合查询的结果
            for (Terms.Bucket bucket : buckets) {
                long docCount = bucket.getDocCount();//个数
                Number keyAsNumber = bucket.getKeyAsNumber();//年龄
                System.err.println(keyAsNumber + "岁的有" + docCount + "个");
                map.put(keyAsNumber.intValue(), docCount);
            }
            System.out.println("聚合查询成功");


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 经纬度查询 location
     */
    public void locationSearch() {

        //拼接条件
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
//        QueryBuilder isdeleteBuilder = QueryBuilders.termQuery("isdelete", false);
        // 以某点为中心，搜索指定范围   字段名称
        GeoDistanceQueryBuilder distanceQueryBuilder = new GeoDistanceQueryBuilder("location");
        //构建一个带你
        distanceQueryBuilder.point(42.12D, -73);
        //查询单位：km
        distanceQueryBuilder.distance(100, DistanceUnit.KILOMETERS);
        boolQueryBuilder.filter(distanceQueryBuilder);
//        boolQueryBuilder.must(isdeleteBuilder);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQueryBuilder);

        //查询Request 附带index
        SearchRequest searchRequest = new SearchRequest("my_index");
        searchRequest.source(searchSourceBuilder);

        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits hits = searchResponse.getHits();
            List<SearchHit> hitsList = Arrays.stream(hits.getHits()).collect(Collectors.toList());
            for (int i = 0; i < hitsList.size(); i++) {
                Map<String, Object> sourceAsMap = hitsList.get(i).getSourceAsMap();
                System.out.print("输出语句:" + sourceAsMap.get("text") + "---");
                //获取具体坐标
                Map<String, Double> localtion = (Map<String, Double>) MapUtils.getMap(sourceAsMap, "location");
                System.out.print("当前位置" + localtion.get("lat") + "," + localtion.get("lon"));
                System.out.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * ***************************************************************************************************************
     * ***********************************更多的搜索语法**************************************************************
     * ***********************************更多的搜索语法**************************************************************
     * ***********************************更多的搜索语法**************************************************************
     * ***************************************************************************************************************
     */
    public void SearchMove() {
        //1.建立查询索引
        SearchRequest searchRequest = new SearchRequest("user");

        //2.搜索语法
        //封装查询条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //查询所有
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());

        //match查询
        searchSourceBuilder.query(QueryBuilders.matchQuery("name", "小明"));

        //term查询
        searchSourceBuilder.query(QueryBuilders.termQuery("name", "小明"));


        //2.1 bool语法查询 start
        // 绑定查询条件
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // status字段为301或302
        boolQueryBuilder.must(QueryBuilders.termsQuery("status.keyword", new String[]{"301", "302"}));
        // args字段包含786754748671257
        boolQueryBuilder.must(QueryBuilders.matchPhraseQuery("args", "786754748671257"));
        // 时间大于等于2020-05-21 00:00:00，小于2020-05-22 00:00:00
        boolQueryBuilder.must(QueryBuilders.rangeQuery("@timestamp").gte(("2020-05-21 00:00:00")).lt(("2020-05-22 00:00:00")));

        /**
         *********
         * 该部分可以放入boolQueryBuilder中
         */
        //范围查询
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("birthday").from("1991-01-01").to("2010-10-10").format("yyyy-MM-dd");
        //闭区间查询
        QueryBuilder queryBuilder0 = QueryBuilders.rangeQuery("fieldName").from("fieldValue1").to("fieldValue2");
        //开区间查询
        QueryBuilder queryBuilder1 = QueryBuilders.rangeQuery("fieldName").from("fieldValue1").to("fieldValue2").includeUpper(false).includeLower(false);//默认是true，也就是包含
        //大于
        QueryBuilder queryBuilder2 = QueryBuilders.rangeQuery("fieldName").gt("fieldValue");
        //大于等于
        QueryBuilder queryBuilder3 = QueryBuilders.rangeQuery("fieldName").gte("fieldValue");
        //小于
        QueryBuilder queryBuilder4 = QueryBuilders.rangeQuery("fieldName").lt("fieldValue");
        //小于等于
        QueryBuilder queryBuilder5 = QueryBuilders.rangeQuery("fieldName").lte("fieldValue");


        //精准查询
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("name.keyword", "小明");
        //前缀查询
        PrefixQueryBuilder prefixQueryBuilder = QueryBuilders.prefixQuery("name.keyword", "张");
        //通配符查询
        WildcardQueryBuilder wildcardQueryBuilder = QueryBuilders.wildcardQuery("name.keyword", "*三");
        //模糊查询
        FuzzyQueryBuilder fuzzyQueryBuilder = QueryBuilders.fuzzyQuery("name", "三");
        //按照年龄排序
        FieldSortBuilder fieldSortBuilder = SortBuilders.fieldSort("age");
        //从小到大排序
        fieldSortBuilder.sortMode(SortMode.MIN);
        //and or  查询
        boolQueryBuilder.must(rangeQueryBuilder).should(prefixQueryBuilder);
        // 2.1 bool语法查询   绑定bool query   end
        searchSourceBuilder.query(boolQueryBuilder).sort(fieldSortBuilder);//多条件查询


        //高亮搜索
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        HighlightBuilder.Field highlightTitle = new HighlightBuilder.Field("title");
        //字段高亮显示类型，默认用标签包裹高亮字词
        highlightTitle.highlighterType("unified");
        highlightBuilder.field(highlightTitle);
        searchSourceBuilder.highlighter(highlightBuilder);

        // 准确计数
        searchSourceBuilder.trackTotalHits(true);
        // 超时时间60s
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        //设置返回字段和排除字段
        searchSourceBuilder.fetchSource(new String[]{"name", "age", "id"}, null);

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
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            SearchHit[] hits = searchResponse.getHits().getHits();
            //获取返回值
            List<SearchHit> hitsList = Arrays.stream(hits).collect(Collectors.toList());
            //7.最后获取到的数据进行遍历转义成对象
            ArrayList<Person> adminLogs = new ArrayList<>();
            //根据返回值转换为实体类
            for (SearchHit hit : hitsList) {
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                Person person = new Person();
                BeanUtilsBean.getInstance().populate(person, sourceAsMap);
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

