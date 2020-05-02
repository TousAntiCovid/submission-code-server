package fr.gouv.stopc.submission.code.server.database.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.OffsetDateTime;


@Data
@Entity
@Table(name ="submission_code")
public class SubmissionCode {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "lot")
    private long lot;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "type_code", nullable = false)
    private String type;

    @Column(name = "date_end_validity")
    private OffsetDateTime dateEndValidity;

    @Column(name = "date_available")
    private OffsetDateTime dateAvailable;

    @Column(name = "date_use")
    private OffsetDateTime dateUse;

    @Column(name = "date_generation")
    private OffsetDateTime dateGeneration;

    @Column(name = "used")
    private Boolean used;

}
