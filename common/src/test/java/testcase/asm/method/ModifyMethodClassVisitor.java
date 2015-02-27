package testcase.asm.method;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ModifyMethodClassVisitor extends ClassVisitor {
	public static final String ENHANCED = "$ENHANCED";

	public ModifyMethodClassVisitor(ClassVisitor cv) {
		super(Opcodes.ASM5, cv);
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		// super.visit(version, access, name, signature, superName, interfaces);
		cv.visit(version, Opcodes.ACC_PUBLIC, name + ENHANCED.replace('.', '/'), signature, name, interfaces);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		if (name.equals("setName")) {
			MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
			return new ModifyMethod(mv, access, name, desc);
		}
		return super.visitMethod(access, name, desc, signature, exceptions);
	}

	@Override
	public void visitEnd() {
		cv.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC, "timer", "J", null, null);
	}
}
