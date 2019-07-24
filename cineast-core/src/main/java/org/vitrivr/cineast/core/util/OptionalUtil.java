package org.vitrivr.cineast.core.util;

import org.vitrivr.cineast.core.data.Pair;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class OptionalUtil {
  private OptionalUtil() {}

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

  /**
   * If a value of the given {@code Optional} is present, returns a sequential {@link Stream}
   * containing only that value, otherwise returns an empty {@code Stream}.
   *
   * @param optional the {@code Optional}
   * @return the optional value as a {@code Stream}
   * @throws NullPointerException if the given {@code Optional} is null
   */
  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  public static <T> Stream<T> toStream(Optional<T> optional) {
    Objects.requireNonNull(optional);
    return optional
        .map(t -> Stream.of(t))
        .orElse(Stream.empty());
  }

  /**
   * If a value of the given {@code Optional} is present, returns a {@link List} containing only
   * that value, otherwise returns an empty {@code List}.
   *
   * @param optional the {@code Optional}
   * @return the optional value as a {@code List}
   * @throws NullPointerException if the given {@code Optional} is null
   */
  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  public static <T> List<T> toList(Optional<T> optional) {
    return toStream(optional).collect(Collectors.toList());
  }
}
