package git.walhay.modweave.api.v1

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController("/api/v1/files")
class FilesController {

    @GetMapping("/{bucket}/{filename}")
    fun downloadFile(@PathVariable bucket: String, @PathVariable filename: String) {

    }
}