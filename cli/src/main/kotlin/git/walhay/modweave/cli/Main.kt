package git.walhay.modweave.cli

import git.walhay.modweave.ModweaveApplication
import org.springframework.boot.runApplication

fun main(args: Array<String>) {
  runApplication<ModweaveApplication>(*args)
}
