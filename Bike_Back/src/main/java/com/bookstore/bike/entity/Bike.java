package com.bookstore.bike.entity;
import jakarta.persistence.*; import java.math.BigDecimal; import java.time.Instant;
@Entity public class Bike {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @ManyToOne(optional=false) private User owner;
 @Column(nullable=false) private String name; private String brand,model,registrationNumber,color,description,pickupLocation,city,imageUrl;
 @Enumerated(EnumType.STRING) private Enums.BikeType type=Enums.BikeType.OTHER; @Enumerated(EnumType.STRING) private Enums.FuelType fuelType=Enums.FuelType.NONE;
 private BigDecimal pricePerHour,pricePerDay,depositAmount; private int manufacturingYear;
 @Enumerated(EnumType.STRING) private Enums.AvailabilityStatus availability=Enums.AvailabilityStatus.AVAILABLE;
 @Enumerated(EnumType.STRING) private Enums.VerificationStatus verificationStatus=Enums.VerificationStatus.PENDING; private Instant createdAt=Instant.now();
 public Long getId(){return id;} public User getOwner(){return owner;} public void setOwner(User v){owner=v;} public String getName(){return name;} public void setName(String v){name=v;}
 public String getRegistrationNumber(){return registrationNumber;} public void setRegistrationNumber(String v){registrationNumber=v;} public String getDescription(){return description;} public void setDescription(String v){description=v;}
 public String getBrand(){return brand;} public void setBrand(String v){brand=v;} public String getModel(){return model;} public void setModel(String v){model=v;} public String getCity(){return city;} public void setCity(String v){city=v;}
 public String getPickupLocation(){return pickupLocation;} public void setPickupLocation(String v){pickupLocation=v;} public String getImageUrl(){return imageUrl;} public void setImageUrl(String v){imageUrl=v;}
 public Enums.BikeType getType(){return type;} public void setType(Enums.BikeType v){type=v;} public Enums.FuelType getFuelType(){return fuelType;} public void setFuelType(Enums.FuelType v){fuelType=v;}
 public BigDecimal getPricePerHour(){return pricePerHour;} public void setPricePerHour(BigDecimal v){pricePerHour=v;} public BigDecimal getPricePerDay(){return pricePerDay;} public void setPricePerDay(BigDecimal v){pricePerDay=v;} public BigDecimal getDepositAmount(){return depositAmount;} public void setDepositAmount(BigDecimal v){depositAmount=v;}
 public Enums.AvailabilityStatus getAvailability(){return availability;} public void setAvailability(Enums.AvailabilityStatus v){availability=v;} public Enums.VerificationStatus getVerificationStatus(){return verificationStatus;} public void setVerificationStatus(Enums.VerificationStatus v){verificationStatus=v;}
}
