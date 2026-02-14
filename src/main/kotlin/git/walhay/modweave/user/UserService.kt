package git.walhay.modweave.user

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class UserService {

    @Autowired
    private val userRepository: UserRepository? = null

    fun registerNewUser(user: User) {
        userRepository?.save(user)
    }
}