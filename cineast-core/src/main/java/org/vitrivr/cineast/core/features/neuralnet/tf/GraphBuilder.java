package org.vitrivr.cineast.core.features.neuralnet.tf;

import org.tensorflow.Graph;
import org.tensorflow.Output;
import org.tensorflow.Tensor;
import org.tensorflow.proto.framework.DataType;
import org.tensorflow.types.TFloat32;
import org.tensorflow.types.TInt32;
import org.tensorflow.types.TString;
import org.tensorflow.types.TUint8;
import org.tensorflow.types.family.TType;

// In the fullness of time, equivalents of the methods of this class should be auto-generated from
// the OpDefs linked into libtensorflow_jni.so. That would match what is done in other languages
// like Python, C++ and Go.

public class GraphBuilder {

  private Graph g;

  public GraphBuilder(Graph g) {
    this.g = g;
  }

  public Output<TFloat32> div(Output<TFloat32> x, Output<TFloat32> y) {
    return binaryOp("Div", x, y);
  }

  public <T extends TType> Output<T> sub(Output<T> x, Output<T> y) {
    return binaryOp("Sub", x, y);
  }

  public <T extends TType> Output<TFloat32> resizeBilinear(Output<T> images, Output<TInt32> size) {
    return binaryOp3("ResizeBilinear", images, size);
  }

  public <T extends TType> Output<T> expandDims(Output<T> input, Output<TInt32> dim) {
    return binaryOp3("ExpandDims", input, dim);
  }

  public <T extends TType, U extends TType> Output<U> cast(Output<T> value, DataType dtype) {
    return g.opBuilder("Cast", "Cast")
        .addInput(value)
        .setAttr("DstT", dtype)
        .build()
        .output(0);
  }

  public Output<TUint8> decodeJpeg(Output<TString> contents, long channels) {
    return g.opBuilder("DecodeJpeg", "DecodeJpeg")
        .addInput(contents)
        .setAttr("channels", channels)
        .build()
        .output(0);
  }

  public <T extends TType> Output<T> constant(String name, Tensor t) {
    return g.opBuilder("Const", name)
        .setAttr("dtype", t.dataType())
        .setAttr("value", t)
        .build()
        .output(0);
  }

  // FIXME: This method used to take a byte array as input, but as it is never used its correctness cannot be verified!
  public Output<TString> constant(String name, String value) {
    return this.constant(name, TString.scalarOf(value));
  }

  public Output<TInt32> constant(String name, int value) {
    return this.constant(name, TInt32.scalarOf(value));
  }

  public Output<TInt32> constant(String name, int[] value) {
    return this.constant(name, TInt32.vectorOf(value));
  }

  public Output<TFloat32> constant(String name, float value) {
    return this.constant(name, TFloat32.scalarOf(value));
  }

  public <T extends TType> Output<T> placeholder(String name, DataType type) {
    return g.opBuilder("Placeholder", name)
        .setAttr("dtype", type)
        .build()
        .output(0);
  }

  private <T extends TType> Output<T> binaryOp(String type, Output<T> in1, Output<T> in2) {
    return g.opBuilder(type, type).addInput(in1).addInput(in2).build().output(0);
  }

  private <T extends TType, U extends TType, V extends TType> Output<T> binaryOp3(String type, Output<U> in1, Output<V> in2) {
    return g.opBuilder(type, type).addInput(in1).addInput(in2).build().output(0);
  }

}