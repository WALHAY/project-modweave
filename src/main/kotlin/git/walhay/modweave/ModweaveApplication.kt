package git.walhay.modweave

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@ConfigurationPropertiesScan @SpringBootApplication class ModweaveApplication

fun main(args: Array<String>) {
  runApplication<ModweaveApplication>(*args)
}
