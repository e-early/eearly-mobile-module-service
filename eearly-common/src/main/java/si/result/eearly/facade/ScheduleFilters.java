package si.result.eearly.facade;

import java.util.HashMap;
import org.springframework.data.jpa.domain.Specification;
import si.result.eearly.exception.ServiceInvalidRequestException;
import si.result.eearly.genproto.ErrorCode;
import si.result.r3.filter.Filter;
import si.result.r3.filter.SqmCondition;
import si.result.r3.filter.SqmPredicate;
import si.result.r3.filter.config.RestFilterGenerator;

/**
 * Turns client-supplied schedule filter strings into specifications.
 *
 * <p>Filters may only use fields of the queried entity itself. A field name with a dot follows a
 * relation (for example {@code profiles.dateOfBirth}); such filters are rejected.
 */
public final class ScheduleFilters {

  private static final RestFilterGenerator GENERATOR = new RestFilterGenerator(new HashMap<>());

  private ScheduleFilters() {}

  public static <T> Specification<T> toSpecification(String filter, Class<T> type) {
    Filter parsed = GENERATOR.parse(filter);
    rejectRelationFields(parsed.getPredicate());
    return parsed.toSpecification(type);
  }

  private static void rejectRelationFields(SqmPredicate predicate) {
    if (predicate == null) {
      return;
    }
    SqmCondition condition = predicate.getCondition();
    if (condition != null && condition.getIdentifier() != null
        && condition.getIdentifier().contains(".")) {
      throw new ServiceInvalidRequestException(
          "Filter fields must belong to the queried record, not to related records",
          ErrorCode.VALIDATION_ERROR);
    }
    rejectRelationFields(predicate.getLeft());
    rejectRelationFields(predicate.getRight());
  }
}
