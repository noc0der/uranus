package net.popbean.pf.helper;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.popbean.pf.entity.IValueObject;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class ClassRefHelper {
	public final static String PROPERTY_CLASSPATH = "java.class.path";

	private final static String CLASS_FILE_SUFFIX = ".class";

	private final static String JAR_FILE_SUFFIX = ".jar";
	private final static String ZIP_FILE_SUFFIX = ".zip";
	protected static Logger log = Logger.getLogger("SERVICE");//这个将来要换成别的logger
	//
	public static List<String> scanEntity(String package_pref)throws Exception{//扫描classpath获得合格的meta
		try {
			ClassRefFilter filter = new ClassRefFilter();
			filter.addFilterpPackage(package_pref);
			List<ClassRefFilter> filters = new ArrayList<ClassRefFilter>();
			filters.add(filter);
			//FIXME fetchClassByAnnotationWithJarName(Entity.class,filters)
			String[] rs = fetchSubClassByFilterWithJarName(IValueObject.class, filters);
			//过滤掉接口及抽象类
			List<String> ret = new ArrayList<String>();
			if(rs == null){
				return ret;
			}
			boolean f = false;
			for(int i=0,len=rs.length;i<len;i++){
				String[] t = rs[i].split("#@#");
				if(t.length == 1){
					f = isEntityClass(t[0]);//可能还需要将commontablemeta本身排除掉
				}else{
					f = isEntityClass(t[1]);//可能还需要将commontablemeta本身排除掉
				}
				if(f){
					ret.add(rs[i]);
				}
			}
			return ret;
		} catch (Exception e) {
			throw e;
		}
	}
	public static String[] fetchSubclassByJar(Class<?> clazz, String jarName) {
		try {
			String[] rets = fetchSubclassByJar(clazz, jarName,null);
			return rets;
		} catch (Exception e) {//
			e.printStackTrace();
		}
		return null;
	}
	public static String[] fetchSubclassByJar(Class<?> clazz, String jarName, List<String> filters) {
		ZipFile zip = null;
		try {
			zip = new ZipFile(jarName);
			ZipEntry entry = null;
			Enumeration<? extends ZipEntry> entries = zip.entries();
			String entryName = null;
			String className = null;
			int endsLen = CLASS_FILE_SUFFIX.length();// 扩展名的长度，包括点号
			ArrayList<String> list = new ArrayList<String>();
			while (entries.hasMoreElements()) {
				entry = entries.nextElement();
				entryName = entry.getName();
				if (!entry.isDirectory() && entryName.endsWith(CLASS_FILE_SUFFIX)) {
					className = entryName.substring(0, entryName.length() - endsLen);
					className = className.replace(File.separator, ".");
					className = className.replaceAll("/", ".");//纯粹是为了保护，避免\/的处理，可能会损失性能
					//过滤jar包中所需要过滤的包
					if(isFilterPackage(className,filters)){
						if(ClassUtils.isAssignable(Class.forName(className), clazz)) {
							list.add(className);
						}
					}
				}
			}
			if (list.size() > 0) {
				String[] rets = new String[list.size()];
				list.toArray(rets);
				return rets;
			}
		} catch (Exception e) {//
			e.printStackTrace();
		}
		return null;
	}
	public static String[] fetchSubClassByClassPath(Class<?> clazz, String originalpath, String filepath) {
		try {
			File dir = new File(filepath);
			if (!dir.exists()) {
				return null;
			}
			//
			ArrayList<String> list = new ArrayList<String>();
			// 1-过滤掉内部类
			if (dir.isFile() && filepath.endsWith(CLASS_FILE_SUFFIX)) {// 如果当前是一个类名直接处理，如果有个目录就叫.class。。。节哀顺变
				//
				String absoluteClassPath = (new File(originalpath)).getAbsolutePath();
				String absolutePath = dir.getAbsolutePath();
				int ignorePrefixLen = absoluteClassPath.length();
				if (!absoluteClassPath.endsWith(File.separator)) {
					ignorePrefixLen += File.separator.length();
				}
				int classLen = absolutePath.length() - CLASS_FILE_SUFFIX.length();
				//
				String classname = absolutePath.substring(ignorePrefixLen, classLen);
				classname = classname.replace(File.separatorChar, '.');
				if (ClassUtils.isAssignable(Class.forName(classname), clazz)) {
					list.add(classname);
				}
			} else if (dir.isDirectory()) {
				for (File file : dir.listFiles()) {
					String[] rets = fetchSubClassByClassPath(clazz, originalpath, file.getAbsolutePath());
					if (!ArrayUtils.isEmpty(rets)) {
						for (String entry : rets) {
							list.add(entry);
						}
					}
				}
			}
			//
			if (list.size() > 0) {
				String[] rets = new String[list.size()];
				list.toArray(rets);
				return rets;
			}
			return null;
		} catch (Exception e) {// 吃掉异常。。。可乎
			e.printStackTrace();
		}
		return null;
	}
	public static boolean isFilterPackage(String clazzName, List<String> filters) {
		if (filters == null || filters.size()<1) {
			return true;
		}
		for (String filter : filters) {
			if (clazzName.toLowerCase().startsWith(filter.toLowerCase())) {
				return true;
			}
		}
		return false;
	}
	/**
	 * 
	 * @param clazz
	 * @param filters
	 * @return
	 */
	public static String[] fetchSubClassByFilterWithJarName(Class<?> clazz,List<ClassRefFilter> filters) {
		try {
			//获取所需要搜索的全路径
			List<String> paths = fetchSearchPaths(clazz);
			//
			String[] list = null;
			ArrayList<String> total = new ArrayList<String>();
			//
			for (String key : paths) {
				log.debug("<!--path:"+key+"-->");
				list = null;
				if ((key.endsWith(JAR_FILE_SUFFIX) || key.endsWith(ZIP_FILE_SUFFIX))) {// 暂时不考虑zip的情况
					if(filters==null){
						list = fetchSubclassByJar(clazz, key);
					}else{
						for (ClassRefFilter filter : filters) {
							if(isFilterJar(key,filter)){
								list = fetchSubclassByJar(clazz, key, filter.getFilterPackages());
								break;
							}
						}
					}
				} else {// 目录的情况
					list = fetchSubClassByClassPath(clazz, key,key);
				}
				if (!ArrayUtils.isEmpty(list)) {
					for (String entry : list) {
						total.add(key+"#@#"+entry);//为了能追溯到jar，我得把jar的名字带上
					}
				}
			}
			//-------------------------------
			if (total.size() > 0) {
				String[] rets = new String[total.size()];
				total.toArray(rets);
				return rets;
			}
		} catch (Exception e) {// 不需要往外扔异常
			e.printStackTrace();
		}
		return null;
	}
	public static List<String> fetchSearchPaths(Class<?> clazz){
		try{
			List<String> paths = new ArrayList<String>();
			String[] classpath = System.getProperty(PROPERTY_CLASSPATH).split(File.pathSeparator);
			for(String cp:classpath){
				paths.add(cp);
			}
			//兼容jetty的osgi的情况
			String rjrclasspath = System.getProperty("rjrclasspath");
			if(!StringUtils.isBlank(rjrclasspath)){
				rjrclasspath = rjrclasspath.replaceAll("file://", "");
				String content = IOHelper.readStringFromFileByChar(rjrclasspath, "UTF-8");
				content = content.replaceAll("-y-", "");
				content = content.replaceAll("-n-", "");
				String[] array = content.split(":");
				for(String p:array){
					paths.add(p);
				}
			}
			//应对扫描war中代码的业务代码情况，不支持类似tomcat中common lib或share lib的情况
			String findpath = "";
			try{
				findpath = getClassRealPath(clazz);
			}catch(NullPointerException npe){
				//如果扫描的时候出错了。。。很有可能是施主/檀越，在扫描jre的内容
			}
			log.debug("<!--findpath:"+findpath+"-->");
			int pos = findpath.indexOf("WEB-INF");
			if(pos>0){
				//1-扫描WEB-INF/lib下的jar
				String jarpath = findpath.substring(0,pos+"WEB-INF".length())+"/lib";//记性不好，且懒得数数，只好牺牲效率了
				//遍历其下的文件，包含子目录的遍历
				List<String> jars = traversal(new File(jarpath));
				if(jars!=null && jars.size()>0){
					paths.addAll(jars);
				}
				//2-扫描WEB-INF/classes下的class
				String classespath = findpath.substring(0,pos+"WEB-INF".length())+"/classes";
				paths.add(classespath);
			}else{
				pos = findpath.indexOf("target");
				if(pos >0){//兼容maven的情况
					String classespath = findpath.substring(0,pos+"target".length())+"/classes";
					paths.add(classespath);
				}
			}
			return paths;
		} catch (Exception e) {// 不需要往外扔异常
			e.printStackTrace();
		}
		return null;
	}
	public static String getClassRealPath(Class<?> cls) {
		try{
			String fullName = cls.getName().replace(".","/")+".class";//将.换成/
			ClassLoader loader = cls.getClassLoader();
			URL url = loader.getResource(fullName);
			String realPath = url.getPath();
			if(log.isDebugEnabled()){
				log.debug("("+fullName+")的实际路径为"+realPath);
			}
			//处理file:/的问题
			realPath = realPath.replaceFirst("file:", "");
			int pos = realPath.indexOf("jar!");//如果来自jar则只取其jar的位置
			if(pos>-1){
				return realPath.substring(0, pos+3);
			}
			
			String pack = cls.getPackage().getName().replace(".", "/");
			if(pack.trim().equals("")){//有可能是default package
				return realPath.replace(cls.getName(), "");//去掉类名
			}else{
				pos = realPath.indexOf(pack);
				return realPath.substring(0,pos+pack.length());
			}
		}catch(Exception e){
			log.error(e);
		}
		return null;
	}
	public static boolean isFilterJar(String clazzName,ClassRefFilter filter) {
		if (filter == null) {
			return true;
		}
		if (clazzName.toLowerCase().endsWith(filter.getFilterJar().toLowerCase())) {
			return true;
		}
		return false;
	}
	/**
	 * 
	 * @param subClassName
	 * @return
	 */
	private static boolean isEntityClass(String subClassName) {
		//FIXME 叫isEntityClass会更合适一些
		try {
			Class<?> temp = Class.forName(subClassName);
			//如果是接口或者抽象类，就不是实体类
			//如果没有实现ivalueobject就不算实体类
			if(temp.isInterface() || Modifier.isAbstract(temp.getModifiers()) || ClassUtils.isAssignable(temp,IValueObject.class)){
				return false;
			}
			return true;
		} catch (Throwable e) {
		}
		return false;
	}
	/**
	 * 遍历目录
	 * @param file
	 * @return
	 */
	public static List<String> traversal(File file){
		try{
			if(file==null || !file.exists()){
				log.debug("file("+file.getAbsolutePath()+")-->"+file.exists()+"--");
				return null;
			}
			FilenameFilter filter_jar = new FilenameFilter(){
				@Override
				public boolean accept(File dir, String name) {
					return name.indexOf("jar")>0 && dir.isFile();
				}
			};
			FilenameFilter filter_dir = new FilenameFilter(){
				@Override
				public boolean accept(File dir, String name) {
					return dir.isDirectory();
				}
			};
			//------------------
			List<String> rets = new ArrayList<String>();
			if(file.isFile() && file.getName().indexOf(".jar")>0){
				rets.add(file.getPath());
				return rets;
			}
			File[] list = file.listFiles(filter_dir);
			if(list!=null){
				for(File single:list){
					List<String> temp = traversal(single);
					rets.addAll(temp);
				}
			}
			//寻找当前目录下的jar
			File[] jars = file.listFiles(filter_jar);
			if(jars!=null && jars.length>0){
				for(File jar:jars){
					rets.add(jar.getPath());
				}
			}
			return rets;
		}catch(Exception e){
			//忽略错误
			log.error(e);
		}
		return null;
	}
	//
	private static class ClassRefFilter {
		
		private String _filterJar = new String();//需要扫描的包的名称
		
		/**
		 * 扫描的jar包中所需扫描的package的列表。
		 * 如果需要扫描如下两个包哪么只需注册成"pb.pf.schedule","pb.pf.xx" 
		 */
		//FIXME 取消设置方法，直接赋值即可
		private List<String> _filterPackages = new ArrayList<String>();

		public void addFilterpPackage(String filterPackage){
			this._filterPackages.add(filterPackage);
		}
		
		public void setFilterJar(String filterJar){
			this._filterJar = filterJar;
		}
		
		public String getFilterJar(){
			return _filterJar;
		}
		
		public void setFilterPackages(List<String> filterPackages){
			this._filterPackages = filterPackages;
		}
		public List<String> getFilterPackages(){
			return _filterPackages;
		}
	}
}
