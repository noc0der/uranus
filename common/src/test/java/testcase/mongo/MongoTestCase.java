package testcase.mongo;

import java.sql.Timestamp;
import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

@ContextConfiguration(locations = { "classpath:/spring/app.test.xml" })
public class MongoTestCase extends AbstractTestNGSpringContextTests{
	@Autowired
	@Qualifier("mongoTemplate")
	protected MongoTemplate operations;
	//
	/**
	 * 验证一下一个复杂的结构进mongo了，出来是个啥
	 */
	@Test
	public void hello(){
		//先清除
		String coll_code = "json_log";
		operations.dropCollection(coll_code);
		//构建一个jsonobject，存进去
		JSONObject jo = new JSONObject();
		jo.put("str_key", "string_1");
		jo.put("ts_key", new Timestamp(System.currentTimeMillis()));
		JSONObject subjo = new JSONObject();
		subjo.put("sub_key_str", "sub_key_str_value_1");
		jo.put("subjo", subjo);
		JSONArray sub_list_simple = new JSONArray();//简单的字符串
		sub_list_simple.add("list_1");
		sub_list_simple.add("list_2");
		jo.put("sub_list_simple",sub_list_simple);
		//
		JSONArray sub_list_complex = new JSONArray();//复杂的对象
		//
		JSONObject sub_list_1 = new JSONObject();
		sub_list_1.put("sub_list_1_key_1", "sub_list_1_value_1");
		sub_list_1.put("sub_list_1_key_2", "sub_list_1_value_2");
		sub_list_complex.add(sub_list_1);
		JSONObject sub_list_2 = new JSONObject();
		sub_list_2.put("sub_list_2_key_1", "sub_list_2_value_1");
		sub_list_2.put("sub_list_2_key_2", "sub_list_2_value_2");
		sub_list_complex.add(sub_list_2);
		jo.put("sub_list_complex",sub_list_complex);
		operations.save(jo, coll_code);
		//
		//查出来比较
		Criteria criteria = Criteria.where("str_key").is("string_1");
		Query query = new Query(criteria);
		//
		Object rs = operations.findOne(query, Object.class, coll_code);
		Assert.assertTrue(rs!=null, "估计没查询成功");
		
		Object sub_jo_obj = ((JSONObject)rs).get("subjo");//拿到的是linkedmap而非jsonobject
		Assert.assertTrue(sub_jo_obj instanceof LinkedHashMap,"哎，果然还是linkedmap吧");
		
		JSONObject sub_jo_jo = ((JSONObject)rs).getJSONObject("subjo");
		Assert.assertTrue(sub_jo_jo instanceof JSONObject,"不负众望的把二级搞出来了");
//		Object sub_jo_obj = ((JSONObject)rs).getJSONObject("subjo");
		
		JSONArray ja = ((JSONObject)rs).getJSONArray("sub_list_complex");
		Assert.assertTrue(ja instanceof JSONArray,"sub_list_complex出来了，还算正常吧");

		//验证list中的element是不是预期的
		Assert.assertTrue(ja.get(0) instanceof JSONObject,"sub_list_complex.get(0)/Jsonobject,出来了，还算正常吧");
		
	}
}
