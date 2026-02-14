package git.walhay.modweave

import git.walhay.modweave.user.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ModweaveApplication

fun main(args: Array<String>) {
	runApplication<ModweaveApplication>(*args)
}
