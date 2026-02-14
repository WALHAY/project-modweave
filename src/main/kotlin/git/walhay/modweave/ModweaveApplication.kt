package git.walhay.modweave

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class ModweaveApplication

fun main(args: Array<String>) {
  runApplication<ModweaveApplication>(*args)
}
