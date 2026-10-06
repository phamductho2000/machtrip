package vn.machtrip.recommend.internal;

import org.springframework.boot.context.properties.ConfigurationProperties;
import vn.machtrip.recommend.BudgetLevel;

import java.time.Duration;

/**
 * @param tagBonus  added to the match factor for every matching tag
 * @param budgetCaps highest price_min (VND) a place may have per budget level; HIGH is uncapped
 */
@ConfigurationProperties("machtrip.recommend")
record RecommendProperties(Duration cacheTtl, double tagBonus, int maxResults, BudgetCaps budgetCaps) {

    record BudgetCaps(int low, int medium) {
    }

    /** Null means no cap. */
    Integer capFor(BudgetLevel level) {
        return switch (level) {
            case LOW -> budgetCaps.low();
            case MEDIUM -> budgetCaps.medium();
            case HIGH -> null;
        };
    }
}
