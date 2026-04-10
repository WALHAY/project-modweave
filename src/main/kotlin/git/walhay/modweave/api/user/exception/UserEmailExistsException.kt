package git.walhay.modweave.api.user.exception

class UserEmailExistsException(email: String) :
    Exception("User with email=\"$email\" already exists")
