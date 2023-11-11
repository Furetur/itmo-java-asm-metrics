import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes



val classnames = listOf(
    "java.util.concurrent.ArrayBlockingQueue",
    "java.util.concurrent.LinkedBlockingQueue",
    "java.util.AbstractQueue",
    "java.util.concurrent.locks.ReentrantLock",
    "java.util.concurrent.CyclicBarrier",
    "java.util.concurrent.ForkJoinPool",
    "java.util.concurrent.AbstractExecutorService",
    "java.util.concurrent.ThreadPoolExecutor",
    "java.time.Instant"
)

fun main(args: Array<String>) {
    val ir = buildIr(classnames)
    println(ir)

    val inh = calculateInheritanceMetrics(ir)
    println("MHF = ${mhf(ir)} ")
    println("AHF = ${ahf(ir)}")
    println("MIF = ${mif(inh)}")
    println("AIF = ${aif(inh)}")
    println("COF = ${cof(ir)}")
}