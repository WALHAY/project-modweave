package git.walhay.modweave.mods

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonManagedReference
import git.walhay.modweave.games.Game
import git.walhay.modweave.user.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
@Table(schema = "modweave", name = "mods")
open class Mod {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JdbcTypeCode(SqlTypes.INTEGER)
    @Column(nullable = false)
    open var id: Long? = null

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "name")
    open lateinit var name: String

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "description")
    open lateinit var description: String

    @ManyToOne(optional = false)
    @JoinColumn(name = "publisher_login", nullable = false)
    @JsonBackReference
    open lateinit var publisher: User

    @ManyToOne(optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    @JsonManagedReference
    open lateinit var game: Game
}