/*
 * Albion Online Data API
 * No description provided (generated by Swagger Codegen https://github.com/swagger-api/swagger-codegen)
 *
 * OpenAPI spec version: v2
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package io.swagger.client.model;

import java.util.Objects;
import java.util.Arrays;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.IOException;
import org.threeten.bp.OffsetDateTime;
/**
 * MarketHistoryResponse
 */

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-08-06T23:40:29.678+02:00[Europe/Berlin]")
public class MarketHistoryResponse {
  @SerializedName("itemCount")
  private Long itemCount = null;

  @SerializedName("averagePrice")
  private Long averagePrice = null;

  @SerializedName("timestamp")
  private OffsetDateTime timestamp = null;

  public MarketHistoryResponse itemCount(Long itemCount) {
    this.itemCount = itemCount;
    return this;
  }

   /**
   * Get itemCount
   * @return itemCount
  **/
  @Schema(description = "")
  public Long getItemCount() {
    return itemCount;
  }

  public void setItemCount(Long itemCount) {
    this.itemCount = itemCount;
  }

  public MarketHistoryResponse averagePrice(Long averagePrice) {
    this.averagePrice = averagePrice;
    return this;
  }

   /**
   * Get averagePrice
   * @return averagePrice
  **/
  @Schema(description = "")
  public Long getAveragePrice() {
    return averagePrice;
  }

  public void setAveragePrice(Long averagePrice) {
    this.averagePrice = averagePrice;
  }

  public MarketHistoryResponse timestamp(OffsetDateTime timestamp) {
    this.timestamp = timestamp;
    return this;
  }

   /**
   * Get timestamp
   * @return timestamp
  **/
  @Schema(description = "")
  public OffsetDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(OffsetDateTime timestamp) {
    this.timestamp = timestamp;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MarketHistoryResponse marketHistoryResponse = (MarketHistoryResponse) o;
    return Objects.equals(this.itemCount, marketHistoryResponse.itemCount) &&
        Objects.equals(this.averagePrice, marketHistoryResponse.averagePrice) &&
        Objects.equals(this.timestamp, marketHistoryResponse.timestamp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(itemCount, averagePrice, timestamp);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MarketHistoryResponse {\n");
    
    sb.append("    itemCount: ").append(toIndentedString(itemCount)).append("\n");
    sb.append("    averagePrice: ").append(toIndentedString(averagePrice)).append("\n");
    sb.append("    timestamp: ").append(toIndentedString(timestamp)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}