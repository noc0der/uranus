package net.popbean.pf.helper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.FileChannel;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;

import net.popbean.pf.exception.ErrorBuilder;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.ResourceUtils;

/**
 * 有关i/o操作的公用方法
 * 
 * @author worm
 * 
 */
public class IOHelper {
	// 缓冲区大小
	// public static int BUFFERSIZE = 2 * 8192;
	/**
	 * The default buffer size to use.
	 */
	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
	/**
	 * 
	 * @param path file:// or classpath: or url://
	 * @return
	 * @throws Exception
	 */
	public static Object readObject(String path)throws Exception{
		ObjectInputStream ois = null;
		InputStream is = null;
		try {
			ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
			Resource resource = resolver.getResource(path);
			is = resource.getInputStream();
			ois = new ObjectInputStream(is);
			Object obj = ois.readObject();
			return obj;
		} finally {
			if (is != null) {
				is.close();
			}
			if (ois != null) {
				ois.close();
			}
		}
	}
	/**
	 * 从文件中读取对象
	 * 
	 * @param filepath
	 * @return
	 * @throws Exception
	 */
	public static Object readObjectFromFile(String filepath) throws Exception {
		ObjectInputStream ois = null;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(filepath);
			ois = new ObjectInputStream(fis);
			Object obj = ois.readObject();
			return obj;
		} finally {
			if (fis != null) {
				fis.close();
			}
			if ( ois != null) {
				ois.close();
			}
		}
	}
	public static byte[] readByte(String filepath)throws Exception{
		FileInputStream in1 = null;
		byte[] bcert = null;
		try {
			in1 = new FileInputStream(filepath);
			bcert = new byte[in1.available()];
			in1.read(bcert);
		} catch (Exception e) {
			throw e;
		}finally{
			if(in1!=null){
				in1.close();
			}
		}
		return bcert;
	}
	public static void copy(InputStream in, OutputStream out) throws IOException {
		copyStream(in, out, -1);
	}

	/**
	 * 转化文件编码(比如:gbk--&gt;utf-8)
	 * 
	 * @param originalfl
	 * @param original
	 * @param vendor 
	 * @throws Exception
	 */
	public static void convertFileCode(String originalfl, String original, String vendorfl,String vendor) throws Exception {
		Reader reader = null;
		OutputStream os = null;
		try {
			InputStream is = IOHelper.readInputStreamFromFile(originalfl);
			reader = new InputStreamReader(is, original);
			//如果目的地文件不存在则主动创建
			int pos = vendorfl.lastIndexOf(File.separator);
			File temp = new File(vendorfl.substring(0, pos));
			if(!temp.exists()){
				temp.mkdirs();
			}
			new File(vendorfl).getAbsolutePath();
			//
			os = new FileOutputStream(vendorfl);
			copy(reader, os, "utf-8");
		} catch (Exception e) {
			throw e;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (Exception e) {
					throw e;
				}
			}
			if (os != null) {
				try {
					os.close();
				} catch (Exception e) {
					throw e;
				}
			}
		}
	}

	public static void copy(Reader input, OutputStream output, String encoding) throws IOException {
		if (encoding == null) {
			copy(input, output);
		} else {
			OutputStreamWriter out = new OutputStreamWriter(output, encoding);
			copy(input, out);
			// XXX Unless anyone is planning on rewriting OutputStreamWriter,
			// we have to flush here.
			out.flush();
		}
	}

	public static void copy(Reader input, OutputStream output) throws IOException {
		OutputStreamWriter out = new OutputStreamWriter(output);
		copy(input, out);
		out.flush();
	}

	public static int copy(Reader input, Writer output) throws IOException {
		long count = copyLarge(input, output);
		if (count > Integer.MAX_VALUE) {
			return -1;
		}
		return (int) count;
	}

	public static long copyLarge(Reader input, Writer output) throws IOException {
		char[] buffer = new char[DEFAULT_BUFFER_SIZE];
		long count = 0;
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}

	/* ------------------------------------------------------------------- */
	/**
	 * @deprecated 不是很理想的实现
	 * Copy Stream in to Stream for byteCount bytes or until EOF or exception.
	 */
	public static void copyStream(InputStream in, OutputStream out, long byteCount) throws IOException {
		byte buffer[] = new byte[DEFAULT_BUFFER_SIZE];
		int len = DEFAULT_BUFFER_SIZE;

		if (byteCount >= 0) {
			while (byteCount > 0) {
				if (byteCount < DEFAULT_BUFFER_SIZE)
					len = in.read(buffer, 0, (int) byteCount);
				else
					len = in.read(buffer, 0, DEFAULT_BUFFER_SIZE);

				if (len == -1)
					break;

				byteCount -= len;
				out.write(buffer, 0, len);
			}
		} else {
			while (true) {
				len = in.read(buffer, 0, DEFAULT_BUFFER_SIZE);
				if (len < 0)
					break;
				out.write(buffer, 0, len);
			}
		}
	}
	/**
	 * 
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	public static void copyStream(InputStream in,OutputStream out)throws IOException{//拷贝完之后给关闭了
		InputStream bis = null;
		OutputStream bos = null;
		try {
			bis = new BufferedInputStream(in);
			bos = new BufferedOutputStream(out);
			int iEOF = -1;
			while ((iEOF = bis.read()) != -1) {
				bos.write(iEOF);
			}
		} finally {
			if (bis != null) {
				bis.close();
			}
			if (bos != null) {
				bos.close();
			}
		}
	}
	/**
	 * 
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	public static void copyReader2Writer(Reader in, Writer out) throws IOException {
		copyReader2Writer(in, out, -1);
	}

	/**
	 * Copy Reader to Writer for byteCount bytes or until EOF or exception.
	 */
	public static void copyReader2Writer(Reader in, Writer out, long byteCount) throws IOException {
		char buffer[] = new char[DEFAULT_BUFFER_SIZE];
		int len = DEFAULT_BUFFER_SIZE;

		if (byteCount >= 0) {
			while (byteCount > 0) {
				if (byteCount < DEFAULT_BUFFER_SIZE)
					len = in.read(buffer, 0, (int) byteCount);
				else
					len = in.read(buffer, 0, DEFAULT_BUFFER_SIZE);

				if (len == -1)
					break;

				byteCount -= len;
				out.write(buffer, 0, len);
			}
		} else {
			while (true) {
				len = in.read(buffer, 0, DEFAULT_BUFFER_SIZE);
				if (len == -1)
					break;
				out.write(buffer, 0, len);
			}
		}
	}

	/**
	 * 根据给定的文件读取配置信息支持类路径,绝对路径<br>
	 * 
	 * @param configpath
	 * @return
	 */
	public static Properties readPropFromFile(String configpath) throws Exception{
		Properties prop = null;
		InputStream is = readInputStreamFromFile(configpath);
		try {
			if (is == null) {
				return prop;
			}
			prop = new Properties();
			prop.load(is);
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if ( is != null) {
					is.close();
				}
			} catch (Exception e) {
				throw e;
			}
		}
		return prop;
	}

	/**
	 * 
	 * @param configpath
	 * @return
	 */
	public static InputStream readInputStreamFromFile(String configpath) {
		try {
			return new FileInputStream(configpath);
		} catch (Exception e) {
			return IOHelper.class.getClassLoader().getResourceAsStream(configpath);
		}
	}

	/**
	 * 编码默认为utf-8
	 * @param filepath(文件的路径)
	 * @return
	 * @throws Exception
	 */
	public static String readStringFromFileByByte(String filepath) throws Exception {
		return readStringFromFileByByte(filepath, "utf-8");
	}
	/**
	 * @deprecated 使用java7的Files重写
	 * @param filepath
	 * @param charset
	 * @return
	 * @throws Exception
	 */
	public static String readStringFromFileByByte(String filepath, String charset) throws Exception {
		FileInputStream fis = null;
		StringBuilder ret = new StringBuilder();
		try {
			fis = new FileInputStream(filepath);
			byte[] array = new byte[1024];
			int datapos = -1;
			while ((datapos = fis.read(array)) != -1) {
				if(ret.length()>0){
					ret.append("\n");	
				}
				ret.append(new String(array, 0, datapos, charset));
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (fis!= null) {
				fis.close();
			}
		}
		return ret.toString();
	}
	/**
	 * 读取内容
	 * @param path
	 * @param charset
	 * @return
	 * @throws Exception
	 */
	public static String readByChar(String path,String charset)throws Exception{
		InputStreamReader isr = null;
		InputStream is = null;
		StringBuilder ret = new StringBuilder();
		BufferedReader br = null;
		//
		try {
			ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
			Resource resource = resolver.getResource(path);
			is = resource.getInputStream();
			
			if (charset == null) {
				isr = new InputStreamReader(is);
			} else {
				isr = new InputStreamReader(is, charset);
			}
			br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null) {
				if(ret.length()>0){//如此这般处理，只是为了少加一个\n，如实的体现文件的原貌
					ret.append("\n");	
				}
				ret.append(line);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (br != null) {
				br.close();
			}
			if (isr != null) {
				isr.close();
			}
			if (is != null) {
				is.close();
			}
		}
		return ret.toString();
	}
	/**
	 * 将来有时间参考spring的写法
	 * 1-classpath:
	 * 2-file:
	 * @deprecated 使用java7的Files重写
	 * @param filepath
	 * @param charset
	 * @return
	 * @throws Exception
	 */
	public static String readStringFromFileByChar(String filepath,String charset) throws Exception {
		InputStreamReader isr = null;
		InputStream is = null;
		StringBuilder ret = new StringBuilder();
		BufferedReader br = null;
		//
		try {
			is = new FileInputStream(filepath);
		} catch (Exception e) {
		}
		if (is == null) {
//			String newpath = ResourceHelper.classpath2filepath(filepath);
			String newpath = ResourceUtils.getFile(filepath).getAbsolutePath();
			try {
				is = new FileInputStream(newpath);
			} catch (Exception e) {
			}
		}
		if (is == null) {
			is = IOHelper.class.getClassLoader().getResourceAsStream(filepath);
			if (is == null) {
				ErrorBuilder.createSys().msg("无法加载，请检查路径:" + filepath).execute();
			}
		}
		//
		try {
			if (charset == null) {
				isr = new InputStreamReader(is);
			} else {
				isr = new InputStreamReader(is, charset);
			}
			br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null) {
				if(ret.length()>0){//如此这般处理，只是为了少加一个\n，如实的体现文件的原貌
					ret.append("\n");	
				}
				ret.append(line);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (br!= null) {
				br.close();
			}
			if (isr!=null) {
				isr.close();
			}
			if (is!=null) {
				is.close();
			}
		}
		return ret.toString();
	}
	/**
	 * 根据绝对路径判断文件||目录是否存在<br>
	 * 
	 * @param configpath
	 *            支持绝对路径以及资源相对路径的判断
	 * @return
	 */
	public static boolean isExist(String configpath) {
		try {
			return new File(configpath).exists();
		} catch (Exception e) {
			String path = IOHelper.class.getClassLoader().getResource(configpath).toString();
			return new File(path).exists();
		}
	}

	/**
	 * 获得最后修改时间
	 * 
	 * @param configpath
	 * @return
	 */
	public static long getLastModifyVersion(String configpath) {
		try {
			return new File(configpath).lastModified();
		} catch (Exception e) {
			String path = IOHelper.class.getClassLoader().getResource(configpath).toString();
			return new File(path).lastModified();
		}
	}

	/**
	 * 根据传入的对象以及路径将文件写入<br>
	 * 进行同步保护
	 * 
	 * @param object
	 * @throws Exception
	 */
	public static synchronized void writerObjectToFile(String filepath, Object object) throws Exception {
		ObjectOutputStream oos = null;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(filepath);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(object);
		} finally {
			if (fos != null) {
				fos.close();
			}
			if (oos != null) {
				oos.close();
			}
		}
	}

	/**
	 * 将属性表序列化之后保存到硬盘上
	 * 
	 * @param filepath
	 *            实际路径
	 * @param prop
	 *            属性
	 * @throws Exception
	 */
	public static synchronized void writerPropToFile(String filepath, Properties prop) throws Exception {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(new File(filepath));
			prop.store(fos, null);
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
	}

	/**
	 * 将字符串写入到文件中(需要注意的是，不能用于非ascii码文件，以及大文件的写入)
	 * 
	 * @param filepath
	 * @param content
	 * @throws Exception
	 */
	public static synchronized void writeStringToFile(String filepath, String content) throws Exception {
		FileWriter fw = null;
		try {
			fw = new FileWriter(filepath);
			fw.write(content);
			fw.flush();
		} catch (Exception e) {
			throw e;
		} finally {
			if (fw != null) {
				fw.close();
			}
		}
	}
	public static synchronized void write(String path,String file,String content)throws Exception{
		FileWriter fw = null;
		try {
			ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
			Resource resource = resolver.getResource(path);
			//
			File f = new File(resource.getURI());
			fw = new FileWriter(f.getAbsolutePath()+"/"+file);
			fw.write(content);
			fw.flush();
		} catch (Exception e) {
			throw e;
		} finally {
			if (fw != null) {
				fw.close();
			}
		}
	}
	/**
	 * 根据指定的编码写入文件
	 * @param filepath
	 * @param content
	 * @param charset
	 * @throws Exception
	 */
	public static synchronized void writeStringToFile(String filepath,String content,String charset) throws Exception{
		OutputStreamWriter osw = null;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(filepath);
			osw = new OutputStreamWriter(fos,charset);
			osw.write(content);
			osw.flush();
		} catch (Exception e) {
			throw e;
		} finally {
			if (osw != null) {
				osw.close();
			}
			if (fos != null) {
				fos.close();
			}
		}
	}
	
	public static void byteArray2OutputStream() throws Exception {

	}

	public static void writeByteArrayToFile(String filePath, byte[] bytes) throws Exception {
		FileWriter fw = null;
		try {
			FileOutputStream fos = new FileOutputStream(filePath);
			fos.write(bytes);
		} catch (Exception e) {
			throw e;
		} finally {
			if (fw != null) {
				fw.close();
			}
		}
	}
	/**
	 * 需要一些参数，比如是否强制生成目标路径；是否强制覆盖已有文件
	 * @param from
	 * @param to
	 * @throws Exception
	 */
	public static void cp(String from,String to)throws Exception{
		File fromdir = new File(from);
		if(fromdir.isDirectory()){//目录
			String temp = null;// 相对位置
			for (File single : fromdir.listFiles()) {
				temp = single.getAbsolutePath().replaceAll(from, "");
				if (single.isDirectory()) {
					cp(single.getAbsolutePath(), to + "/" + temp);
				} else {
					String substring = temp.substring(temp.lastIndexOf("\\") + 1, temp.length());
					cpFile(single.getAbsolutePath(), to + substring);
				}
			}
		}else{//文件
			cpFile(from, to);//都没一个是否强制覆盖啥的？
		}
		if (!fromdir.isDirectory()) {
			throw new Exception("传入的源位置并非目录");
		}
		//

	}
	/**
	 * @deprecated 目录拷贝
	 * 建议封存，直接用cp替代(不再识别file还是dir)
	 * @param from
	 * @param to
	 * @throws Exception
	 */
	private static void copyDir(String from, String to) throws Exception {
		File fromdir = new File(from);
		if (!fromdir.isDirectory()) {
			throw new Exception("传入的源位置并非目录");
		}
		//
		String temp = null;// 相对位置
		for (File single : fromdir.listFiles()) {
			temp = single.getAbsolutePath().replaceAll(from, "");
			if (single.isDirectory()) {
				copyDir(single.getAbsolutePath(), to + "/" + temp);
			} else {
				String substring = temp.substring(temp.lastIndexOf("\\") + 1, temp.length());
				copyFile(single.getAbsolutePath(), to + substring);
			}
		}
	}
	private static void cpFile(String from,String to)throws Exception{//拷贝文件，用nio的方式(非阻塞式)
		FileChannel inChannel = null;
		FileChannel outChannel = null;
		try {
			inChannel = new FileInputStream(from).getChannel();
			outChannel = new FileOutputStream(to).getChannel();
			outChannel.transferFrom(inChannel, 0, inChannel.size());
		} catch (Exception e) {
		}finally{
			if(inChannel!=null && inChannel.isOpen()){
				inChannel.close();
			}
			if(outChannel!=null && outChannel.isOpen()){
				outChannel.close();
			}
		}
//		FileChannel inChannel = fin.getChannel();
//		FileChannel outChannel = fout.getChannel();
		/*
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		while (true) {
			int ret = inChannel.read(buffer);
			if (ret == -1) {
				break;
			}
			buffer.flip(); // 该方法为父类Buffer的方法
			outChannel.write(buffer);
			buffer.clear(); // 该方法为父类Buffer的方法
		}*/
		
	}
	/**
	 * 拷贝文件
	 * 
	 * @param from
	 * @param to
	 * @throws Exception
	 */
	private static void copyFile(String from, String to) throws Exception {
		/*
		FileInputStream fis = null;
		FileOutputStream fos = null;
		try {
			fis = new FileInputStream(from);
			fos = new FileOutputStream(to);
			copyStream(fis, fos, -1);
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if (!AssertHelper.isNull(fos)) {
					fos.close();
				}
				if (!AssertHelper.isNull(fis)) {
					fis.close();
				}
			} catch(IllegalArgumentException iae){
				//忽略
			}catch (IOException ioe) {
				throw ioe;
			}
		}*/
		InputStream bis = null;
		OutputStream bos = null;
		try {
			InputStream in = new FileInputStream(from);
			bis = new BufferedInputStream(in);
			OutputStream out = new FileOutputStream(to);
			bos = new BufferedOutputStream(out);
			int iEOF = -1;
			while ((iEOF = bis.read()) != -1) {
				bos.write(iEOF);
			}
		} finally {
			if (bis != null) {
				bis.close();
			}
			if (bos != null) {
				bos.close();
			}
		}
	}



	/**
	 * 支持子目录删除
	 * 
	 * @param path
	 * @throws Exception
	 */
	public static void delDir(String path) throws Exception {
		File file = new File(path);
		if (file.isDirectory()) {
			for (File sub : file.listFiles()) {
				delDir(sub.getAbsolutePath());
			}
		}
		file.delete();
	}

	/**
	 * Unzips a theme from a ZIP file into a directory.
	 * 
	 * @param zip
	 *            the ZIP file
	 * @param dir
	 *            the directory to extract the plugin to.
	 * @return the root directory.
	 */
	public static File unzipPack(File zip, File dir) throws Exception{
		File rootDirectory = null;
		try {
			ZipFile zipFile = new JarFile(zip);

			dir.mkdir();
			for (Enumeration<?> e = zipFile.entries(); e.hasMoreElements();) {
				JarEntry entry = (JarEntry) e.nextElement();
				File entryFile = new File(dir, entry.getName());
				// Ignore any manifest.mf entries.
				if (entry.getName().toLowerCase().endsWith("manifest.mf")) {
					continue;
				}

				if (entry.isDirectory() && rootDirectory == null) {
					rootDirectory = entryFile;
				}

				if (!entry.isDirectory()) {
					entryFile.getParentFile().mkdirs();
					FileOutputStream out = new FileOutputStream(entryFile);
					InputStream zin = zipFile.getInputStream(entry);
					byte[] b = new byte[512];
					int len = 0;
					while ((len = zin.read(b)) != -1) {
						out.write(b, 0, len);
					}
					out.flush();
					out.close();
					zin.close();
				}
			}
			zipFile.close();
			zipFile = null;
		} catch (Exception e) {
			throw e;
		}

		return rootDirectory;
	}

	/**
	 * 
	 * @param is
	 * @throws Exception
	 */
	public static void close(InputStream is) throws Exception {
		if (is != null) {
			is.close();
		}
	}

	/**
	 * 
	 * @param os
	 * @throws Exception
	 */
	public static void close(OutputStream os) throws Exception {
		if (os != null) {
			os.close();
		}
	}
}
