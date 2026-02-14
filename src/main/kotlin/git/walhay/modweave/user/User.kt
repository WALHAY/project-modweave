package git.walhay.modweave.user

import com.fasterxml.jackson.annotation.JsonManagedReference
import git.walhay.modweave.mods.Mod
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.sql.Date

@Entity(name = "users")
@Table(schema = "modweave", name = "users")
open class User {
    @Id
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "login", nullable = false)
    open lateinit var login: String

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "nickname", unique = true)
    open lateinit var nickname: String

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "email", unique = true)
    open lateinit var email: String

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "passhash")
    private lateinit var passhash: String

    @JdbcTypeCode(SqlTypes.DATE)
    @Column(name = "register_date")
    open lateinit var register_date: Date

    @JdbcTypeCode(SqlTypes.BOOLEAN)
    @Column(name = "is_admin")
    open var is_admin: Boolean? = false

    @OneToMany(mappedBy = "publisher", orphanRemoval = true)
    @JsonManagedReference
    open var mods: MutableSet<Mod> = mutableSetOf()
}