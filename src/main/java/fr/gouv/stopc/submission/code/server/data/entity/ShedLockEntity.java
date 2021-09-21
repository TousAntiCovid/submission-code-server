package fr.gouv.stopc.submission.code.server.data.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import java.sql.Timestamp;
import java.time.Instant;

@Data
@Entity
@Table(name = "shedlock")
public class ShedLockEntity {

    @Id
    @Column(name = "name", nullable = false)
    private String name = "";

    @Column(name = "lock_until", nullable = false)
    private Timestamp lock_until = Timestamp.from(Instant.now());

    @Column(name = "locked_at", nullable = false)
    private Timestamp locked_at = Timestamp.from(Instant.now());

    @Column(name = "locked_by", nullable = false)
    private String locked_by = "";

}
