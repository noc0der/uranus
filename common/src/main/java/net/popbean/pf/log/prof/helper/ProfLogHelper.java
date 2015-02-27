package net.popbean.pf.log.prof.helper;

import java.lang.management.ManagementFactory;
import java.sql.Timestamp;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import com.alibaba.fastjson.JSONObject;
/**
 * 
 * @author to0ld
 *
 */
public class ProfLogHelper {
	protected static Logger slog = Logger.getLogger("SERVICE");
	//
	protected static ThreadLocal<long[]> _bus = new ThreadLocal<>();
	//
	public static final String PROF_CPU = "PROF_CPU";
	public static final String PROF_TIME = "PROF_TIME";
	public static final String PROF_MEM = "PROF_MEM";
	//
	public static int TIME = 0;
	public static int MEMORY = 1;
	public static int CPU = 2;
	//
	/**
	 * 
	 * @return
	 */
	public static long[] start() {
		long[] start = new long[3];
		start[TIME] = System.currentTimeMillis();
		start[MEMORY] = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
		start[CPU] = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
		//
		return start;
	}
	public static void begin(){
		long[] start = new long[3];
		start[TIME] = System.currentTimeMillis();
		start[MEMORY] = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
		start[CPU] = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
		_bus.set(start);
	}
	/**
	 * 
	 * @param start
	 * @param msg
	 */
	public static void info(long[] start,Object msg){
		long[] sub = cal(start);
		//写入mdc:putProf
		putProf(sub);
		//log
		slog.info(msg);
		//清除mdc:removeProf
		removeProf();
	}
	public static void info(Object msg){
		long[] start = _bus.get();
		if(start == null){
			slog.info(msg);
		}else{
			info(start,msg);
		}
	}
	public static void debug(Object msg){
		long[] start = _bus.get();
		if(start == null){
			slog.debug(msg);
		}else{
			debug(start,msg);
		}
	}
	/**
	 * 
	 * @param start
	 * @param msg
	 */
	public static void debug(long[] start,Object msg){
		long[] sub = cal(start);
		//写入mdc:putProf
		putProf(sub);
		//log
		slog.debug(msg);
		//清除mdc:removeProf
		removeProf();
	}
	/**
	 * 
	 * @param args
	 */
	private static void putProf(long[] args){//没判空，private一下
		MDC.put(PROF_CPU, args[CPU]);
		MDC.put(PROF_TIME, args[TIME]);
		MDC.put(PROF_MEM, args[MEMORY]);		
	}
	/**
	 * 
	 */
	private static void removeProf(){
		MDC.remove(PROF_CPU);
		MDC.remove(PROF_TIME);
		MDC.remove(PROF_MEM);		
	}
	/**
	 * 
	 * @param start
	 */
	public static long[] cal(long[] start){
		long[] end = new long[3];
		end[TIME] = System.currentTimeMillis();
		end[MEMORY] = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
		end[CPU] = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
		//
		end[TIME] = end[TIME] - start[TIME];
		end[MEMORY] = end[MEMORY] - start[MEMORY];
		end[CPU] = (end[CPU] - start[CPU]) / 1000000;
		//
		return end;
	}
	/**
	 * 
	 * @param msg
	 * @return
	 */
	public static JSONObject endJO(long[] start, Object msg) {
		//FIXME clientenv单独放一个拦截器里搞吧
		JSONObject ret = new JSONObject();
		long[] end = new long[3];
		end[TIME] = System.currentTimeMillis();
		end[MEMORY] = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
		end[CPU] = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
		if(msg!=null){
			ret.put("rid", msg);	
		}
		ret.put("time(ms)", (end[TIME] - start[TIME]));
		ret.put("memory(k)", (end[MEMORY] - start[MEMORY])/1000);
		ret.put("cpu(ms)", ((end[CPU] - start[CPU]) / 1000000));
		ret.put("CRT_TS", new Timestamp(System.currentTimeMillis()));
		return ret;
	}
	public static JSONObject endJO(Object msg) {
		long[] start = _bus.get();
		_bus.remove();
		if(start !=null){
			return endJO(start,msg);
		}
		return new JSONObject();//这样其实不好，还不如直接抛出异常呢
	}
}
