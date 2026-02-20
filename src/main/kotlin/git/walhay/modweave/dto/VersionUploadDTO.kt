package git.walhay.modweave.dto

import org.springframework.web.multipart.MultipartFile

data class VersionUploadDTO(val name: String, val changes: String, val files: List<MultipartFile>)
