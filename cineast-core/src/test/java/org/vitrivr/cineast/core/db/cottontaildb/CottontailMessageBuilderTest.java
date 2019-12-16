package org.vitrivr.cineast.core.db.cottontaildb;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.CompoundBooleanPredicate;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.CompoundBooleanPredicate.Operator;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Data;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Where;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.vitrivr.cineast.core.db.RelationalOperator;

class CottontailMessageBuilderTest {

  private final static String fieldname = "test_fieldname";
  private final static RelationalOperator op = RelationalOperator.EQ;

  @Test
  @DisplayName("Single argument for compound or")
  void singleArgCompoundOr() {
    Data data = CottontailMessageBuilder.toData("hello");
    Where where = CottontailMessageBuilder.compoundOrWhere(fieldname, op, data);
    assertEquals(where.getAtomic(), CottontailMessageBuilder.atomicPredicate(fieldname, op, data));
  }

  @Test
  @DisplayName("two arguments for compound or")
  void doubleArgCompoundOr() {
    Data one = CottontailMessageBuilder.toData("hello");
    Data two = CottontailMessageBuilder.toData("world");
    CompoundBooleanPredicate where = CottontailMessageBuilder.compoundOrWhere(fieldname, op, one, two).getCompound();
    assertEquals(where.getOp(), Operator.OR);
    assertEquals(where.getAleft(), CottontailMessageBuilder.atomicPredicate(fieldname, op, one));
    assertEquals(where.getAright(), CottontailMessageBuilder.atomicPredicate(fieldname, op, two));
  }

  @Test
  @DisplayName("multiple arguments for compound or")
  void fourArgCompoundOr() {
    Data one = CottontailMessageBuilder.toData("one");
    Data two = CottontailMessageBuilder.toData("two");
    Data three = CottontailMessageBuilder.toData("three");
    Data four = CottontailMessageBuilder.toData("four");
    CompoundBooleanPredicate where = CottontailMessageBuilder.compoundOrWhere(fieldname, op, one, two, three, four).getCompound();
    assertEquals(where.getOp(), Operator.OR);
    assertEquals(where.getAleft(), CottontailMessageBuilder.atomicPredicate(fieldname, op, one));
    where = where.getCright();
    assertEquals(where.getOp(), Operator.OR);
    assertEquals(where.getAleft(), CottontailMessageBuilder.atomicPredicate(fieldname, op, two));
    where = where.getCright();
    assertEquals(where.getOp(), Operator.OR);
    assertEquals(where.getAleft(), CottontailMessageBuilder.atomicPredicate(fieldname, op, three));
    assertEquals(where.getAright(), CottontailMessageBuilder.atomicPredicate(fieldname, op, four));
  }

}
