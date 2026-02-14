package git.walhay.modweave.games

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonManagedReference
import git.walhay.modweave.mods.Mod
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
@Table(schema = "modweave", name = "games")
open class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    open var id: Long? = null

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "name")
    open lateinit var name: String

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "description")
    open lateinit var description: String

    @OneToMany(mappedBy = "game", orphanRemoval = true)
    @JsonBackReference
    open var mods: MutableList<Mod> = mutableListOf()
}