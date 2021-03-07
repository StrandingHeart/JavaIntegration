package com.zy.integrate.controller;

import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.PipelineAggregatorBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregator;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.pipeline.BucketSelectorPipelineAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.ScriptSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.*;

/**
 * 记载查询操作
 * @author zhangyong
 * Created on 2021-03-03
 */
@RestController
public class ElasticSearchController {

    private static final Logger logger = LoggerFactory.getLogger(ElasticSearchController.class);

    @Autowired
    private RestHighLevelClient client;

    /**
     * 记录一些常用的过滤查询手段。包括bool查询中must、mustNot、范围查询、嵌套查询、范围查询
     * 搜索也可以设置搜索模板 searchTemplate 这个还没真正使用过，看API没看出来哪里好，有替换变量的功能
     * @author zhangyong
     * 2021/3/3
     */
    @GetMapping("/es-normal-search")
    public Map<String,Map<String, Object>> normalSearch(){
        // 查询请求 类似 where
        SearchRequest searchRequest = new SearchRequest(ElasticController.ES_INDEX);
        // 构建查询文档条件的builder构建器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // bool 查询
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.mustNot(QueryBuilders.termQuery("businessId",10000));
        // 范围查询
        boolQueryBuilder.must(QueryBuilders.rangeQuery("age").gte(0).lt(20));
        // 集合查询，满足一个集合属性就会返回
        boolQueryBuilder.must(QueryBuilders.termsQuery("signalList","asd"));
        // 嵌套查询
        BoolQueryBuilder nestBoolQuery = new BoolQueryBuilder();
        // 需要注意的是term查询如果是字符串是会有大小写问题的，match查询可以避免这个问题，把matchQuery改成termQuery会发现搜不到，因为JAVA是大写
        // term表示词条查询，被查询的字段不会进行分词，需要完全匹配。
        nestBoolQuery.must(QueryBuilders.matchQuery("attribute.language","JAVA"));
        NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery("attribute", nestBoolQuery, ScoreMode.None);
        boolQueryBuilder.must(nestedQueryBuilder);
        logger.info(boolQueryBuilder.toString());
        // 设置查询条件，设置分页数量 类似 limit 0,20
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(20);
        // 设置展示全部total 不然如果你数据比较多，超过10000了就会展示10000而不是真实的数据
        searchSourceBuilder.trackTotalHits(true);
        // 排序
        searchSourceBuilder.sort("id", SortOrder.DESC);
        searchRequest.source(searchSourceBuilder);
        return search(searchRequest);
    }

