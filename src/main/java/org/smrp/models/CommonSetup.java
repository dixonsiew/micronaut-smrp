package org.smrp.models;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.micronaut.serde.annotation.Serdeable;
import io.r2dbc.spi.Row;


@Serdeable
public class CommonSetup {
    
    private Long id;
    private String code;
    private String desc;
    private String ref;

    @JsonProperty("created_by")
    private Long createdBy;

    @JsonProperty("created_date")
    private LocalDateTime createdDate;

    @JsonProperty("modified_by")
    private Long modifiedBy;

    @JsonProperty ("modified_date")
    private LocalDateTime modifiedDate;
    private Boolean deleted;

    @JsonProperty("deleted_by")
    private Long deletedBy;
    
    @JsonProperty("deleted_date")
    private LocalDateTime deletedDate;

    public static CommonSetup fromRow(Row row) {
        CommonSetup commonSetup = new CommonSetup();
        commonSetup.setId(row.get("id", Long.class));
        commonSetup.setCode(row.get("code", String.class));
        commonSetup.setDesc(row.get("desc", String.class));
        commonSetup.setRef(row.get("ref", String.class));
        commonSetup.setCreatedBy(row.get("created_by", Long.class));
        commonSetup.setCreatedDate(row.get("created_date", LocalDateTime.class));
        commonSetup.setModifiedBy(row.get("modified_by", Long.class));
        commonSetup.setModifiedDate(row.get("modified_date", LocalDateTime.class));
        commonSetup.setDeleted(row.get("deleted", Boolean.class));
        commonSetup.setDeletedBy(row.get("deleted_by", Long.class));
        commonSetup.setDeletedDate(row.get("deleted_date", LocalDateTime.class));
        return commonSetup;
    }
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getDesc() {
        return desc;
    }
    
    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public Long getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(Long modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public LocalDateTime getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(LocalDateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public Boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Long getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(Long deletedBy) {
        this.deletedBy = deletedBy;
    }

    public LocalDateTime getDeletedDate() {
        return deletedDate;
    }

    public void setDeletedDate(LocalDateTime deletedDate) {
        this.deletedDate = deletedDate;
    }
}
