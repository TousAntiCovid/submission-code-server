package fr.gouv.stopc.submissioncode.repository.model

import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "jwt_used")
data class JwtUsed(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0,

    @Column(name = "jti")
    val jti: String,

    @Column(name = "date_use")
    val dateUse: Instant
)
