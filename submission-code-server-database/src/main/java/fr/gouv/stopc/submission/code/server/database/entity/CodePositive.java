package fr.gouv.stopc.submission.code.server.database.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.OffsetDateTime;


@Data
@Entity
@Table(name ="codepositive")
public class CodePositive {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "lot")
    private long lot;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "type_code", nullable = false)
    private String type;

    @Column(name = "date_end_validity", nullable = false)
    private OffsetDateTime dateEndValidity;

    @Column(name = "date_available", nullable = false)
    private OffsetDateTime dateAvailable;

    @Column(name = "date_use")
    private OffsetDateTime dateUse;

    @Column(name = "date_generation", nullable = false)
    private OffsetDateTime dateGeneration;

    @Column(name = "used", nullable = false)
    private Boolean used;

}