    /**
     * 搜索
     * @author zhangyong
     * 2021/3/3
     */
    private Map<String,Map<String, Object>> search(SearchRequest searchRequest){
        Map<String,Map<String, Object>> res = new LinkedHashMap<>(32);
        try {
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits hits = response.getHits();
            SearchHit[] hitsHits = hits.getHits();
            if(hitsHits !=null && hitsHits.length > 0){
                for (SearchHit hit : hitsHits) {
                    Map<String, Object> source = hit.getSourceAsMap();
                    if (!CollectionUtils.isEmpty(source) && Objects.nonNull(source.get("businessId"))){
                        res.put(source.get("businessId").toString(),source);
                    }
                }
            }
            return res;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }


    /**
     * 模糊匹配---也是es最大的优势，全文检索模糊匹配
     * 投影返回，只返回businessId和name
     * @author zhangyong
     * 2021/3/3
     */
    @GetMapping("/es-fuzzy-match")
    public Map<String, Map<String, Object>> fuzzyMatching(){
        SearchRequest searchRequest = new SearchRequest(ElasticController.ES_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //GET zy_test_es/_analyze
        //{"field": "name","text": "sari-1111"}
        //以上是获取分词信息的查询DSL，分词分啥样跟你选的分词器直接相关，minimumShouldMatch是设置匹配程度，可以是百分比,operator是or/and默认是or
        //表示包含多少分词，可以是百分比，也可以是数字  比方说是 2代表 包含两个分词的文档才返回。
        //还可以是百分比，比如 50%代表 sari和1111占全部分词数量的一半   2*50%向下取整，乘积为0时就是1了。百分比就是你输入的话进行分词之后要满足百分之多少的分词
        boolQueryBuilder.must(QueryBuilders.matchQuery("name","sari").minimumShouldMatch("1").operator(Operator.OR));
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.size(20);
        // 投影操作
        searchSourceBuilder.fetchSource(new String[]{"name","businessId"},null);
        // 设置随机排序,分页返回时是随机返回的
        Script script = new Script("Math.random()");
        ScriptSortBuilder sortBuilder = new ScriptSortBuilder(script, ScriptSortBuilder.ScriptSortType.NUMBER);
        searchSourceBuilder.sort(sortBuilder);
        searchRequest.source(searchSourceBuilder);
        return search(searchRequest);
    }


    /**
     * 批量自定义查询，有时我们有批量搜索的场景，multiSearch只发送一次HTTP请求比起for发请求好太多
     * @author zhangyong
     * 2021/3/3
     */
    @GetMapping("/es-multi-search")
    public Map<String,Map<String, Object>> multiSearch(){
        MultiSearchRequest multiSearchRequest = new MultiSearchRequest();
        SearchRequest searchRequest1 = new SearchRequest(ElasticController.ES_INDEX);
        SearchSourceBuilder sourceBuilder1 = new SearchSourceBuilder();
        sourceBuilder1.query(QueryBuilders.boolQuery().must(QueryBuilders.termQuery("businessId",10004)));
        searchRequest1.source(sourceBuilder1);
        multiSearchRequest.add(searchRequest1);

        SearchRequest searchRequest2 = new SearchRequest(ElasticController.ES_INDEX);
        SearchSourceBuilder sourceBuilder2 = new SearchSourceBuilder();
        sourceBuilder2.query(QueryBuilders.boolQuery().must(QueryBuilders.termQuery("businessId",10002)));
        searchRequest2.source(sourceBuilder2);
        multiSearchRequest.add(searchRequest2);

        SearchRequest searchRequest3 = new SearchRequest(ElasticController.ES_INDEX);
        SearchSourceBuilder sourceBuilder3 = new SearchSourceBuilder();
        sourceBuilder3.query(QueryBuilders.boolQuery().must(QueryBuilders.termQuery("businessId",10001)));
        searchRequest3.source(sourceBuilder3);
        multiSearchRequest.add(searchRequest3);
        Map<String,Map<String, Object>> allRes = new LinkedHashMap<>(32);

        try {
            MultiSearchResponse msearch = client.msearch(multiSearchRequest, RequestOptions.DEFAULT);
            MultiSearchResponse.Item[] responses = msearch.getResponses();
            for (MultiSearchResponse.Item res : responses) {
                if (Objects.nonNull(res.getFailure())){
                    logger.warn("批量请求异常: "+res.getFailureMessage());
                    continue;
                }
                SearchResponse response = res.getResponse();
                SearchHits hits = response.getHits();
                SearchHit[] hitsHits = hits.getHits();
                if(hitsHits !=null && hitsHits.length > 0){
                    for (SearchHit hit : hitsHits) {
                        Map<String, Object> source = hit.getSourceAsMap();
                        if (!CollectionUtils.isEmpty(source) && Objects.nonNull(source.get("businessId"))){
                            allRes.put(source.get("businessId").toString(),source);
                        }
                    }
                }
            }
            return allRes;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return allRes;
    }


    /**
     * 内嵌聚合查询 having查询
     * 聚合算 每个年龄 平均薪水 > 10000的数量，count_age是数量
     * select age,count(age) as count_age,avg(attribute.money) as average_money
     * from table
     * group by age
     * having average_money > 20050 and count_age > 0
     * @author zhangyong
     * 2021/3/3
     */
    @GetMapping("/es-aggregation-search")
    public Map<String,Long> aggregationSearch(){
        SearchRequest searchRequest = new SearchRequest(ElasticController.ES_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 聚合查询，按age字段分组，返回名称为count_age 返回100条(因为不指定不会将数据都返回) field指定group by 的字段
        TermsAggregationBuilder aggregationBuilder = AggregationBuilders.terms("count_age").field("age").size(100);
        searchSourceBuilder.aggregation(aggregationBuilder);

        // 能放到select 中的聚合操作,嵌套聚合,其中average_money是select的，nest_average_money是对象字段名字
        aggregationBuilder.subAggregation(AggregationBuilders.nested("average_money","attribute")
                .subAggregation(AggregationBuilders.avg("nest_average_money").field("attribute.money")));
        // 非嵌套的聚合
        /// aggregationBuilder.subAggregation(AggregationBuilders.avg("average_money").field("age"));
        // 声明BucketPath，用于后面的bucket筛选
        Map<String, String> bucketsPathsMap = new HashMap<>(8);
        bucketsPathsMap.put("count_age", "_count");
        // 如果解开"非嵌套的聚合"的代码，average_money.nest_average_money需要替换成average_money
        bucketsPathsMap.put("average_money", "average_money.nest_average_money");
        // 这个里面的params 执行的就是bucketsPathsMap 中的参数,bucketsPathsMap中是查出来值，可以理解是select的结果变量,对于内嵌结构语法如上
        Script script = new Script("params.average_money >= 20050 && params.count_age > 0");
        //构建bucket选择器
        BucketSelectorPipelineAggregationBuilder bpab = PipelineAggregatorBuilders.bucketSelector("having", bucketsPathsMap, script);
        // 加having条件
        aggregationBuilder.subAggregation(bpab);
        // 不要source数据，只要聚合数据，两者分开返回的，互不影响；只要聚合数据，这个数据就没用就不需要返回了、
        searchSourceBuilder.size(0);
        System.out.println(searchSourceBuilder.toString());
        searchRequest.source(searchSourceBuilder);
        Map<String,Long> map = new LinkedHashMap<>();
        try {
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
            Aggregations aggregations = response.getAggregations();
            Terms terms = aggregations.get("count_age");
            for (Terms.Bucket bucket : terms.getBuckets()) {
                map.put(bucket.getKeyAsString(),bucket.getDocCount());
                Map<String, Aggregation> asMap = bucket.getAggregations().getAsMap();
                // 这个需要注意 强转的类型
                ParsedNested averageMoney = (ParsedNested) asMap.get("average_money");
                Avg avg = averageMoney.getAggregations().get("nest_average_money");
                System.out.println(avg.value());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 高亮搜索
     * @author zhangyong
     * 2021/3/3
     */
    @GetMapping("/es-high-light")
    public Map<String,List<String>> highlightSearch(){
        SearchRequest searchRequest = new SearchRequest(ElasticController.ES_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 模糊匹配搜name 包含 sari的 或者language是Java的
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.should(QueryBuilders.matchQuery("name","sari"));
        BoolQueryBuilder nestBool = new BoolQueryBuilder();
        nestBool.must(QueryBuilders.matchQuery("attribute.language","java"));
        boolQueryBuilder.should(QueryBuilders.nestedQuery("attribute",nestBool,ScoreMode.None));
        searchSourceBuilder.query(boolQueryBuilder);
        // 高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        // 高亮 name 字段搜出来的结果
        HighlightBuilder.Field highlightName =new HighlightBuilder.Field("name");
        HighlightBuilder.Field highlightLanguage =new HighlightBuilder.Field("attribute.language");
        // 设置
        highlightName.highlighterType("unified");
        highlightBuilder.fields().add(highlightName);
        highlightBuilder.fields().add(highlightLanguage);
        // 设置要高亮字段的标签 默认是<em> 这个标签tag可以根据前端框架css来写
        highlightBuilder.preTags("<span style=\"color:#F56C6C\">");
        highlightBuilder.postTags("</span>");
        searchSourceBuilder.highlighter(highlightBuilder);
        searchRequest.source(searchSourceBuilder);
        System.out.println(searchSourceBuilder.toString());
        Map<String, List<String>> res = new HashMap<>();
        try {
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits hits = response.getHits();
            SearchHit[] hitsHits = hits.getHits();
            if(hitsHits !=null && hitsHits.length > 0){
                for (SearchHit hit : hitsHits) {
                    Map<String, Object> asMap = hit.getSourceAsMap();
                    String businessId = asMap.get("businessId").toString();
                    Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                    // 我们得到高亮的字段后就可以返回给前端了，前端展示的就是高亮的了
                    HighlightField highLightName = highlightFields.get("name");
                    HighlightField language = highlightFields.get("attribute.language");
                    if (Objects.nonNull(highLightName) && Objects.nonNull(language)){
                        Text[] fragments = highLightName.getFragments();
                        Text[] languageFragments = language.getFragments();
                        List<String> high = new ArrayList<>();
                        if (fragments.length > 0){
                            high.add(fragments[0].toString());
                        }
                        if (languageFragments.length > 0){
                            high.add(languageFragments[0].toString());
                        }
                        res.put(businessId,high);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

}
