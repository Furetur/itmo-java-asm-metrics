import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes



val classnames = listOf(
    "com.example.input.Base",
    "com.example.input.Child",
)

fun main(args: Array<String>) {
    val ir = buildIr(classnames)
    println(ir)

    val inh = calculateInheritanceMetrics(ir)
    println("MHF = ${mhf(ir)} ")
    println("AHF = ${ahf(ir)}")
    println("MIF = ${mif(inh)}")
    println("AIF = ${aif(inh)}")
}