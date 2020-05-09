package fr.gouv.stopc.submission.code.server.database.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name ="lot_keys")
public class Lot {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
}
