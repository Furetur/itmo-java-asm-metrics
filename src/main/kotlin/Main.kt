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

// Hiding Factor
//val classnames = listOf(
//    "com.example.hidingfactor.Base",
//    "com.example.hidingfactor.Child",
//)

// Inheritance Factor
//val classnames = listOf(
//    "com.example.inheritancefactor.Base",
//    "com.example.inheritancefactor.Child",
//)

// Polymorphism Factor
//val classnames = listOf(
//    "com.example.polymorphismfactor.Base",
//    "com.example.polymorphismfactor.Child",
//)

// Coupling Factor
//val classnames = listOf(
//    "com.example.hidingfactor.Base",
//    "com.example.hidingfactor.Child",
//)

fun main(args: Array<String>) {
    val ir = buildIr(classnames)
    println(ir)

    val inh = calculateInheritanceMetrics(ir)
    println("MHF = ${mhf(ir)} ")
    println("AHF = ${ahf(ir)}")
    println("MIF = ${mif(inh)}")
    println("AIF = ${aif(inh)}")
    println("POF = ${pof(ir, inh)}")
    println("COF = ${cof(ir)}")
}