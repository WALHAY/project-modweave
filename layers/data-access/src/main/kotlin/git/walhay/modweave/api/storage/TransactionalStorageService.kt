package git.walhay.modweave.api.storage

import java.io.InputStream
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class TransactionalStorageService(
    private val storage: ObjectStorageClient,
    private val operations: StorageOperationRepository,
) : ISimpleStorageService {
  @Transactional(propagation = Propagation.MANDATORY, rollbackFor = [Exception::class])
  override fun uploadImage(filename: String, file: MultipartFile): String = upload(StorageBucket.IMAGES, filename, file)

  @Transactional(propagation = Propagation.MANDATORY, rollbackFor = [Exception::class])
  override fun uploadVersionFile(filename: String, file: MultipartFile): String = upload(StorageBucket.MODS, filename, file)

  private fun upload(bucket: StorageBucket, filename: String, file: MultipartFile): String {
    val intent = operations.registerUpload(bucket, filename)
    operations.lockUpload(intent)
    require(!operations.isReferenced(bucket, filename)) { "Storage keys are immutable" }
    storage.upload(bucket, filename, file)
    // This deletion commits atomically with the application's references to the object.
    operations.completeUpload(intent)
    return filename
  }

  @Transactional(propagation = Propagation.MANDATORY, rollbackFor = [Exception::class])
  override fun removeVersionFile(filename: String) { operations.enqueueDeletion(StorageBucket.MODS, filename) }

  @Transactional(propagation = Propagation.MANDATORY, rollbackFor = [Exception::class])
  override fun removeImage(filename: String) { operations.enqueueDeletion(StorageBucket.IMAGES, filename) }

  override fun downloadVersionFile(filename: String): InputStream = storage.downloadVersionFile(filename)
}
