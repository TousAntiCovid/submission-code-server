package fr.gouv.stopc.submission.code.server.database.Entity;

import com.sun.istack.NotNull;
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

    @Column(name = "LOT")
    private long lot;


    @Column(name = "CODE", nullable = false)
    private String code;

    @Column(name = "type_code", nullable = false)
    private char type;

    @Column(name = "DATE_END_VALIDITY", nullable = false)
    private OffsetDateTime dateEndValidity;

    @Column(name = "DATE_AVAILABLE", nullable = false)
    private OffsetDateTime dateAvailable;

    @Column(name = "DATE_USE")
    private OffsetDateTime dateUse;

    @Column(name = "DATE_GENERATION", nullable = false)
    private OffsetDateTime dateGeneration;

    @Column(name = "USED", nullable = false)
    private Boolean used;

}
