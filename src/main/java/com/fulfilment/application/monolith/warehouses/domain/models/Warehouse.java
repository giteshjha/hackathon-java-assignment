package com.fulfilment.application.monolith.warehouses.domain.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

public class Warehouse {

  @NotBlank(message = "Business unit code is required.")
  public String businessUnitCode;

  @NotBlank(message = "Location is required.")
  public String location;

  @Positive(message = "Capacity must be a positive number.")
  public Integer capacity;

  public Integer stock;

  public LocalDateTime createdAt;

  public LocalDateTime archivedAt;
}
