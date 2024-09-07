package org.tlsys.helm

/*
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import org.yaml.snakeyaml.Yaml

abstract class ChartUpdateTask : DefaultTask() {
    class Dependency {
        var name: String? = null
        var version: String? = null
        var repository: String? = null
    }

    class Chart {
        var apiVersion: String? = null
        var name: String? = null
        var description: String? = null
        var home: String? = null
        var icon: String? = null
        var keywords: List<String>? = null
        var type: String? = null
        var dependencies: List<Dependency> = emptyList()
        var version: String? = null
        var appVersion: String? = null
    }

    @get:InputDirectory
    abstract val directory: RegularFileProperty

    @get:Input
    open val changes = ArrayList<(Chart) -> Unit>()

    fun change(func: (Chart) -> Unit) {
        changes += func
    }

    @TaskAction
    fun execute() {
        val file = directory.get().asFile.resolve("Chart.yaml")
        val yaml = Yaml()
//        yaml.load<Chart>("")
        val node =
            file.reader().use {
                yaml.loadAs(it, Chart::class.java)
            } // as MappingNode
        changes.forEach {
            it(node)
        }
        file.writer().use {
            yaml.dump(node, it)
        }
//        node.value.forEach {
//            val key = (it.keyNode as ScalarNode).value
//            println("1=>$key")
//            println("2=>${it.valueNode::class.java.name}")
//        }
//        println("Node: ${node::class.java.name}")
    }
}
*/
