fun mhf(ir: Ir): Double {
    val totalHidden = ir.classes.values.sumOf { cl -> cl.methods.count { it.access != Access.PUBLIC } }
    val totalVisible = ir.classes.values.sumOf { cl -> cl.methods.count { it.access == Access.PUBLIC } }
    return totalHidden.toDouble() / (totalHidden.toDouble() + totalVisible.toDouble())
}

fun ahf(ir: Ir): Double {
    val totalHidden = ir.classes.values.sumOf { cl -> cl.attributes.values.count { it.access != Access.PUBLIC } }
    val totalVisible = ir.classes.values.sumOf { cl -> cl.attributes.values.count { it.access == Access.PUBLIC } }
    return totalHidden.toDouble() / (totalHidden.toDouble() + totalVisible.toDouble())
}

fun parents(ir: Ir, cl: IrClass) = sequence<IrClass> {
    var cur = ir.classes[cl.baseClass]
    while (cur != null) {
        yield(cur)
        cur = ir.classes[cur.baseClass]
    }
}

fun getAllInheritedMethods(ir: Ir, cl: IrClass): Set<IrMethod> {
    val result = mutableSetOf<IrMethod>()
    for (parent in parents(ir, cl)) {
        result.addAll(
            parent.methods.filter { it.access != Access.PRIVATE }
        )
    }
    return result
}

fun getAllInheritedAttributes(ir: Ir, cl: IrClass): Set<IrAttribute> {
    val result = mutableSetOf<IrAttribute>()
    for (parent in parents(ir, cl)) {
        result.addAll(
            parent.attributes.values.filter { it.access != Access.PRIVATE }
        )
    }
    return result
}


data class InheritanceMetrics(
    val newCount: Int,
    val overriddenCount: Int,
    val inheritedNotOverriddenCount: Int
)

fun calculateMethodInheritanceMetrics(ir: Ir, cl: IrClass): InheritanceMetrics {
    val definedMethods = cl.methods.toSet()
    val inheritedMethods = getAllInheritedMethods(ir, cl)

    return InheritanceMetrics(
        newCount = (definedMethods - inheritedMethods).size,
        overriddenCount = (definedMethods.intersect(inheritedMethods)).size,
        inheritedNotOverriddenCount = (inheritedMethods - definedMethods).size
    )
}

fun calculateAttributeInheritanceMetrics(ir: Ir, cl: IrClass): InheritanceMetrics {
    val definedAttributes = cl.methods.toSet()
    val inheritedAttributes = getAllInheritedAttributes(ir, cl)

    return InheritanceMetrics(
        newCount = (definedAttributes - inheritedAttributes).size,
        overriddenCount = (definedAttributes.intersect(inheritedAttributes)).size,
        inheritedNotOverriddenCount = (inheritedAttributes - definedAttributes).size
    )
}

data class ClassInheritanceMetrics(val attr: InheritanceMetrics, val meth: InheritanceMetrics)

typealias IrInheritanceMetrics = Map<String, ClassInheritanceMetrics>

fun calculateInheritanceMetrics(ir: Ir): IrInheritanceMetrics =
    ir.classes.mapValues {
        ClassInheritanceMetrics(
            attr = calculateAttributeInheritanceMetrics(ir, it.value),
            meth = calculateMethodInheritanceMetrics(ir, it.value)
        )
    }

fun mif(inh: IrInheritanceMetrics): Double {
    val numerator = inh.values.sumOf { it.meth.inheritedNotOverriddenCount }
    val denominator =
        inh.values.sumOf { it.meth.inheritedNotOverriddenCount + it.meth.overriddenCount + it.meth.newCount }
    return numerator.toDouble() / denominator
}

fun aif(inh: IrInheritanceMetrics): Double {
    val numerator = inh.values.sumOf { it.attr.inheritedNotOverriddenCount }
    val denominator =
        inh.values.sumOf { it.attr.inheritedNotOverriddenCount + it.attr.overriddenCount + it.attr.newCount }
    return numerator.toDouble() / denominator
}

fun descendantsCount(ir: Ir, cl: IrClass): Int = ir.classes.values.count { parents(ir, it).contains(cl) }

fun pof(ir: Ir, inh: IrInheritanceMetrics): Double {
    val totalOverriden = inh.values.sumOf { it.meth.overriddenCount }
    val denomimator =
        inh.map { (clName, metrics) -> descendantsCount(ir, ir.classes[clName]!!) * metrics.meth.newCount }.sum()
    return totalOverriden.toDouble() / denomimator
}





