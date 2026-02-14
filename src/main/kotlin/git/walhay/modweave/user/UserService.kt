package git.walhay.modweave.user

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService @Autowired constructor(
    private val userRepository: UserRepository
) {

    @Transactional
    fun registerNewUser(user: User) {
        if(userRepository.existsById(user.login)) {
            throw Exception()
        }

        userRepository.save(user)
    }
}