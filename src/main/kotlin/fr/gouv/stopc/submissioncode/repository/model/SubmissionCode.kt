package fr.gouv.stopc.submissioncode.repository.model

import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "submission_code")
data class SubmissionCode(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0,

    @Column(name = "code")
    val code: String,

    @Column(name = "type_code")
    val type: String,

    @Column(name = "date_end_validity")
    val dateEndValidity: Instant,

    @Column(name = "date_available")
    val dateAvailable: Instant,

    @Column(name = "date_generation")
    val dateGeneration: Instant,

    @Column(name = "date_use")
    val dateUse: Instant? = null,

    @Column(name = "used")
    val used: Boolean
) {

    enum class Type(val dbValue: String) {
        LONG("1"),
        SHORT("2"),
        TEST("3")
    }
}
