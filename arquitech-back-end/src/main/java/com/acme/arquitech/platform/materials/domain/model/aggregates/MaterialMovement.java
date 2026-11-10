package com.acme.arquitech.platform.materials.domain.model.aggregates;
import com.acme.arquitech.platform.iam.domain.model.aggregates.User;
import com.acme.arquitech.platform.materials.domain.model.valueobjects.MovementType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "material_movements", uniqueConstraints = {
    @UniqueConstraint(name = "uk_material_movements_client_request",
                      columnNames = {"material_id", "client_request_id"})
})
@Getter
@NoArgsConstructor
public class MaterialMovement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private MovementType type;
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;
    private String supplier;
    @ManyToOne(optional = false)
    @JoinColumn(name = "registered_by_user_id", nullable = false)
    private User registeredBy;
    @Column(nullable = false)
    private OffsetDateTime occurredAt;
    @Column(length = 2000)
    private String note;
    @Column(name = "client_request_id", length = 128)
    private String clientRequestId;

    public MaterialMovement(Material material, MovementType type, BigDecimal quantity, String supplier,
                            User registeredBy, OffsetDateTime occurredAt, String note) {
        this.material = material;
        this.type = type;
        this.quantity = quantity;
        this.supplier = supplier;
        this.registeredBy = registeredBy;
        this.occurredAt = occurredAt;
        this.note = note;
    }

    public MaterialMovement(Material material, MovementType type, BigDecimal quantity, String supplier,
                            User registeredBy, OffsetDateTime occurredAt, String note, String clientRequestId) {
        this(material, type, quantity, supplier, registeredBy, occurredAt, note);
        this.clientRequestId = clientRequestId;
    }
}
