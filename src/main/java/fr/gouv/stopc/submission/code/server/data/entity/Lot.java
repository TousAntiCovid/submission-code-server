package fr.gouv.stopc.submission.code.server.data.entity;

import lombok.Data;

import javax.persistence.*;

import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "lot_keys")
public class Lot {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "date_execution", nullable = false)
    private OffsetDateTime dateExecution;

    @Column(name = "number_of_codes", nullable = false)
    private Long numberOfCodes;
}
