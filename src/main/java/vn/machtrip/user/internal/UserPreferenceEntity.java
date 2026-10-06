package vn.machtrip.user.internal;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "user_preference")
class UserPreferenceEntity {

    @Id
    UUID userId;
    float energy;
    String budgetLevel;
    @JdbcTypeCode(SqlTypes.ARRAY)
    List<String> tags;
    String partyType;
}
