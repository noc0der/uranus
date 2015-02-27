package testcase.asm.method;

import java.io.InputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.springframework.util.ClassUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import testcase.vo.AccountVO;

public class MethodModifyTestCase {
	private static SimpleClassLoader classLoader = new SimpleClassLoader();
	@Test
	public void testPrint(){
		try {
			ClassPrinter cp = new ClassPrinter();
			ClassReader cr = new ClassReader("java.lang.Runnable");
			cr.accept(cp, 0);
		} catch (Exception e) {
			Assert.fail("soso", e);
		}

	}
	@Test
	public void test(){
		try {
			Class clazz = AccountVO.class;
			InputStream is = clazz.getResourceAsStream(ClassUtils.getClassFileName(clazz));
			ClassReader classReader = new ClassReader(is);
			ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			ClassVisitor classVisitor = new ModifyMethodClassVisitor(classWriter);
			classReader.accept(classVisitor, 0);
			Object tmp = clazz.newInstance();
			System.out.println("-->"+(tmp instanceof AccountVO));
			((AccountVO)tmp).setName("test");
		} catch (Exception e) {
			Assert.fail("soso", e);
		}

	}
	@Test
	public void modiySleepMethod() throws Exception {
		try {
			ClassReader classReader = new ClassReader("testcase.vo.AccountVO");
			ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			ClassVisitor classAdapter = new ModifyMethodClassVisitor(classWriter);
			classReader.accept(classAdapter, ClassReader.SKIP_DEBUG);

			byte[] classFile = classWriter.toByteArray();

//			Class clazz = classLoader.defineClassFromClassFile("testcase.vo.AccountVO", classFile);
//			Class clazz = (Class) classLoader.loadClass(enhancedClassName);
//			Class clazz = (Class) classLoader.loadClass("testcase.vo.AccountVO");
			
			String enhancedClassName = AccountVO.class.getName() + "$ENHANCED";
			Class clazz = (Class)classLoader.defineClass(enhancedClassName, classFile);
			//
			//
			//
			Object tmp = clazz.newInstance();
			System.out.println(tmp.getClass().getName()+"--"+AccountVO.class.getName());
			Assert.assertTrue(tmp.getClass().getName().equals(AccountVO.class.getName()));
			System.out.println((tmp instanceof AccountVO)+"-->");
			AccountVO obj = (AccountVO)clazz.newInstance();
			obj.setName("soso");
		} catch (Exception e) {
			Assert.fail("soso", e);
		}
	}

	public static class SimpleClassLoader extends ClassLoader {
		public Class<?> defineClass(String className, byte[] byteCodes) {
			return super.defineClass(className, byteCodes, 0, byteCodes.length);
		}
	}
}
