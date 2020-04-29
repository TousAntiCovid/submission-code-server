package fr.gouv.stopc.submission.code.server.database.Entity;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Entity
public class CodePositive {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "LOT")
    private long lot;

    @Column(name = "CODE")
    private String code;

    @Column(name = "TYPE")
    private char type;

    @Column(name = "DATE_END_VALIDITY")
    private OffsetDateTime dateEndValidity;

    @Column(name = "DATE_AVAILABLE")
    private OffsetDateTime dateAvailable;

    @Column(name = "DATE_USE")
    private OffsetDateTime dateUse;

    @Column(name = "DATE_GENERATION")
    private OffsetDateTime dateGeneration;

    @Column(name = "USED")
    private Boolean used;

}
