package org.vitrivr.cineast.core.util;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import org.vitrivr.cineast.core.data.Pair;

public class OptionalUtil {
  /**
   * If both a value of the first {@code Optional} and a value produced by the supplying function is
   * present, returns a {@code Pair} of both, otherwise returns an empty {@code Optional}.
   *
   * @param first the first {@code Optional}
   * @param secondSupplier the supplying function that produces a second {@code Optional} to be
   *                       returned
   * @return an {@code Optional} describing the value of the first {@code Optional} and the result
   *         produced by the supplying function, if both are present, otherwise an empty
   *         {@code Optional}.
   * @throws NullPointerException if the first {@code Optional} is null or the supplying function or
   *                              its result are {@code null}
   */
  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  public static <T, U> Optional<Pair<T, U>> and(Optional<T> first,
      Supplier<Optional<U>> secondSupplier) {
    Objects.requireNonNull(first);
    Objects.requireNonNull(secondSupplier);
    return first.flatMap(t -> Objects.requireNonNull(secondSupplier.get())
                                     .map(u -> new Pair<>(t, u)));
  }

  /**
   * If a value of the first {@code Optional} is present, returns it, otherwise returns an
   * {@code Optional} produced by the supplying function.
   *
   * @param first the first {@code Optional}
   * @param secondSupplier the supplying function that produces a second {@code Optional} to be
   *                       returned
   * @return an {@code Optional} describing the value of the first {@code Optional}, if present,
   *         otherwise an {@Optional} produced by the supplying function.
   * @throws NullPointerException if the first {@code Optional} is null or the supplying function or
   *                              its result is {@code null}
   */
  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  public static <T> Optional<T> or(Optional<T> first, Supplier<Optional<T>> secondSupplier) {
    Objects.requireNonNull(first);
    Objects.requireNonNull(secondSupplier);
    if (first.isPresent()) {
      return first;
    } else {
      return Objects.requireNonNull(secondSupplier.get());
    }
  }
}
