package de.upb.crc901.automl.hascoscikitlearnml;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class SLPipeline implements Iterable<SLPipeline> {

  private final String code;

  private List<SLPipeline> children = new LinkedList<>();

  public SLPipeline(final String code) {
    this.code = code;
  }

  @Override
  public String toString() {
    return this.toPythonString(true, "");
  }

  public String toPythonString(final boolean root, final String indent) {
    StringBuilder sb = new StringBuilder();

    sb.append(indent + this.code + "\n");
    for (SLPipeline child : this.children) {
      sb.append(child.toPythonString(false, indent + "\t"));
    }

    return sb.toString();
  }

  public void addChild(final SLPipeline componentInstance) {
    this.children.add(componentInstance);
  }

  @Override
  public Iterator<SLPipeline> iterator() {
    return this.children.iterator();
  }

}
