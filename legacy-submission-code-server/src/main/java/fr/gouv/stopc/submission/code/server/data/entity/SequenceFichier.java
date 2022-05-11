package fr.gouv.stopc.submission.code.server.data.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@Table(name = "seq_fichier")
public class SequenceFichier {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "annee", nullable = false)
    private int annee;

    @Column(name = "mois", nullable = false)
    private int mois;

    @Column(name = "jour", nullable = false)
    private int jour;

    @Column(name = "sequence", nullable = false)
    private int sequence;

}
