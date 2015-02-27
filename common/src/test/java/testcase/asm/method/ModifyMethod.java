package testcase.asm.method;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import testcase.vo.AccountVO;
/**
 * 具体变更的内容
 * @author to0ld
 *
 */
public class ModifyMethod extends MethodVisitor {

	 public ModifyMethod(MethodVisitor mv, int access, String name, String desc) {  
	        super(Opcodes.ASM5,mv);  
	    }

	@Override
	public void visitCode() {
		mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(AccountVO.class), "timer", "J");
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J");
		mv.visitInsn(Opcodes.LSUB);
		mv.visitFieldInsn(Opcodes.PUTSTATIC, Type.getInternalName(AccountVO.class), "timer", "J");
	}

	@Override
	public void visitInsn(int opcode) {
		if (opcode == Opcodes.RETURN) {
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
			mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(AccountVO.class), "timer", "J");
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J");
			mv.visitInsn(Opcodes.LADD);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(J)V");
		}
		mv.visitInsn(opcode);
	}
}
