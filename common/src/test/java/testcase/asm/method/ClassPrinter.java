package testcase.asm.method;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
/**
 * 打印类的相关信息
 * @author to0ld
 *
 */
public class ClassPrinter extends ClassVisitor {
	public ClassPrinter(){
		super(Opcodes.ASM5);
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		System.out.println(name + " extends "+superName + " {");
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		System.out.println(" "+desc + " "+name);
		return null;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		System.out.println(" "+name+" "+desc );
		return null;
	}

	@Override
	public void visitEnd() {
		System.out.println("}");
	}
}
