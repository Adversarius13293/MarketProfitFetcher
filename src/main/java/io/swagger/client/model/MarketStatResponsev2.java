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
import java.util.ArrayList;
import java.util.List;
import org.threeten.bp.OffsetDateTime;
/**
 * MarketStatResponsev2
 */


public class MarketStatResponsev2 {
  @SerializedName("timestamps")
  private List<OffsetDateTime> timestamps = null;

  @SerializedName("pricesAverage")
  private List<Double> pricesAverage = null;

  @SerializedName("itemCount")
  private List<Long> itemCount = null;

  public MarketStatResponsev2 timestamps(List<OffsetDateTime> timestamps) {
    this.timestamps = timestamps;
    return this;
  }

  public MarketStatResponsev2 addTimestampsItem(OffsetDateTime timestampsItem) {
    if (this.timestamps == null) {
      this.timestamps = new ArrayList<OffsetDateTime>();
    }
    this.timestamps.add(timestampsItem);
    return this;
  }

   /**
   * Get timestamps
   * @return timestamps
  **/
  @Schema(description = "")
  public List<OffsetDateTime> getTimestamps() {
    return timestamps;
  }

  public void setTimestamps(List<OffsetDateTime> timestamps) {
    this.timestamps = timestamps;
  }

  public MarketStatResponsev2 pricesAverage(List<Double> pricesAverage) {
    this.pricesAverage = pricesAverage;
    return this;
  }

  public MarketStatResponsev2 addPricesAverageItem(Double pricesAverageItem) {
    if (this.pricesAverage == null) {
      this.pricesAverage = new ArrayList<Double>();
    }
    this.pricesAverage.add(pricesAverageItem);
    return this;
  }

   /**
   * Get pricesAverage
   * @return pricesAverage
  **/
  @Schema(description = "")
  public List<Double> getPricesAverage() {
    return pricesAverage;
  }

  public void setPricesAverage(List<Double> pricesAverage) {
    this.pricesAverage = pricesAverage;
  }

  public MarketStatResponsev2 itemCount(List<Long> itemCount) {
    this.itemCount = itemCount;
    return this;
  }

  public MarketStatResponsev2 addItemCountItem(Long itemCountItem) {
    if (this.itemCount == null) {
      this.itemCount = new ArrayList<Long>();
    }
    this.itemCount.add(itemCountItem);
    return this;
  }

   /**
   * Get itemCount
   * @return itemCount
  **/
  @Schema(description = "")
  public List<Long> getItemCount() {
    return itemCount;
  }

  public void setItemCount(List<Long> itemCount) {
    this.itemCount = itemCount;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MarketStatResponsev2 marketStatResponsev2 = (MarketStatResponsev2) o;
    return Objects.equals(this.timestamps, marketStatResponsev2.timestamps) &&
        Objects.equals(this.pricesAverage, marketStatResponsev2.pricesAverage) &&
        Objects.equals(this.itemCount, marketStatResponsev2.itemCount);
  }

  @Override
  public int hashCode() {
    return Objects.hash(timestamps, pricesAverage, itemCount);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MarketStatResponsev2 {\n");
    
    sb.append("    timestamps: ").append(toIndentedString(timestamps)).append("\n");
    sb.append("    pricesAverage: ").append(toIndentedString(pricesAverage)).append("\n");
    sb.append("    itemCount: ").append(toIndentedString(itemCount)).append("\n");
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
