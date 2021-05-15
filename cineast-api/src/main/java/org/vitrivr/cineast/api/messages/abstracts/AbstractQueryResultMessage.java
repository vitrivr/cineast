package org.vitrivr.cineast.api.messages.abstracts;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.api.messages.interfaces.QueryResultMessage;

import java.util.List;

public abstract class AbstractQueryResultMessage<T> implements QueryResultMessage<T> {

  private List<T> content;

  private final Class<T> contentType;

  private final String queryId;

  public AbstractQueryResultMessage(String queryId, Class<T> contentType, List<T> content) {
    this.queryId = queryId;
    this.contentType = contentType;
    this.content = content;
  }

  @Override
  public String getQueryId() {
    return this.queryId;
  }

  @Override
  public List<T> getContent() {
    return this.content;
  }

  @Override
  public Class<T> getContentType() {
    return this.contentType;
  }

  @Override
  public int count() {
    if (this.content != null) {
      return this.content.size();
    } else {
      return 0;
    }
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
  }
}
