package testcase.persistence;

import net.popbean.pf.entity.helper.JO;
import net.popbean.pf.exception.BusinessError;
import net.popbean.pf.persistence.SQLStruct;
import net.popbean.pf.persistence.helper.DaoHelper;
import net.popbean.pf.testcase.TestHelper;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.alibaba.fastjson.JSONObject;

/**
 * sql的结构转化测试，这是一个内部的数据结构测试，外部并不推荐使用
 * - 字符串替换(单引号)
 * - 可选条件
 * - 性能测试
 * @author to0ld
 *
 */
public class SqlStructTestCase {
	@Test
	public void replace(){
		try {
			//
			JSONObject vo = JO.gen("pk_key1","soso");
			//
			String sql = "select * from some_tab where pk_key1=${pk_key1}";
			String target = "select * from some_tab where pk_key1=?";
			SQLStruct struct = DaoHelper.Sql.parseSQL(sql, vo);
			Assert.assertEquals(target.trim(), struct.nataiveSql.toString().trim(),"这么简单，不可能错吧");
			//可选条件
			sql = "select * from some_tab where 1=1 $[and pk_key1=${pk_key1}]";
			target = "select * from some_tab where 1=1 ";
			struct = DaoHelper.Sql.parseSQL(sql, JO.gen());
			Assert.assertEquals(target.trim(), struct.nataiveSql.toString().trim(),"condition");
			//需要注意的是，如果嵌套的条件中有或，建议还是if-else来拼写吧
			//nested conditon/嵌套可选条件
			sql = "select * from some_tab where 1=1 $[and( 1=1 $[and pk_1=${pk_1}] $[and pk_2=${pk_2}])]";//从效果上看，挺晕的，不够清晰
			target = "select * from some_tab where 1=1";//一根毛都没有的情况
			struct = DaoHelper.Sql.parseSQL(sql, JO.gen());
			Assert.assertEquals(target.trim(), struct.nataiveSql.toString().trim(),"nested condition(空)");
			//
			target = "select * from some_tab where 1=1 and( 1=1 and pk_1=? )";//保留pk_1的情况
			struct = DaoHelper.Sql.parseSQL(sql, JO.gen("pk_1","soso"));
			Assert.assertEquals(target.trim(), struct.nataiveSql.toString().trim(),"条件二选一");
			//
			target = "select * from some_tab where 1=1 and( 1=1 and pk_2=?)";//保留pk_1的情况
			struct = DaoHelper.Sql.parseSQL(sql, JO.gen("pk_2","soso"));
			Assert.assertEquals(target.replaceAll(" ", ""), struct.nataiveSql.toString().replaceAll(" ", ""),"条件二选一");
		} catch (Exception e) {
			Assert.fail(TestHelper.getErrorMsg(e),e);
		}
	}
	@Test
	public void test(){
		try {
			String cond_pre = "$[";
			String sql = "select * from some_tab where 1=1 $[and( $[pk_1=${pk_1}] $[or pk_2=${pk_2}])]";
			StringBuffer buf = new StringBuffer(sql);
			int start_cond_index = buf.lastIndexOf(cond_pre);// 从尾部开始
			System.out.println(buf);
		} catch (Exception e) {
			Assert.fail(TestHelper.getErrorMsg(e),e);
		}
	}	
}
