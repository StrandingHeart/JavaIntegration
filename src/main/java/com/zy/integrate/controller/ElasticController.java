package com.zy.integrate.controller;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.zy.integrate.domain.AttrDTO;
import com.zy.integrate.domain.EsDemoPO;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.Script;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 记载elasticSearch的update、insert、delete操作以及bulk操作
 * @author zhangyong
 * Created on 2021-03-01
 */
@RestController
public class ElasticController {

    private static final Logger logger = LoggerFactory.getLogger(ElasticController.class);

    /**
     * 索引名称
     */
    public static final String ES_INDEX = "zy_test_es";

    /**
     * 创建索引mapping的properties信息(类似MySQL中的字段)
     */
    private static final String INDEX_PROPERTY = "{\"properties\": {\"id\":{\"type\":\"keyword\"},\"businessId\":{\"type\": \"long\"},\"signalList\":{\"type\":\"text\"},\"name\":{\"type\":\"text\",\"analyzer\": \"standard\"},\"age\":{\"type\":\"integer\"},\"attribute\":{\"type\": \"nested\",\"properties\": {\"language\":{\"type\":\"text\"},\"money\":{\"type\":\"double\"}}}}}";
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 创建索引，这个方法创建设计好的mapping并指定索引的shard和副本
     * mapping中的属性，有嵌套结构，也有测试模糊搜索的text类型，以及默认的分词器standard，可以使用中文支持好的分词器 ik_max_word 下载安装插件
     * 当然如果你用kibana或者curl啥的通过API创建也行
     * PUT zy_test_es {"settings":{SETTING},"mapping":{INDEX_PROPERTY}}
     * @author zhangyong
     * 2021/3/1
     */
    @PostMapping("/es-index-create")
    public Boolean createIndex(){
        GetIndexRequest request = new GetIndexRequest(ES_INDEX);
        try {
            boolean exists = restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
            if (exists){
                logger.info("索引已经存在了: "+ES_INDEX);
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(ES_INDEX);
        // GET zy_test_es/_mapping 获取mapping信息
        Settings settings = Settings.builder()
                .put("index.number_of_shards", 1)
                .put("index.number_of_replicas", 0).build();
        createIndexRequest.settings(settings);
        // 设置字段映射，如果不设置的话，es可以自动更新(开启dynamic)插入文档的时候会根据文档自动生成，但是可能会有偏差，所以还是自己设置吧
        createIndexRequest.mapping(INDEX_PROPERTY, XContentType.JSON);
        try {
            CreateIndexResponse response = restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
            if (response.isAcknowledged()){
                logger.info("索引创建完毕: "+ES_INDEX);
            }
            return response.isAcknowledged();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 删除索引
     * @author zhangyong
     * 2021/3/1
     */
    @PostMapping("/es-index-delete")
    public Boolean deleteIndex(){
        GetIndexRequest request = new GetIndexRequest(ES_INDEX);
        try {
            boolean exists = restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
            if (!exists){
                logger.info("索引不存在: "+ES_INDEX);
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(ES_INDEX);
        try {
            AcknowledgedResponse response = restHighLevelClient.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
            if (response.isAcknowledged()){
                logger.info("索引删除完毕: "+ES_INDEX);
            }
            return response.isAcknowledged();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * 保存文档，指定id(es默认会生成，但是随机的东西跟我们业务联系比较难)
     * 因为保存文档在es中的叫法是索引一个文档，所以就叫indexRequest了
     * @author zhangyong
     * 2021/3/1
     */
    @PostMapping("/es-doc-insert")
    public Boolean saveDocument(){
        long id = System.currentTimeMillis()%100000;
        IndexRequest indexRequest = new IndexRequest(ES_INDEX);
        // 获取测试文档对象
        Map<String, Object> map = getObject(id);
        indexRequest.id(String.valueOf(id)).source(map);
        try {
            restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 批量导入，一次连接
     * 批量操作适用于增删改操作，不能用于查询
     * 对于批量查询而言有一个multiGet操作，是通过id查询的，也有专门批量查定制化search的multiSearch
     * @author zhangyong
     * 2021/3/1
     */
    @PostMapping("/es-bulk-insert")
    public Boolean bulkInsertDocument(){
        BulkProcessor bulkProcessor;
        BulkProcessor.Listener listener = new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long executionId, BulkRequest request) {
                int numberOfActions = request.numberOfActions();
                logger.debug("Executing bulk [{}] with {} requests",
                        executionId, numberOfActions);
            }
            @Override
            public void afterBulk(long executionId, BulkRequest request,
                                  BulkResponse response) {
                if (response.hasFailures()) {
                    logger.warn("Bulk [{}] executed with failures", executionId);
                } else {
                    logger.debug("Bulk [{}] completed in {} milliseconds",
                            executionId, response.getTook().getMillis());
                }
            }
            @Override
            public void afterBulk(long executionId, BulkRequest request,
                                  Throwable failure) {
                logger.error("Failed to execute bulk", failure);
            }
        };
        BulkProcessor.Builder builder = BulkProcessor.builder(
                (request, bulkListener) -> restHighLevelClient.bulkAsync(request, RequestOptions.DEFAULT, bulkListener),listener);
        // 每500个request flush一次
        builder.setBulkActions(500);
        // bulk数据每达到1MB flush一次
        builder.setBulkSize(new ByteSizeValue(1L, ByteSizeUnit.MB));
        // 0代表同步提交即只能提交一个request；即同步bulk操作，例如500个提交，那就500提交完再继续，异步不等
        // 1代表当有一个新的bulk正在累积时，1个并发请求可被允许执行
        builder.setConcurrentRequests(1);
        // 每10s刷一次数据提交
        builder.setFlushInterval(TimeValue.timeValueSeconds(10L));
        // 设置策略
        // Set a constant back off policy that initially waits for 1 second and retries up to 3 times.
        builder.setBackoffPolicy(BackoffPolicy.constantBackoff(TimeValue.timeValueSeconds(1L), 3));
        bulkProcessor = builder.build();
        // 要插入的数量
        int loop = 100;
        for (int i = 0; i < loop; i++) {
            long id = 10000+i;
            IndexRequest indexRequest = new IndexRequest(ES_INDEX);
            // The number of object passed must be even but was [1]
            Map<String, Object> map = getObject(id);
            indexRequest.id(String.valueOf(id)).source(map);
            bulkProcessor.add(indexRequest);
            if (i%2 == 0){
                // 批量操作，控制刷新(提交)时刻；
                bulkProcessor.flush();
            }
        }
        bulkProcessor.flush();
        try {
            // 关闭bulk连接
            bulkProcessor.awaitClose(100, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 创建测试对象
     * @author zhangyong
     * 2021/3/2
     */
    private Map<String,Object> getObject(long id){
        AttrDTO attrDTO = new AttrDTO("JAVA-C", BigDecimal.valueOf(10000).add(BigDecimal.valueOf(id)));
        List<String> signal = new ArrayList<>();
        signal.add("qwe");
        signal.add("asd");
        signal.add(String.valueOf(id));
        EsDemoPO demo = new EsDemoPO(String.valueOf(id),String.valueOf(id),attrDTO,(int)(id%100),"sari-"+id,signal);
        System.out.println(JSONObject.toJSONString(demo));
        // The number of object passed must be even but was [1]
        // 这块如果用json形式会报错 需要在source的时候，改用map
        Gson gson = new Gson();
        String jsonString = JSONObject.toJSONString(demo);
        return gson.fromJson(jsonString, Map.class);
    }

    /**
     * 通过id批量查文档
     * @author zhangyong
     * 2021/3/2
     */
    @PostMapping("/es-multi-get")
    public MultiGetItemResponse[] multiGet(@RequestBody List<String> ids){
        MultiGetRequest request = new MultiGetRequest();
        if (CollectionUtils.isEmpty(ids)){
            return null;
        }
        for (String id : ids) {
            request.add(new MultiGetRequest.Item(ES_INDEX, id));
        }
        try {
            MultiGetResponse responses = restHighLevelClient.mget(request, RequestOptions.DEFAULT);
            return responses.getResponses();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 更新文档 通过id
     * 里面还有upsert 不存在就插入(用id来判存在的)
     * update table set k1=v1... where id = x
     * @author zhangyong
     * 2021/3/1
     */
    @PostMapping("/es-doc-update")
    public Boolean updateDocument(@RequestParam("id")String id) throws IOException {
        if (Objects.isNull(id)){
            return false;
        }
        AttrDTO attrDTO = new AttrDTO("JAVA123", BigDecimal.valueOf(15001));
        List<String> signal = new ArrayList<>();
        signal.add("asd");
        signal.add("qwe");
        // 不为空的字段就会覆盖旧值
        EsDemoPO demo = new EsDemoPO("123",null,attrDTO,null,"update_"+id,signal);
        UpdateRequest updateRequest = new UpdateRequest(ES_INDEX,id);
//        UpdateRequest updateRequest = new UpdateRequest(ES_INDEX,id+"123");//换成开启这行在upsert时会新增source
        Gson gson = new Gson();
        String jsonString = JSONObject.toJSONString(demo);
        // doc语法的更新就是该字段如果存在就覆盖，该字段没有就在这个source加上该字段
        updateRequest.doc(gson.fromJson(jsonString, Map.class));
        // 如果不存在就插入，存在就更新(这里是指id的这个文档是否存在)
        updateRequest.docAsUpsert(true);
        UpdateResponse response = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
        return response.status().equals(RestStatus.OK);
    }

    /**
     * 删除文档 通过id
     * delete from table where id = x
     * @author zhangyong
     * 2021/3/1
     */
    @PostMapping("/es-doc-delete")
    public Boolean deleteDocument(@RequestParam("id")String id){
        if (Objects.isNull(id)){
            return false;
        }
        DeleteRequest deleteRequest = new DeleteRequest(ES_INDEX,id);
        try {
            DeleteResponse response = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
            return response.status().equals(RestStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 通过查询条件删除
     * delete from table where age = 10;
     * @author zhangyong
     * 2021/3/2
     */
    @PostMapping("/es-doc-delete-query")
    public Long deleteDocumentByQuery(){
        DeleteByQueryRequest delete = new DeleteByQueryRequest(ES_INDEX);
        // 这里用一下BoolQueryBuilder 就是我们用的bool DSL语法
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.must(QueryBuilders.termQuery("age",10));
        delete.setQuery(boolQueryBuilder);
        try {
            BulkByScrollResponse response = restHighLevelClient.deleteByQuery(delete, RequestOptions.DEFAULT);
            return response.getDeleted();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0L;
    }

    /**
     * 通过查询条件更新
     * update table set name = 'zhangyong',set money = money+1 where age = 18;
     * @author zhangyong
     * 2021/3/2
     */
    @PostMapping("/es-doc-update-query")
    public Long updateDocumentByQuery(){
        UpdateByQueryRequest update = new UpdateByQueryRequest(ES_INDEX);
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.must(QueryBuilders.termQuery("age",69));
        update.setQuery(boolQueryBuilder);
        // es可以利用脚本更新 ctx是上下文然后 . 字段就行了
        update.setScript(new Script("ctx._source['name']='zhangyong';ctx._source.attribute.money++;"));
        try {
            BulkByScrollResponse response = restHighLevelClient.updateByQuery(update, RequestOptions.DEFAULT);
            return response.getUpdated();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0L;
    }
}
