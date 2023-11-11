import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import java.lang.Class
import kotlin.math.min

fun StringBuilder.indent(func: StringBuilder.() -> Unit) {
    val sb = StringBuilder()
    sb.func()
    val s = sb.toString().lines().joinToString("\n") { l -> "\t" + l }
    appendLine(s)
}

data class Ir(val classes: Map<String, IrClass>) {
    override fun toString(): String = buildString {
        appendLine("Ir")
        indent {
            for (cl in classes.values) {
                appendLine(cl.toString())
            }
        }
    }
}

data class IrClass(
    val name: String,
    val baseClass: String,
    val attributes: Map<String, IrAttribute>,
    val methods: List<IrMethod>
) {
    override fun toString(): String = buildString {
        appendLine("class $name : $baseClass")
        indent {
            appendLine("attributes")
            indent {
                for (attr in attributes.values) {
                    appendLine(attr.toString())
                }
            }

            appendLine("methods")
            indent {
                for (m in methods) {
                    appendLine(m.toString())
                }
            }
        }
    }
}


enum class Access(val level: Int) {
    PUBLIC(0),
    PROTECTED(1),
    PRIVATE(2),
}

fun accessFromLevel(level: Int) = when (level) {
    0 -> Access.PUBLIC
    1 -> Access.PROTECTED
    2 -> Access.PRIVATE
    else -> error("damn")
}

fun minAccess(a: Access, b: Access) = accessFromLevel(min(a.level, b.level))

data class IrAttribute(val name: String, val access: Access, val typeDescriptor: String) {
    override fun toString(): String {
        return "${access.toString().lowercase()} $name: $typeDescriptor"
    }
}

data class IrMethod(val name: String, val access: Access, val descriptor: String) {
    override fun toString(): String {
        return "${access.toString().lowercase()} $name: $descriptor"
    }
}


fun getAccess(access: Int) = if (access == Opcodes.ACC_PUBLIC) {
    Access.PUBLIC
} else if (access == Opcodes.ACC_PRIVATE) {
    Access.PRIVATE
} else if (access == Opcodes.ACC_PROTECTED) {
    Access.PROTECTED
} else {
    null
}

fun asAttribute(name: String, access: Int, descriptor: String): IrAttribute? {
    return when {
        name.startsWith("get") -> {
            val attrName = name.drop(3).lowercase()
            if (descriptor.startsWith("()")) {
                getAccess(access)?.let { access ->
                    IrAttribute(
                        name = attrName,
                        access = access,
                        typeDescriptor = descriptor.drop(2)
                    )
                }
            } else {
                null
            }
        }

        name.startsWith("set") -> {
            if (descriptor.endsWith("V") and (descriptor.count { it == ';' } == 1)) {
                getAccess(access)?.let { access ->
                    IrAttribute(
                        name = name.drop(3).lowercase(),
                        access = access,
                        typeDescriptor = descriptor.drop(1).dropLast(2)
                    )
                }
            } else {
                null
            }
        }

        else -> {
            null
        }
    }
}

fun asMethod(name: String, access: Int, descriptor: String): IrMethod? = getAccess(access)?.let { access ->
    if (name == "<init>") null
    else IrMethod(name = name, access = access, descriptor = descriptor)
}


class MethodsVisitor : ClassVisitor(Opcodes.ASM9) {
    val methods: MutableList<IrMethod> = mutableListOf()

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor? {
        if (asAttribute(name, access, descriptor) != null) return null
        val m = asMethod(name, access, descriptor) ?: return null
        methods.add(m)
        return null
    }
}

class BaseClassVisitor : ClassVisitor(Opcodes.ASM9) {
    var className: String? = null
    var baseClass: String? = null

    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        className = name
        baseClass = superName
    }
}

class AttributesVisitor : ClassVisitor(Opcodes.ASM9) {
    val attributes: MutableMap<String, IrAttribute> = mutableMapOf()

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor? {
        val attr = asAttribute(name, access, descriptor) ?: return null
        val oldAttr = attributes[attr.name]
        if (oldAttr != null) {
            assert(oldAttr.typeDescriptor == attr.typeDescriptor)
            attributes[attr.name] = IrAttribute(
                name = attr.name,
                access = minAccess(attr.access, oldAttr.access),
                typeDescriptor = attr.typeDescriptor
            )
        } else {
            attributes[attr.name] = attr
        }
        return null
    }
}

fun buildIr(classnames: List<String>): Ir {
    val classes = classnames.map { cl ->
        val reader = ClassReader(cl)

        val m = MethodsVisitor()
        reader.accept(m, 0)
        val a = AttributesVisitor()
        reader.accept(a, 0)
        val b = BaseClassVisitor()
        reader.accept(b, 0)

        IrClass(
            name=b.className!!,
            baseClass = b.baseClass!!,
            methods = m.methods,
            attributes = a.attributes
        )
    }.associateBy { it.name }
    return Ir(classes)
}
